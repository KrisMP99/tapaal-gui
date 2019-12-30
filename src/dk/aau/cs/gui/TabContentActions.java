package dk.aau.cs.gui;

import pipe.gui.GuiFrameActions;
import pipe.gui.Pipe;
import pipe.gui.SafeGuiFrameActions;

import java.io.File;

public interface TabContentActions {

    //public interface UndoRedo {
    void undo();

    void redo();
    //}

    void setApp(GuiFrameActions app);

    void setSafeGuiFrameActions(SafeGuiFrameActions ref);

    void zoomOut();

    void zoomIn();

    void selectAll();

    void deleteSelection();

    //public interface Animation {}
    void stepBackwards();

    void stepForward();

    void timeDelay();

    void delayAndFire();

    void toggleAnimationMode();

    void setMode(Pipe.ElementType mode);

    void showStatistics();

    void importSUMOQueries();

    void importXMLQueries();

    void workflowAnalyse();

    void verifySelectedQuery();

    void previousComponent();

    void nextComponent();

    void exportTrace();

    void importTrace();

    void zoomTo(int newZoomLevel);

    String getTabTitle();

    void saveNet(File outFile);
}
