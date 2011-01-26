package pipe.gui.widgets;

import java.awt.Color;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.CaretListener;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Transition;
import pipe.gui.CreateGui;
import pipe.gui.GuiView;


/**
 *
 * @author Kenneth and Joakim on a base made by pere
 */
public class TAPNTransitionEditor 
extends javax.swing.JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1744651413834659994L;
	Transition transition;
	boolean attributesVisible;
	boolean timed;
	boolean infiniteServer;
	Integer priority = 0;
	String name;   
	DataLayer pnmlData;
	GuiView view;
	JRootPane rootPane;

	/**
	 * Creates new form TransitionEditor
	 */
	public TAPNTransitionEditor(JRootPane _rootPane, Transition _transition, 
			DataLayer _pnmlData, GuiView _view) {
		rootPane = _rootPane;
		transition = _transition;
		pnmlData = _pnmlData;
		view = _view;
		name = transition.getName();
		timed = transition.isTimed();
		infiniteServer = transition.isInfiniteServer();

		initComponents();

		rootPane.setDefaultButton(okButton);

		attributesVisible = transition.getAttributesVisible();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		transitionEditorPanel = new javax.swing.JPanel();
		nameLabel = new javax.swing.JLabel();
		nameTextField = new javax.swing.JTextField();
		rotationLabel = new javax.swing.JLabel();
		rotationComboBox = new javax.swing.JComboBox();
		buttonPanel = new javax.swing.JPanel();
		cancelButton = new javax.swing.JButton();
		okButton = new javax.swing.JButton();

		setLayout(new java.awt.GridBagLayout());

		transitionEditorPanel.setLayout(new java.awt.GridBagLayout());

		transitionEditorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Transition Editor"));
		nameLabel.setText("Name:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(nameLabel, gridBagConstraints);

		nameTextField.setText(transition.getName());
		nameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				nameTextFieldFocusGained(evt);
			}
			@Override
			public void focusLost(java.awt.event.FocusEvent evt) {
				nameTextFieldFocusLost(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(nameTextField, gridBagConstraints);

		rotationLabel.setText("Rotate:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(rotationLabel, gridBagConstraints);

		rotationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0\u00B0", "+45\u00B0", "+90\u00B0", "-45\u00B0" }));
		rotationComboBox.setMaximumSize(new java.awt.Dimension(120, 20));
		rotationComboBox.setMinimumSize(new java.awt.Dimension(120, 20));
		rotationComboBox.setPreferredSize(new java.awt.Dimension(120, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(rotationComboBox, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		add(transitionEditorPanel, gridBagConstraints);

		buttonPanel.setLayout(new java.awt.GridBagLayout());

		okButton.setText("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonHandler(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		buttonPanel.add(okButton, gridBagConstraints);

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonHandler(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		buttonPanel.add(cancelButton, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 8, 3);
		add(buttonPanel, gridBagConstraints);

	}// </editor-fold>//GEN-END:initComponents

	private void nameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameTextFieldFocusLost
		focusLost(nameTextField);
	}//GEN-LAST:event_nameTextFieldFocusLost

	private void nameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameTextFieldFocusGained
		focusGained(nameTextField);
	}//GEN-LAST:event_nameTextFieldFocusGained


	private void focusGained(javax.swing.JTextField textField){
		textField.setCaretPosition(0);
		textField.moveCaretPosition(textField.getText().length());
	}

	private void focusLost(javax.swing.JTextField textField){
		textField.setCaretPosition(0);
	}   

	CaretListener caretListener = new javax.swing.event.CaretListener() {
		public void caretUpdate(javax.swing.event.CaretEvent evt) {
			JTextField textField = (JTextField)evt.getSource();
			textField.setBackground(new Color(255,255,255));
			//textField.removeChangeListener(this);
		}
	};   

	private void okButtonHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonHandler

		view.getUndoManager().newEdit(); // new "transaction""

		String newName = nameTextField.getText();
		if (!newName.equals(name)){
			if (! Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", newName)){
				System.err.println("Acceptable names for transitions are defined by the regular expression:\n[a-zA-Z][_a-zA-Z]*");
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"Acceptable names for transitions are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
						"Error",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}else if ( (pnmlData.getPlaceByName(newName) != null) || (pnmlData.getTransitionByNameIgnoreGiven(transition, newName) != null) ){
				System.err.println("Transitions cannot be called the same as an other Place or Transition.");
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"Transitions cannot be called the same as another Place or Transition.",
						"Error",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}else {
				view.getUndoManager().addEdit(transition.setPNObjectName(newName));
			}
			//    	  if (!(newName.charAt(0)=='#')){
			//    		  if(newName.contains("*")){
			//        		  System.err.println("Transitions can't have names with *'s or +'s");
			//        		  JOptionPane.showMessageDialog(CreateGui.getApp(),
			//          				"Transitions can't have names with *'s or +'s\n",
			//          				"Error",
			//          				JOptionPane.INFORMATION_MESSAGE);
			//        		  return;
			//        	  }else {
			//        		  view.getUndoManager().addEdit(transition.setPNObjectName(newName));
			//        	  }
			//    	  } else {
			//    		  System.err.println("Transition can't have a name starting with #");
			//    		  JOptionPane.showMessageDialog(CreateGui.getApp(),
			//        				"Transition can't have a name starting with #\n",
			//        				"Error",
			//        				JOptionPane.INFORMATION_MESSAGE);
			//      		  return;
			//    	  }
		}      

		Integer rotationIndex = rotationComboBox.getSelectedIndex();
		if (rotationIndex > 0) {
			int angle = 0;
			switch (rotationIndex) {
			case 1:
				angle = 45;
				break;
			case 2:
				angle = 90;
				break;
			case 3:
				angle = 135; //-45
				break;
			default:
				break;               
			}
			if (angle != 0) {
				view.getUndoManager().addEdit(transition.rotate(angle));
			}
		}
		transition.repaint();
		exit();
	}//GEN-LAST:event_okButtonHandler

	private void exit() {
		rootPane.getParent().setVisible(false);
	}


	private void cancelButtonHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonHandler
		//Provisional!
		exit();
	}//GEN-LAST:event_cancelButtonHandler


	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JButton cancelButton;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameTextField;
	private javax.swing.JButton okButton;
	private javax.swing.JComboBox rotationComboBox;
	private javax.swing.JLabel rotationLabel;
	private javax.swing.JPanel transitionEditorPanel;
	// End of variables declaration//GEN-END:variables

}