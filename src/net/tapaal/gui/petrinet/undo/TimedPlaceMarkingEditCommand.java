package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;

// TODO: Fix this to work on the model class instead of the GUI class
public class TimedPlaceMarkingEditCommand extends Command {
	private final int numberOfTokens;
	private final TimedPlaceComponent timedPlaceComponent;

	public TimedPlaceMarkingEditCommand(TimedPlaceComponent tpc, int numberOfTokens) {
		timedPlaceComponent = tpc;
		this.numberOfTokens = numberOfTokens;
	}

	@Override
	public void redo() {
		if (numberOfTokens > 0) {
			timedPlaceComponent.underlyingPlace().addTokens(Math.abs(numberOfTokens));
		} else {
			timedPlaceComponent.underlyingPlace().removeTokens(Math.abs(numberOfTokens));
		}
		timedPlaceComponent.repaint();
	}

	@Override
	public void undo() {
		if (numberOfTokens > 0) {
			timedPlaceComponent.underlyingPlace().removeTokens(Math.abs(numberOfTokens));
		} else {
			timedPlaceComponent.underlyingPlace().addTokens(Math.abs(numberOfTokens));
		}
		timedPlaceComponent.repaint();
	}

}