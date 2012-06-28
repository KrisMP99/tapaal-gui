package pipe.gui;

import java.awt.Color;

public class Pipe {
   
	// Filesystem Definitions
	public static final String PROPERTY_FILE_EXTENSION = ".properties";
	public static final String PROPERTY_FILE_DESC = "PIPE Properties file";
	public static final String CLASS_FILE_EXTENSION = ".class";
	public static final String CLASS_FILE_DESC = "Java Class File";

	//Enum for all actions and types of elements
	public static enum ElementType {
		PLACE, IMMTRANS, TIMEDTRANS, ANNOTATION, ARC, INHIBARC, 
		//TAPN Elements
		TAPNPLACE, TAPNTRANS, TAPNARC, TRANSPORTARC, TAPNINHIBITOR_ARC,
		//Others (might refactore)
		ADDTOKEN, DELTOKEN, SELECT, DELETE, DRAW, GRID, DRAG, CREATING,
		//Fast modes
		FAST_TRANSITION, FAST_PLACE, 
		//Others (refactore?)
		STOP, STEPBACKWARD, STEPFORWARD, FIRE, START, RANDOM, ANIMATE, TIMEPASS, VERIFY
		
	}

	public static final ElementType DEFAULT_ELEMENT_TYPE = ElementType.SELECT;

	public static final int PLACE_TRANSITION_HEIGHT = 30;
	public static final int DASHED_PADDING = 8;

	public static final Color ENABLED_TRANSITION_COLOUR = new Color(192, 0, 0);
	public static final Color ELEMENT_LINE_COLOUR = Color.BLACK;
	public static final Color ELEMENT_FILL_COLOUR = Color.WHITE;
	public static final Color SELECTION_LINE_COLOUR = new Color(0, 0, 192);
	public static final Color SELECTION_FILL_COLOUR = new Color(192, 192, 255);
	public static final Color SELECTION_TEXT_COLOUR = SELECTION_LINE_COLOUR;
	public static final Color ELEMENT_TEXT_COLOUR = ELEMENT_LINE_COLOUR;

	// For ArcPath:
	public static final int ARC_CONTROL_POINT_CONSTANT = 3;
	public static final int ARC_PATH_SELECTION_WIDTH = 6;
	public static final int ARC_PATH_PROXIMITY_WIDTH = 10;

	// For Place/Transition Arc Snap-To behaviour:
	public static final int PLACE_TRANSITION_PROXIMITY_RADIUS = 25;

	// Object layer positions for GuiView:
	public static final int WHITE_LAYER_OFFSET = 80;
	public static final int ARC_POINT_LAYER_OFFSET = 50;
	public static final int ARC_LAYER_OFFSET = 20;
	public static final int PLACE_TRANSITION_LAYER_OFFSET = 30;
	public static final int NOTE_LAYER_OFFSET = 10;
	public static final int SELECTION_LAYER_OFFSET = 90;
	public static final int LOWEST_LAYER_OFFSET = 0;
	

	// For AnnotationNote appearance:
	public static final int RESERVED_BORDER = 12;
	public static final int ANNOTATION_SIZE_OFFSET = 4;
	public static final int ANNOTATION_MIN_WIDTH = 40;
	public static final Color NOTE_DISABLED_COLOUR = Color.BLACK;
	public static final Color NOTE_EDITING_COLOUR = Color.BLACK;
	public static final Color RESIZE_POINT_DOWN_COLOUR = new Color(220, 220,
			255);
	public static final String ANNOTATION_DEFAULT_FONT = "Helvetica";
	public static final int ANNOTATION_DEFAULT_FONT_SIZE = 12;

	public static final String LABEL_FONT = "Dialog";
	public static final int LABEL_DEFAULT_FONT_SIZE = 10;

	public static final int DEFAULT_OFFSET_X = -5;
	public static final int DEFAULT_OFFSET_Y = 35;

	public static final int NAMELABEL_OFFSET = 12;

	public static int DEFAULT_BUFFER_CAPACITY = 50;

	public static boolean JOIN_ARCS = false;

	public static final int ZOOM_DELTA = 10;
	public static final int ZOOM_MAX = 300;
	public static final int ZOOM_MIN = 40;
	public static final int ZOOM_DEFAULT = 100;

	public static Color BACKGROUND_COLOR = new Color(255, 255, 255, 200);
	public static Color ANIMATION_BACKGROUND_COLOR = new Color(246, 250, 255);

	public static final int MAX_NODES = 20000;

	public static final int verifytaMinRev = 4543;// 4409;
	public static final int AGE_DECIMAL_PRECISION = 5;
	public static final int AGE_PRECISION = AGE_DECIMAL_PRECISION + 4;

}
