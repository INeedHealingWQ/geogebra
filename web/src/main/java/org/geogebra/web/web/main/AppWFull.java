package org.geogebra.web.web.main;

import java.util.Date;

import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.gui.view.probcalculator.ProbabilityCalculatorView;
import org.geogebra.common.gui.view.spreadsheet.CopyPasteCut;
import org.geogebra.common.gui.view.spreadsheet.DataImport;
import org.geogebra.common.io.OFFHandler;
import org.geogebra.common.javax.swing.GOptionPane;
import org.geogebra.common.kernel.View;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Localization;
import org.geogebra.common.util.opencsv.CSVException;
import org.geogebra.web.html5.gui.GuiManagerInterfaceW;
import org.geogebra.web.html5.gui.ToolBarInterface;
import org.geogebra.web.html5.gui.laf.GLookAndFeelI;
import org.geogebra.web.html5.gui.tooltip.ToolTipManagerW;
import org.geogebra.web.html5.gui.tooltip.ToolTipManagerW.ToolTipLinkType;
import org.geogebra.web.html5.gui.util.CancelEventTimer;
import org.geogebra.web.html5.gui.view.algebra.GeoContainer;
import org.geogebra.web.html5.gui.view.algebra.MathKeyboardListener;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.html5.main.StringHandler;
import org.geogebra.web.html5.util.ArticleElement;
import org.geogebra.web.web.css.GuiResources;
import org.geogebra.web.web.gui.GuiManagerW;
import org.geogebra.web.web.gui.HeaderPanelDeck;
import org.geogebra.web.web.gui.dialog.DialogBoxW;
import org.geogebra.web.web.gui.dialog.DialogManagerW;
import org.geogebra.web.web.gui.layout.DockManagerW;
import org.geogebra.web.web.gui.layout.DockPanelW;
import org.geogebra.web.web.gui.layout.panels.AlgebraStyleBarW;
import org.geogebra.web.web.gui.util.PopupBlockAvoider;
import org.geogebra.web.web.gui.view.algebra.AlgebraViewW;
import org.geogebra.web.web.gui.view.dataCollection.DataCollection;
import org.geogebra.web.web.gui.view.spreadsheet.MyTableW;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class AppWFull extends AppW {

	private DataCollection dataCollection;
	private GuiManagerInterfaceW guiManager = null;
	protected AppWFull(ArticleElement ae, int dimension, GLookAndFeelI laf) {
		super(ae, dimension, laf);
	}

	@Override
	public void showKeyboard(MathKeyboardListener textField) {
		showKeyboard(textField, false);
	}

	public DataCollection getDataCollection() {
		if (this.dataCollection == null) {
			this.dataCollection = new DataCollection(this);
		}
		return this.dataCollection;
	}

	@Override
	public void showKeyboard(MathKeyboardListener textField, boolean forceShow) {
		getAppletFrame().showKeyBoard(true, textField, forceShow);
		if (textField != null) {
			CancelEventTimer.keyboardSetVisible();
		}
	}

	@Override
	public void updateKeyboardHeight() {
		getAppletFrame().updateKeyboardHeight();
	}
	
	@Override
	public void updateKeyBoardField(MathKeyboardListener field) {
		getGuiManager().setOnScreenKeyboardTextField(field);
	}

	@Override
	public final GuiManagerInterfaceW getGuiManager() {
		return guiManager;
	}

	@Override
	public final void initGuiManager() {
		// this should not be called from AppWsimple!
		setWaitCursor();
		guiManager = newGuiManager();
		getGuiManager().setLayout(
				new org.geogebra.web.web.gui.layout.LayoutW(this));
		getGuiManager().initialize();
		setDefaultCursor();
	}

	/**
	 * @return a GuiManager for GeoGebraWeb
	 */
	protected GuiManagerW newGuiManager() {
		return new GuiManagerW(AppWFull.this, getDevice());
	}

	protected abstract GDevice getDevice();

	@Override
	public void hideKeyboard() {
		getAppletFrame().showKeyBoard(false, null, false);
	}

	@Override
	public final boolean letShowPropertiesDialog() {
		return rightClickEnabled
				|| getArticleElement().getDataParamShowMenuBar(false)
				|| getArticleElement().getDataParamApp();
	}

	@Override
	public void updateKeyboard() {

		getGuiManager().focusScheduled(false, false, false);
		getGuiManager().invokeLater(new Runnable() {

			public void run() {
				DockPanelW dp = ((DockManagerW) getGuiManager().getLayout().getDockManager()).getPanelForKeyboard();
				if (dp != null && dp.getKeyboardListener() != null) {
					// dp.getKeyboardListener().setFocus(true);
					dp.getKeyboardListener().ensureEditing();
					dp.getKeyboardListener().setFocus(true, true);
					if (AppWFull.this.isKeyboardNeeded()) {
						getAppletFrame().showKeyBoard(true,
								dp.getKeyboardListener(), true);
					}
				}
				if (!AppWFull.this.isKeyboardNeeded()) {
					getAppletFrame().showKeyBoard(false, null, true);
				}

			}
		});

	}

	@Override
	public void showStartTooltip() {
		if (articleElement.getDataParamShowStartTooltip()) {
			ToolTipManagerW.sharedInstance().setBlockToolTip(false);
			ToolTipManagerW.sharedInstance().showBottomInfoToolTip(
			        getPlain("NewToGeoGebra") + "<br/>"
			                + getPlain("ClickHereToGetHelp"),
			        GeoGebraConstants.QUICKSTART_URL, ToolTipLinkType.Help,
			        this);
			ToolTipManagerW.sharedInstance().setBlockToolTip(true);
		}
	}

	@Override
	public void checkSaved(Runnable runnable) {
		((DialogManagerW) getDialogManager()).getSaveDialog().showIfNeeded(
				runnable);
	}

	@Override
	public void openCSV(String csv) {
			String[][] data = DataImport.parseExternalData(this, csv, true);
		CopyPasteCut cpc = ((MyTableW) getGuiManager().getSpreadsheetView()
				.getSpreadsheetTable()).getCopyPasteCut();
		cpc.pasteExternal(
					data, 0, 0, data.length > 0 ? data[0].length - 1 : 0,
					data.length);
			onOpenFile();
	}

	// maybe this is unnecessary, just I did not want to make error here
	boolean infiniteLoopPreventer = false;

	@Override
	public void focusGained(View v, Element el) {
		super.focusGained(v, el);
		if (getGuiManager() != null) {
			// somehow the panel was not activated in case focus gain
			// so it is good to do here, unless it makes an
			// infinite loop... my code inspection did not find
			// infinite loop, but it is good to try to exclude that
			// anyway, e.g. for future changes in the code
			if (!infiniteLoopPreventer) {
				infiniteLoopPreventer = true;
				getGuiManager().setActiveView(v.getViewID());
				infiniteLoopPreventer = false;
			}
		}
	}

	@Override
	public final void uploadToGeoGebraTube() {

		final PopupBlockAvoider popupBlockAvoider = new PopupBlockAvoider();
		final GeoGebraTubeExportWeb ggbtube = new GeoGebraTubeExportWeb(this);
		getGgbApi().getBase64(true, new StringHandler() {

			@Override
			public void handle(String s) {
				ggbtube.uploadWorksheetSimple(s, popupBlockAvoider);

			}
		});
	}

	@Override
	public void fileNew() {
		super.fileNew();
		resetEVs();

		// make sure file->new->probability does not clear the prob. calc
		if (this.getGuiManager() != null
				&& this.getGuiManager().hasProbabilityCalculator()) {
			((ProbabilityCalculatorView) this.getGuiManager()
					.getProbabilityCalculator()).updateAll();
		}
		// remove all Macros before loading preferences
		kernel.removeAllMacros();
		// reload the saved/(default) preferences
		GeoGebraPreferencesW.getPref().loadXMLPreferences(this);

		resetAllToolbars();
	}

	private void resetAllToolbars() {

		GuiManagerW gm = (GuiManagerW) getGuiManager();

		DockPanelW[] panels = gm.getLayout().getDockManager().getPanels();
		for (DockPanelW panel : panels) {
			if (panel.canCustomizeToolbar()) {
				panel.setToolbarString(panel.getDefaultToolbarString());
			}
		}

		gm.setToolBarDefinition(gm.getDefaultToolbarString());
	}
	
	@Override
	public void openOFF(String content){
		OFFHandler h = new OFFHandler(getKernel(), 
				getKernel().getConstruction());
		h.reset();
		String[] lines = content.split("\n");
		try {
			for (String line : lines) {

				h.addLine(line);

			}
		} catch (CSVException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		h.updateAfterParsing();
		afterLoadFileAppOrNot();

	}

	@Override
	public void showConfirmDialog(String title, String mess) {
		getOptionPane().showInputDialog(this, "", title, mess,
				GOptionPane.OK_CANCEL_OPTION, GOptionPane.PLAIN_MESSAGE, null,
				null, null);
	}

	/**
	 * Updates the stylebar in Algebra View
	 */
	public void updateAVStylebar() {
		if (getGuiManager() != null && getGuiManager().hasAlgebraView()) {
			AlgebraStyleBarW styleBar = ((AlgebraViewW) getView(App.VIEW_ALGEBRA))
					.getStyleBar(false);
			if (styleBar != null) {
				styleBar.update(null);
			}
		}
	}


	@Override
	public void examWelcome(){

		if (isExam() && getExam().getStart() < 0) {
			Localization loc = getLocalization();
			StyleInjector
					.inject(GuiResources.INSTANCE.examStyleLTR().getText());
			final DialogBoxW box = new DialogBoxW(false, true, null, getPanel());
			VerticalPanel mainWidget = new VerticalPanel();
			FlowPanel btnPanel = new FlowPanel();

			Button btnOk = new Button();
			mainWidget.add(btnPanel);
			btnPanel.add(btnOk);
			btnOk.setText(loc.getMenu("StartExam"));
			// mainWidget.add(new Label(getMenu("WelcomeExam")));
			final CheckBox cas = new CheckBox(loc.getMenu("Perspective.CAS"));
			cas.addStyleName("examCheckbox");
			final CheckBox allow3D = new CheckBox(
					loc.getMenu("Perspective.3DGraphics"));
			allow3D.addStyleName("examCheckbox");
			mainWidget.add(cas);
			mainWidget.add(allow3D);
			cas.setValue(true);
			allow3D.setValue(true);
			getExam().setCASAllowed(cas.getValue());
			getExam().set3DAllowed(allow3D.getValue());
			getGuiManager().updateToolbarActions();
			cas.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {
					getExam().setCASAllowed(cas.getValue());
					getGuiManager().updateToolbarActions();
				}
			});
			allow3D.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {
					getExam().set3DAllowed(allow3D.getValue());
					getGuiManager().updateToolbarActions();

				}
			});
			mainWidget.add(btnPanel);
			box.setWidget(mainWidget);
			box.getCaption().setText(getMenu("GeoGebraExam"));
			box.center();
			btnOk.addStyleName("examStartButton");
			btnOk.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {
					Date date = new Date();
					getExam().setStart(date.getTime());
					getGuiManager().updateToolbarActions();
					getGuiManager().updateMenubar();
					DockPanelW dp = ((DockManagerW) getGuiManager().getLayout()
							.getDockManager()).getPanelForKeyboard();
					if (dp != null
							&& dp.getKeyboardListener() instanceof GeoContainer) { // dp.getKeyboardListener().setFocus(true);

						showKeyboard(dp.getKeyboardListener(), true);

					}
					box.hide();

				}
			});
			if (Location.getHost() != null) {
				return;
			}

		}
	}

	public abstract HeaderPanelDeck getAppletFrame();
	@Override
	public ToolBarInterface getToolbar() {
		return getAppletFrame().getToolbar();
	}

}
