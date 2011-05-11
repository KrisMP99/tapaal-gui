package dk.aau.cs.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.SwingWorker.StateValue;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.gui.CreateGui;
import pipe.gui.widgets.FileBrowser;
import dk.aau.cs.gui.components.BatchProcessingResultsTableModel;
import dk.aau.cs.gui.components.MultiLineAutoWrappingToolTip;
import dk.aau.cs.io.batchProcessing.BatchProcessingResultsExporter;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingListener;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions;
import dk.aau.cs.verification.batchProcessing.BatchProcessingWorker;
import dk.aau.cs.verification.batchProcessing.FileChangedEvent;
import dk.aau.cs.verification.batchProcessing.StatusChangedEvent;
import dk.aau.cs.verification.batchProcessing.VerificationTaskCompleteEvent;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions.QueryPropertyOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions.SymmetryOption;

public class BatchProcessingDialog extends JDialog {
	private static final long serialVersionUID = -5682084589335908227L;
	
	private static final String name_verifyTAPN = "VerifyTAPN";
	private static final String name_OPTIMIZEDSTANDARD = "UPPAAL: Optimised Standard Reduction";
	private static final String name_STANDARD = "UPPAAL: Standard Reduction";
	private static final String name_BROADCAST = "UPPAAL: Broadcast Reduction";
	private static final String name_BROADCASTDEG2 = "UPPAAL: Broadcast Degree 2 Reduction";
	private static final String name_AllReductions = "All Verification Methods";
	private static final String name_BFS = "Breadth First Search";
	private static final String name_DFS = "Depth First Search";
	private static final String name_RandomDFS = "Random Depth First Search";
	private static final String name_ClosestToTarget = "Search by Closest To Target First";
	private static final String name_KeepQueryOption = "Do Not Override";
	private static final String name_SEARCHWHOLESTATESPACE = "Search Whole State Space";
	private static final String name_SYMMETRY = "Yes";
	private static final String name_NOSYMMETRY = "No";

	private JPanel filesButtonsPanel;
	private JButton addFilesButton;
	private JButton clearFilesButton;
	private JButton removeFileButton;
	private JList fileList;
	private DefaultListModel listModel;
	
	private JLabel statusLabel;
	private JLabel fileStatusLabel;
	private JButton startButton;
	private JButton cancelButton;
	private JButton skipFileButton;
	private JLabel progressLabel;
	private JLabel timerLabel;
	private long startTimeMs = 0;
	    

	private JComboBox reductionOption;
	private JComboBox searchOption;
	private JButton exportButton;
	private JComboBox queryPropertyOption;
	private JPanel verificationOptionsPanel;
	private JSpinner numberOfExtraTokensInNet;
	private JCheckBox keepQueryCapacity;
	private JComboBox symmetryOption;
	
	private BatchProcessingResultsTableModel tableModel;
	
	private List<File> files = new ArrayList<File>();
	private BatchProcessingWorker currentWorker;

	private Timer timer = new Timer(1000, new AbstractAction() {
		private static final long serialVersionUID = 1327695063762640628L;
		
		public void actionPerformed(ActionEvent e) {
	    	timerLabel.setText((System.currentTimeMillis()-startTimeMs)/1000 + " s");
	    }
	});
	
	private Timer timeoutTimer = new Timer(3*(60*1000), new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			timeoutCurrentVerificationTask();
		}
	});

	private JCheckBox noTimeoutCheckbox;

	private JSpinner timeoutValue;


	public BatchProcessingDialog(Frame frame, String title, boolean modal) {	
		super(frame, title, modal);
		
		addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent we) {
		    	terminateBatchProcessing();
		    }
		});

		initComponents();
	}

	
	private void initComponents() {
		setLayout(new GridBagLayout());
		
		initFileListPanel();
		initVerificationOptionsPanel();
		initMonitorPanel();
		initResultTablePanel();		
	}

	private void initFileListPanel() {
		JPanel fileListPanel = new JPanel(new GridBagLayout());
		fileListPanel.setBorder(BorderFactory.createTitledBorder("Models"));
				
		listModel = new DefaultListModel();
		fileList = new JList(listModel);
		fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fileList.setSelectedIndex(0);
		fileList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DELETE) {
					removeSelectedFiles();
				}
			}
		});
		
		fileList.addListSelectionListener(new ListSelectionListener() {	

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (fileList.getSelectedIndex() == -1) {
						removeFileButton.setEnabled(false);
					} else {
						removeFileButton.setEnabled(true);
					}
				}				
			}
		});
		
		fileList.setCellRenderer(new FileNameCellRenderer());

		JScrollPane scrollpane = new JScrollPane(fileList);
		scrollpane.setMinimumSize(new Dimension(175, 225));
		scrollpane.setPreferredSize(new Dimension(175, 225));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 0, 10, 0);
		fileListPanel.add(scrollpane,gbc);
		
		filesButtonsPanel = new JPanel(new GridBagLayout());
		
		addFilesButton = new JButton("Add Models");
		addFilesButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				addFiles();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 0, 5);
		filesButtonsPanel.add(addFilesButton, gbc);
	
		removeFileButton = new JButton("Remove Models");
		removeFileButton.setEnabled(false);
		removeFileButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				removeSelectedFiles();		
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 5);
		filesButtonsPanel.add(removeFileButton, gbc);
		
		clearFilesButton = new JButton("Clear");
		clearFilesButton.setEnabled(false);
		clearFilesButton.addActionListener(new ActionListener() {	
		
			public void actionPerformed(ActionEvent e) {
				clearFiles();
				enableButtons();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		filesButtonsPanel.add(clearFilesButton, gbc);
		
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		fileListPanel.add(filesButtonsPanel,gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridheight = 2;
		gbc.insets = new Insets(10, 0, 0, 10);
		add(fileListPanel,gbc);
	}
	
	private void addFiles() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new FileNameExtensionFilter("TAPAAL models", new String[] { "xml" }));
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setMultiSelectionEnabled(true);
		int result = fileChooser.showOpenDialog(BatchProcessingDialog.this);
		if(result == JFileChooser.APPROVE_OPTION){
			File[] filesArray = fileChooser.getSelectedFiles();
			for(File file : filesArray) {
				if(!files.contains(file)) {
					files.add(file);
					listModel.addElement(file);
				}
			}
			
			enableButtons();
					
		}
	}
	
	private void removeSelectedFiles() {
		for(Object o : fileList.getSelectedValues()) {
			File file = (File)o;
			files.remove(file);
			listModel.removeElement(file);
		}
		
		enableButtons();
	}

	private void initVerificationOptionsPanel() {
		verificationOptionsPanel = new JPanel(new GridBagLayout());
		verificationOptionsPanel.setBorder(BorderFactory.createTitledBorder("Override Verification Options"));

		initQueryPropertyOptionsComponents();
		initCapacityComponents();
		initSearchOptionsComponents();
		initSymmetryOptionsComponents();
		initReductionOptionsComponents();
		initTimeoutComponents();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1.0;
		gbc.weightx = 0;
		gbc.insets = new Insets(10, 0, 0, 0);
		add(verificationOptionsPanel, gbc);
	}

	private void initQueryPropertyOptionsComponents() {
		JLabel queryLabel = new JLabel("Query:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		verificationOptionsPanel.add(queryLabel,gbc);
		
		String[] options = new String[] { name_KeepQueryOption, name_SEARCHWHOLESTATESPACE };
		queryPropertyOption = new JComboBox(options);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 5, 0);
		verificationOptionsPanel.add(queryPropertyOption,gbc);
	}
	
	private void initCapacityComponents() {
		JLabel capacityLabel = new JLabel("Extra tokens:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		verificationOptionsPanel.add(capacityLabel, gbc);
		
		numberOfExtraTokensInNet = new JSpinner(new SpinnerNumberModel(3, 0, Integer.MAX_VALUE, 1));	
		numberOfExtraTokensInNet.setMaximumSize(new Dimension(50, 30));
		numberOfExtraTokensInNet.setMinimumSize(new Dimension(50, 30));
		numberOfExtraTokensInNet.setPreferredSize(new Dimension(50, 30));
		numberOfExtraTokensInNet.setEnabled(false);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 0, 5, 10);
		verificationOptionsPanel.add(numberOfExtraTokensInNet, gbc);
		
		keepQueryCapacity = new JCheckBox(name_KeepQueryOption);
		keepQueryCapacity.setSelected(true);
		keepQueryCapacity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(keepQueryCapacity.isSelected())
					numberOfExtraTokensInNet.setEnabled(false);
				else
					numberOfExtraTokensInNet.setEnabled(true);
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		verificationOptionsPanel.add(keepQueryCapacity, gbc);
	}
	
	private void initTimeoutComponents() {
		JLabel timeoutLabel = new JLabel("Verification Task Timeout (minutes): ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		verificationOptionsPanel.add(timeoutLabel, gbc);
		
		timeoutValue = new JSpinner(new SpinnerNumberModel(5, 1, Integer.MAX_VALUE, 1));	
		timeoutValue.setMaximumSize(new Dimension(50, 30));
		timeoutValue.setMinimumSize(new Dimension(50, 30));
		timeoutValue.setPreferredSize(new Dimension(50, 30));
		timeoutValue.setEnabled(false);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 0, 5, 10);
		verificationOptionsPanel.add(timeoutValue, gbc);
		
		noTimeoutCheckbox = new JCheckBox("Do not use timeout");
		noTimeoutCheckbox.setSelected(true);
		noTimeoutCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(noTimeoutCheckbox.isSelected())
					timeoutValue.setEnabled(false);
				else
					timeoutValue.setEnabled(true);
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		verificationOptionsPanel.add(noTimeoutCheckbox, gbc);
	}


	private void initReductionOptionsComponents() {
		JLabel reductionLabel = new JLabel("Verification Method:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 0, 5, 5);
		verificationOptionsPanel.add(reductionLabel,gbc);
		
		String[] options = new String[] { name_KeepQueryOption, name_verifyTAPN, name_STANDARD, name_OPTIMIZEDSTANDARD, name_BROADCAST, name_BROADCASTDEG2, name_AllReductions };
		reductionOption = new JComboBox(options);
		
		reductionOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox source = (JComboBox)e.getSource();
				String selectedItem = (String)source.getSelectedItem();
				if(selectedItem != null) {
					setEnabledOptionsAccordingToCurrentReduction();
				}
			}
		});
	
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridwidth = 2;
		verificationOptionsPanel.add(reductionOption,gbc);
	}
	
	private void initSymmetryOptionsComponents() {
		JLabel symmetryLabel = new JLabel("Symmetry:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		verificationOptionsPanel.add(symmetryLabel,gbc);
		
		String[] options = new String[] { name_KeepQueryOption, name_SYMMETRY, name_NOSYMMETRY };
		symmetryOption = new JComboBox(options);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridwidth = 2;
		verificationOptionsPanel.add(symmetryOption,gbc);
	}
	
	private void setEnabledOptionsAccordingToCurrentReduction() {
		refreshSearchOptions();
	}
	
	private void refreshSearchOptions() {
		String currentSearchOption = getSearchOptionAsString();

		String[] options;
		if(getReductionOption() == ReductionOption.VerifyTAPN)
		{
			options = new String[] { name_KeepQueryOption, name_BFS, name_DFS };
		}
		else {
			options = new String[] { name_KeepQueryOption, name_BFS, name_DFS, name_RandomDFS, name_ClosestToTarget };
		}
		
		Dimension d = searchOption.getSize();
		searchOption.removeAllItems();
		boolean selectedOptionStillAvailable = false;	
		for (String s : options) {
			searchOption.addItem(s);
			if (s.equals(currentSearchOption)) {
				selectedOptionStillAvailable = true;
			}
		}
		searchOption.setMinimumSize(d); // stop dropdown box from automatically resizing when we remove options with "longer" names.
		searchOption.setPreferredSize(d);

		if (selectedOptionStillAvailable) {
			searchOption.setSelectedItem(currentSearchOption);
		}
	}
	
	private SearchOption getSearchOption() {
		if(((String)searchOption.getSelectedItem()).equals(name_DFS))
			return SearchOption.DFS;
		else if(((String)searchOption.getSelectedItem()).equals(name_ClosestToTarget))
			return SearchOption.CLOSE_TO_TARGET_FIRST;
		else if(((String)searchOption.getSelectedItem()).equals(name_RandomDFS))
			return SearchOption.RDFS;
		else if(((String)searchOption.getSelectedItem()).equals(name_BFS))
			return SearchOption.BFS;
		else
			return SearchOption.BatchProcessingKeepQueryOption;
	}
	
	private String getSearchOptionAsString() {
		return (String)searchOption.getSelectedItem();
	}


	private void disableVerificationOptionsButtons() {
		verificationOptionsPanel.setEnabled(false);		
		for(Component c : verificationOptionsPanel.getComponents())
			c.setEnabled(false);
	}

	private void enabledVerificationOptionButtons() {
		verificationOptionsPanel.setEnabled(false);		
		for(Component c : verificationOptionsPanel.getComponents())
			c.setEnabled(true);
		
		numberOfExtraTokensInNet.setEnabled(!keepQueryCapacity.isSelected());
		timeoutValue.setEnabled(useTimeout());
	}
	
	private BatchProcessingVerificationOptions getVerificationOptions() {
		return new BatchProcessingVerificationOptions(getQueryPropertyOption(), keepQueryCapacity.isSelected(), getNumberOfExtraTokens(), getSearchOption(), getSymmetryOption(), getReductionOption());		
	}
	
	

	private int getNumberOfExtraTokens() {
		return (Integer)numberOfExtraTokensInNet.getValue();
	}

	private SymmetryOption getSymmetryOption() {
		String symmetryString = (String)symmetryOption.getSelectedItem();
		if(symmetryString.equals(name_SYMMETRY))
			return SymmetryOption.Yes;
		else if(symmetryString.equals(name_NOSYMMETRY))
			return SymmetryOption.No;
		else
			return SymmetryOption.KeepQueryOption;
	}

	private QueryPropertyOption getQueryPropertyOption() {
		String propertyOptionString = (String)queryPropertyOption.getSelectedItem();
		if(propertyOptionString.equals(name_SEARCHWHOLESTATESPACE))
			return QueryPropertyOption.SearchWholeStateSpace;
		else
			return QueryPropertyOption.KeepQueryOption;
	}


	private ReductionOption getReductionOption() {
		String reductionOptionString = (String)reductionOption.getSelectedItem();
		
		if (reductionOptionString.equals(name_STANDARD))
				return ReductionOption.STANDARD;
		else if (reductionOptionString.equals(name_OPTIMIZEDSTANDARD))
				return ReductionOption.OPTIMIZEDSTANDARD;
		else if (reductionOptionString.equals(name_BROADCAST))
				return ReductionOption.BROADCAST;
		else if (reductionOptionString.equals(name_BROADCASTDEG2))
				return ReductionOption.DEGREE2BROADCAST;
		else if (reductionOptionString.equals(name_verifyTAPN))
			return ReductionOption.VerifyTAPN;
		else if (reductionOptionString.equals(name_AllReductions))
			return ReductionOption.BatchProcessingAllReductions;
		else
			return ReductionOption.BatchProcessingKeepQueryOption;
	}
	
	private void initSearchOptionsComponents() {
		JLabel searchLabel = new JLabel("Search Order:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		verificationOptionsPanel.add(searchLabel,gbc);
		
		String[] options = new String[] { name_KeepQueryOption, name_BFS, name_DFS, name_RandomDFS };
		searchOption = new JComboBox(options);
		searchOption.setMinimumSize(searchOption.getSize());
	
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridwidth = 2;
		verificationOptionsPanel.add(searchOption,gbc);
	}

	private void initResultTablePanel() {
		JPanel resultTablePanel = new JPanel(new GridBagLayout());
		resultTablePanel.setBorder(BorderFactory.createTitledBorder("Results"));
		
		exportButton = new JButton("Export");
		exportButton.setEnabled(false);
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportResults();
			}

			private void exportResults() {
				String filename = new FileBrowser("CSV file", "csv", "").saveFile();
				if (filename != null) {
					File exportFile = new File(filename);
					BatchProcessingResultsExporter exporter = new BatchProcessingResultsExporter();
					try {
						exporter.exportToCSV(tableModel.getResults(), exportFile);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(CreateGui.getApp(), "An error occurred while trying to export the results. Please try again", "Error Exporting Results", JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(5, 0, 0, 0);
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		resultTablePanel.add(exportButton, gbc);
		
		tableModel = new BatchProcessingResultsTableModel();
		JTable table = new JTable(tableModel) {
			private static final long serialVersionUID = -146530769055564619L;

			public javax.swing.JToolTip createToolTip() {
				ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE); //disable tooltips disappearing
				ToolTipManager.sharedInstance().setInitialDelay(200);
				return new MultiLineAutoWrappingToolTip();
			};
		};
		ResultTableCellRenderer renderer = new ResultTableCellRenderer(true);
		table.getColumn("Model").setCellRenderer(renderer);
		table.getColumn("Query").setCellRenderer(renderer);
		table.getColumn("Result").setCellRenderer(renderer);
		table.getColumn("Verification Time").setCellRenderer(renderer);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Dimension scrollPaneDims = new Dimension(850,400);
		scrollPane.setMinimumSize(scrollPaneDims);
		scrollPane.setPreferredSize(scrollPaneDims);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		resultTablePanel.add(scrollPane,gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 4;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(10, 5, 5, 5);
		add(resultTablePanel,gbc);
	}

	private void initMonitorPanel() {
		JPanel monitorPanel = new JPanel(new GridBagLayout());
		monitorPanel.setBorder(BorderFactory.createTitledBorder("Monitor"));
	
		JLabel file = new JLabel("File:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(file,gbc);
			
		fileStatusLabel = new JLabel("");
		Dimension fileStatusLabelDim = new Dimension(350, 25);
		fileStatusLabel.setMinimumSize(fileStatusLabelDim);
		fileStatusLabel.setPreferredSize(fileStatusLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(fileStatusLabel,gbc);

		
		JLabel status = new JLabel("Status:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(status,gbc);
			
		statusLabel = new JLabel("");
		statusLabel.setMinimumSize(fileStatusLabelDim);
		statusLabel.setPreferredSize(fileStatusLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		monitorPanel.add(statusLabel,gbc);
		
		JLabel progress = new JLabel("Progress: ");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		monitorPanel.add(progress,gbc);
		
		progressLabel = new JLabel("");
		Dimension dim = new Dimension(280, 25);
		progressLabel.setMinimumSize(dim);
		progressLabel.setPreferredSize(dim);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 10);
		monitorPanel.add(progressLabel, gbc);
		
		JLabel time = new JLabel("Time: ");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 10, 0, 0);
		monitorPanel.add(time, gbc);
		
		timerLabel = new JLabel("");
		Dimension timerLabelDim = new Dimension(50,25);
		timerLabel.setMinimumSize(timerLabelDim);
		timerLabel.setPreferredSize(timerLabelDim);
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 0, 5);
		monitorPanel.add(timerLabel, gbc);

		startButton = new JButton("Start");
		startButton.setEnabled(false);
		startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				process();
			}			
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,0,0, 10);
		monitorPanel.add(startButton, gbc);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				terminateBatchProcessing();
				fileStatusLabel.setText("");
				statusLabel.setText("Batch processing cancelled");
				enableButtons();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,0,0, 10);
		monitorPanel.add(cancelButton, gbc);
		
		skipFileButton = new JButton("Skip");
		skipFileButton.setEnabled(false);
		skipFileButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				skipCurrentFile();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,0,0, 10);
		monitorPanel.add(skipFileButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0;
		gbc.insets = new Insets(10, 0, 0, 0);
		add(monitorPanel,gbc);
	}


	private void process() {
		tableModel.clear();
		currentWorker = new BatchProcessingWorker(files, tableModel, getVerificationOptions());
		currentWorker.addPropertyChangeListener(new PropertyChangeListener(){
		
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals("state")){
					if((StateValue)evt.getNewValue() == StateValue.DONE){
						enableButtons();
						cancelButton.setEnabled(false);
						skipFileButton.setEnabled(false);
						timerLabel.setText("");
						timer.stop();
						timeoutTimer.stop();
					}else if((StateValue)evt.getNewValue() == StateValue.STARTED){
						disableButtonsDuringProcessing();
						cancelButton.setEnabled(true);
						skipFileButton.setEnabled(true);
						timerLabel.setText("");
						progressLabel.setText("0 Verification Tasks Completed");
					}
				}
			}
		});
		currentWorker.addBatchProcessingListener(new BatchProcessingListener() {
			public void fireVerificationTaskStarted() {
				if(timer.isRunning())
					timer.restart();
				else
					timer.start();
				
				if(useTimeout()) {
					if(timeoutTimer.isRunning())
						timeoutTimer.restart();
					else
						timeoutTimer.start();
				}
				
				startTimeMs = System.currentTimeMillis();
			}

			public void fireVerificationTaskComplete(VerificationTaskCompleteEvent e) {
				if(timer.isRunning())
					timer.stop();
				if(timeoutTimer.isRunning())
					timeoutTimer.stop();
				int tasksCompleted = e.verificationTasksCompleted();
				progressLabel.setText(e.verificationTasksCompleted() + " Verification Task" + (tasksCompleted > 1 ? "s" : "") + " Completed");
				timerLabel.setText("");
			}
			
			public void fireStatusChanged(StatusChangedEvent e) {
				statusLabel.setText(e.status());		
			}

	
			public void fireFileChanged(FileChangedEvent e) {
				fileStatusLabel.setText(e.fileName());
			}

		});
		
		if(useTimeout())
			setupTimeoutTimer();
		
		currentWorker.execute();
	}


	private void setupTimeoutTimer() {
		int timeout = (Integer)timeoutValue.getValue();
		timeout = timeout*(60*1000);
		timeoutTimer.setInitialDelay(timeout);
		timeoutTimer.setDelay(timeout);
		timeoutTimer.setRepeats(false);
	}


	private boolean useTimeout() {
		return !noTimeoutCheckbox.isSelected();
	}


	private void terminateBatchProcessing() {
		if(currentWorker != null && !currentWorker.isDone()){
			boolean cancelled = false;
			do{
				currentWorker.notifyExiting();
				cancelled = currentWorker.cancel(true);
			}while(!cancelled);
			System.out.print("Batch processing terminated");
		}
	}
	
	private void skipCurrentFile() {
		if(currentWorker != null && !currentWorker.isDone()){
			currentWorker.notifySkipCurrentVerification();
		}
	}
	
	private void timeoutCurrentVerificationTask() {
		if(currentWorker != null && !currentWorker.isDone()){
			currentWorker.notifyTimeoutCurrentVerificationTask();
		}
	}
	
	
	private void clearFiles() {
		files.clear();
		listModel.removeAllElements();
	}
	
	private void disableButtonsDuringProcessing() {
		addFilesButton.setEnabled(false);
		removeFileButton.setEnabled(false);
		clearFilesButton.setEnabled(false);
		startButton.setEnabled(false);
		exportButton.setEnabled(false);
		fileList.setEnabled(false);
		
		disableVerificationOptionsButtons();
	}

	private void enableButtons() {
		fileList.setEnabled(true);
		addFilesButton.setEnabled(true);
		
		if(listModel.size() > 0) {
			clearFilesButton.setEnabled(true);
			startButton.setEnabled(true);
		} else {
			clearFilesButton.setEnabled(false);
			startButton.setEnabled(false);
		}
		
		if(tableModel.getRowCount() > 0) 
			exportButton.setEnabled(true);
		else
			exportButton.setEnabled(false);
		
		enabledVerificationOptionButtons();
	}
	
	// Custom cell renderer for the file list to only display the name of the file
	// instead of the whole path.
	private class FileNameCellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 3071924451912979500L;

		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if(value instanceof File)
				setText(((File)value).getName());
			else
				setText(value.toString());
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}
	
	// Custom cell renderer for the Query Column of the result table display the property of the query
	private class ResultTableCellRenderer extends JLabel  implements TableCellRenderer {
		private static final long serialVersionUID = 3054497986242852099L;
		Border unselectedBorder = null;
	    Border selectedBorder = null;
	    boolean isBordered = true;
	
	    public ResultTableCellRenderer(boolean isBordered) {
	        this.isBordered = isBordered;
	    }

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isBordered) {
				if (isSelected) {
					setBackground(table.getSelectionBackground());
					setForeground(table.getSelectionForeground());
					
					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2,5,2,5, table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					setBackground(table.getBackground());
					setForeground(table.getForeground());
					
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5, table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}
			
			setEnabled(table.isEnabled());
			setFont(table.getFont());
			setOpaque(true);
			
			if(value != null) {
				if(value instanceof TAPNQuery) {
					TAPNQuery newQuery = (TAPNQuery)value;
					setToolTipText(generateTooltipTextFromQuery(newQuery));
					setText(newQuery.getName());
				} else {
					setToolTipText(value.toString());
					setText(value.toString());
				}
			}
			else {
				setToolTipText("");
				setText("");
			}
			
			return this;
		}

		private String generateTooltipTextFromQuery(TAPNQuery query) {
			StringBuilder s = new StringBuilder();
			s.append("Extra Tokens: ");
			s.append(query.getCapacity());
			s.append("\n\n");
			
			s.append("Search Method: \n");
			if(query.getSearchOption() == SearchOption.DFS)
				s.append(name_DFS);
			else if(query.getSearchOption() == SearchOption.RDFS)
				s.append(name_RandomDFS);
			else if(query.getSearchOption() == SearchOption.CLOSE_TO_TARGET_FIRST)
				s.append(name_ClosestToTarget);
			else
				s.append(name_BFS);
			s.append("\n\n");
			
			s.append("Reduction: \n");
			if (query.getReductionOption() == ReductionOption.STANDARD)
				s.append(name_STANDARD);
			else if (query.getReductionOption() == ReductionOption.OPTIMIZEDSTANDARD)
				s.append(name_OPTIMIZEDSTANDARD);
			else if (query.getReductionOption() == ReductionOption.BROADCAST)
				s.append(name_BROADCAST);
			else if (query.getReductionOption() == ReductionOption.DEGREE2BROADCAST)
				s.append(name_BROADCASTDEG2);
			else if (query.getReductionOption() == ReductionOption.VerifyTAPN) {
				s.append(name_verifyTAPN);
			} else {
				s.append(name_BROADCAST);
			}
			
			s.append("\n\n");
			s.append("Symmetry: ");
			s.append(query.useSymmetry() ? "Yes\n\n" : "No\n\n");
			
			s.append("Query Property:\n"); 
			if(query.getProperty().toString().equals("AG P0>=0"))
				s.append(name_SEARCHWHOLESTATESPACE);
			else
				s.append(query.getProperty().toString());
			
			return s.toString();
		}
	}
}