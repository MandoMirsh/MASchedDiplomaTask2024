package Main;

import Main.DTO.InputFileDTO;
import Main.DTO.SolutionControllerTaskDTO;
import Main.loggers.TableLogger;
import Main.loggers.TextPaneLogger;
import Main.models.SolutionHandler;
import Main.models.SolutionHandlerModel;
//import Test.DebugInfoShower;
//import Test.ShowAThingDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import static javax.swing.SwingUtilities.getRoot;

public class MainHandler {
    private final ButtonGroup SolverChooseButtonGroup = new ButtonGroup();
    private JRadioButton rdbtnGreedy;
    private JRadioButton rdbtnMultiAgent;



    private JRadioButton rdbtnCPLEX;
    private JPanel panel1;
    private JPanel leftPanel;
    private JPanel centralPanel;
    private JButton confirmButton;
    private JButton fileChooseButton;
    private JTextField fileChooseTextField;
    private JCheckBox needExtraFilesCheckBox;
    private JSpinner spinnerHowManyFiles;
    private ButtonGroup solverChooseBtnGroup;
    private JPanel panel2 = new JPanel();
    private JFileChooser fileChooser = new JFileChooser("C:\\Users\\seriu0007\\IdeaProjects\\MirshCMCDiploma2023-2024\\src\\Docs\\InputFiles");

    private JPanel fileProcessingPane;
    private JTable table;
    JPanel TopPanel = new JPanel();
    JPanel panel_1 = new JPanel();
    JButton btnNextMove = new JButton("Next");
    JButton btnStopMove = new JButton("Stop");
    JPanel panel = new JPanel();
    JCheckBox automationCheckBox = new JCheckBox("Automate run");
    JLabel lblNewLabel = new JLabel("Experiment Results");
    JSplitPane splitPane = new JSplitPane();
    JTextPane textPane = new JTextPane();
    JScrollPane scrollPane = new JScrollPane(textPane);
    String[] columnNames = {"taskname", "solver", "result", "best", "Mark"};
    String[][] data = {};
    DefaultTableModel tableModel = new DefaultTableModel(data,columnNames);
    JScrollPane scrollPane_1;
    private SolutionHandler solutionHandler = new SolutionHandlerModel();
    private TableLogger tableLogger = new TableLogger();
    private TextPaneLogger paneLogger = new TextPaneLogger();
    private int extraRuns = 0;
    public MainHandler() {

        fileProcessingPane = new JPanel();
        fileProcessingPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        fileProcessingPane.setLayout(new BorderLayout(0, 0));
        fileProcessingPane.add(TopPanel, BorderLayout.SOUTH);
        TopPanel.setLayout(new BorderLayout(0, 0));
        TopPanel.add(panel_1, BorderLayout.EAST);
        panel_1.add(btnNextMove);
        panel_1.add(btnStopMove);
        TopPanel.add(automationCheckBox, BorderLayout.CENTER);
        fileProcessingPane.add(panel, BorderLayout.WEST);
        panel.add(lblNewLabel);
        fileProcessingPane.add(splitPane, BorderLayout.CENTER);
        splitPane.setLeftComponent(scrollPane);
        table = new JTable(tableModel);
        scrollPane_1 = new JScrollPane(table);
        splitPane.setRightComponent(scrollPane_1);
        splitPane.setResizeWeight(0.75);

        needExtraFilesCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spinnerHowManyFiles.setEnabled(!spinnerHowManyFiles.isEnabled());
            }
        });
        fileChooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fileChooseTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileChooseTextField.getText().equals("")){
                    JOptionPane.showMessageDialog(null, "Please choose a file!",
                                                "File not chosen",JOptionPane.WARNING_MESSAGE);
                }
                else {
                    SolutionControllerTaskDTO mainTask = getMainTask();
                    paneLogger.setWriteInto(textPane);
                    tableLogger.setWriteInto(tableModel);
                    tableLogger.setSeparator(",");
                    solutionHandler.setLog(paneLogger, tableLogger);
                    solutionHandler.setProblem(mainTask);
                    solutionHandler.makeReady();
                    JFrame root = (JFrame) getRoot(confirmButton);
                    root.setContentPane(fileProcessingPane);
                    root.setBounds(100, 100, 900, 300);
                    root.invalidate();
                    root.validate();
                    solutionHandler.startSolving();
                }
            }
        });
        btnStopMove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnNextMove.setEnabled(false);
                extraRuns = 0;
                btnStopMove.setEnabled(false);
                paneLogger.write("Omitted all extra tasks for solver.\nYou may exit by clicking cross button.");
            }
        });
        btnNextMove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extraRuns--;
                solutionHandler.runNext();
                if (extraRuns == 0){
                    btnNextMove.setEnabled(false);
                    btnStopMove.setEnabled(false);
                    paneLogger.write("Finished giving all extra tasks for solver.\nYou may exit by clicking cross button.");
                }
            }
        });
    }

    private InputFileDTO getFileDTO (String filePath) {
        InputFileDTO ret = new InputFileDTO();
        int divisor = filePath.lastIndexOf("\\");
        String path = filePath.substring(0, divisor + 1);
        String fileName = filePath.substring(divisor + 1);
        String ext;
        int problemSet, prefixLen = 3, palceParamsLen = 3, pos, subPos;
        int firstSeparator = fileName.indexOf('_'), secondSeparator = fileName.indexOf('.');

        problemSet = Integer.parseInt("" + fileName.charAt(1));
        if (problemSet == 1) {
            prefixLen++;
            problemSet = 12;
        }
        ext = fileName.substring(secondSeparator+1);

        pos = Integer.parseInt( fileName.substring(prefixLen,firstSeparator));
        subPos = Integer.parseInt(fileName.substring(firstSeparator + 1,secondSeparator));

        ret.setPath(path);
        ret.setProblemClass(problemSet);
        ret.setExtension(ext);
        ret.setPosition(subPos);
        ret.setDecNum(pos);
        return ret;
    }

    private SolutionControllerTaskDTO getMainTask () {
        SolutionControllerTaskDTO ret = new SolutionControllerTaskDTO();
        ret.setFileInfo(getFileDTO(fileChooseTextField.getText()));
        if (needExtraFilesCheckBox.isSelected())
        {
            int proceedCount = Integer.parseInt(spinnerHowManyFiles.getValue().toString());
            if (proceedCount >= 0) {
                ret.setHowManyToProceed(proceedCount);
            }
            else ret.setHowManyToProceed(1);
        }
        else {
            ret.setHowManyToProceed(1);
        }
        extraRuns = ret.getHowManyToProceed() - 1;

        ret.setSolver(getSolverId(getSelectedButtonText(solverChooseBtnGroup)));
        return ret;
    }
    private String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }
    private int getSolverId( String buttonName) {
        if (buttonName.equals("Greedy Solver")){
            return SolutionControllerTaskDTO.GREEDY_SOLVER;
        }
        else{
            return SolutionControllerTaskDTO.MA_SOLVER;
        }
    }

    public JPanel getContentPane() {
        return panel1;
    }
}
