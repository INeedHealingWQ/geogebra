package org.geogebra.common.kernel.stepbystep.solution;

import java.util.List;
import java.util.Stack;

import org.geogebra.common.kernel.stepbystep.steptree.StepNode;
import org.geogebra.common.kernel.stepbystep.steptree.StepSolvable;

public class SolutionBuilder {
	private Stack<SolutionStep> previousSteps;
	private SolutionStep currentStep;
	private SolutionStep lastStep;

	public SolutionBuilder() {
		previousSteps = new Stack<>();
		add(SolutionStepType.WRAPPER);
	}

	/**
	 * Get the step tree
	 * 
	 * @return root of the StepTree (always of the type WRAPPER)
	 */
	public SolutionStep getSteps() {
		while (!previousSteps.isEmpty()) {
			currentStep = previousSteps.pop();
		}

		return currentStep;
	}

	/**
	 * Creates a new solution line and adds it to the tree
	 * 
	 * @param type
	 *            SolutionStepType of the SolutionLine
	 * @param color
	 *            color assigned to the SolutionLine
	 */
	public void add(SolutionStepType type, int color) {
		add(new SolutionLine(type, color));
	}

	/**
	 * Creates a new solutions line and adds it to the tree
	 * 
	 * @param type
	 *            SolutionStepType of the SolutionLine
	 * @param arguments
	 *            StepNode arguments of the SolutionLine
	 */
	public void add(SolutionStepType type, StepNode... arguments) {
		add(new SolutionLine(type, arguments));
	}

	/**
	 * Adds a SolutionStep to the solution tree
	 * 
	 * @param newStep
	 *            SolutionStep to add
	 */
	public void add(SolutionStep newStep) {
		if (currentStep == null) {
			currentStep = newStep;
		} else {
			currentStep.addSubStep(newStep);
		}

		lastStep = newStep;
	}

	public void add(StepSolvable ss) {
		add(SolutionStepType.EQUATION, ss);
	}

	/**
	 * Add all the substeps of s to the solution that is being built
	 * 
	 * @param s
	 *            wrapper of substeps to add
	 */
	public void addAll(SolutionStep s) {
		List<SolutionStep> substeps = s.getSubsteps();
		if (substeps != null) {
			for (SolutionStep substep : substeps) {
				currentStep.addSubStep(substep);
			}
		}
	}

	public void addGroup(SolutionStepType groupHeader, SolutionBuilder group, StepNode result) {
		addGroup(new SolutionLine(groupHeader), group, result);
	}

	public void addGroup(SolutionLine groupHeader, SolutionBuilder group, StepNode result) {
		add(SolutionStepType.GROUP_WRAPPER);
		levelDown();
		add(groupHeader);
		levelDown();
		addAll(group.getSteps());
		levelUp();
		add(SolutionStepType.EQUATION, result);
		levelUp();
	}

	public void addSubsteps(StepNode original, StepNode result, SolutionBuilder substeps) {
		add(SolutionStepType.SUBSTEP_WRAPPER);
		levelDown();
		add(SolutionStepType.EQUATION, original);
		addAll(substeps.getSteps());
		add(SolutionStepType.EQUATION, result);
		levelUp();
	}

	/**
	 * Go a level down in the tree. (i.e. indent more)
	 */
	public void levelDown() {
		previousSteps.push(currentStep);
		currentStep = lastStep;
	}

	/**
	 * Go a level up in the tree. (i.e. indent less)
	 */
	public void levelUp() {
		lastStep = currentStep = previousSteps.pop();
	}

	/**
	 * Cleans the solution builder, so you don't have to create a new instance, when
	 * many are needed
	 */
	public void reset() {
		previousSteps = new Stack<>();
		currentStep = lastStep = null;
		add(SolutionStepType.WRAPPER);
	}
}
