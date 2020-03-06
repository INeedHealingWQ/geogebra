package org.geogebra.web.html5.gui.laf;

import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.main.GeoGebraColorConstants;
import org.geogebra.common.move.ggtapi.models.Material;
import org.geogebra.web.html5.gui.zoompanel.FullScreenHandler;

/**
 * Ggb specific settings
 */
public class GgbSettings implements VendorSettings {

	@Override
	public String getLicenseURL() {
		return GeoGebraConstants.GGW_ABOUT_LICENSE_URL;
	}

	@Override
	public String getAppTitle(AppConfig config) {
		return config.getAppTitle();
	}

	@Override
	public FullScreenHandler getFullscreenHandler() {
		return null;
	}

	@Override
	public GColor getPrimaryColor() {
		return GeoGebraColorConstants.GEOGEBRA_ACCENT;
	}

	@Override
	public boolean isMainMenuExternal() {
		return true;
	}

	@Override
	public String getMenuLocalizationKey(String key) {
		return key;
	}

	@Override
	public String getStyleName(String styleName) {
		return styleName;
	}

	@Override
	public boolean isGraspableMathEnabled() {
		return true;
	}

	@Override
	public Material.MaterialType getTemplateType() {
		return Material.MaterialType.notesTemplate;
	}

    @Override
    public String getAPIBaseUrl() {
        return "";
    }
}
