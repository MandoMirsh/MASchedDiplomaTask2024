package Main;

import Main.DataObjects.SolutionControllerTaskDO;
import Main.loggers.TableLogger;
import Main.loggers.TextPaneLogger;
import Main.models.DummySolutionHandler;
import Main.models.SolutionHandler;
import Main.models.SolutionHandlerModel;
import Main.DataConstructors.InputFileDataConstructor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Paths;
import java.util.Enumeration;

import static javax.swing.SwingUtilities.getRoot;

public class MainForm3 {
    private JPanel mainPanel;
    private JPanel card1;
    private JPanel card2;
    private JPanel leftTaskPanel;
    private JRadioButton rdbtnGreedy;
    private JRadioButton rdbtnMultiAgent;
    private JRadioButton rdbtnCPLEX;
    private JPanel centralTaskPanel;
    private JButton confirmButton;
    private JCheckBox automationCheckbox;
    private JTextField fileChooseTextField;
    private JButton fileChooseButton;
    private JSpinner spinnerHowManyFiles;
    private JCheckBox needExtraFilesCheckBox;
    private JPanel taskPanel;
    private JPanel processingPanel;
    private JButton btnStopMove;
    private JButton btnNextMove;
    private JTextPane textPane1;
    private JTable table;
    private JScrollPane tablePane;
    private ButtonGroup solverChooseBtnGroup;
    private JFileChooser fileChooser = new JFileChooser(Paths.get("").toAbsolutePath().toString());
    String[] columnNames = {"taskname", "solver", "result", "best", "Mark"};
    String[][] data = {};
    DefaultTableModel tableModel = new DefaultTableModel(data,columnNames);

    private boolean automation = false;
    private SolutionHandler solutionHandler = new SolutionHandlerModel();
    private TableLogger tableLogger = new TableLogger();
    private TextPaneLogger paneLogger = new TextPaneLogger();
    int extraRuns = 0;
    SolutionHandler handler = new SolutionHandlerModel();//DummySolutionHandler();
    public MainForm3() {
        int def = 0, lowBound = 0, upBound = 600, inc = 10;
        table.setModel(tableModel);
        mainPanel.revalidate();
        spinnerHowManyFiles.setModel(new SpinnerNumberModel(def,lowBound,upBound,inc));
        tableLogger.setSeparator(",");
        fileChooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputFilesRedirect = "\\..\\Docs\\InputFiles";
                String currentPath = Paths.get("").toAbsolutePath().toString() + inputFilesRedirect;
                fileChooser.setCurrentDirectory(new File(currentPath));
                int returnVal = fileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fileChooseTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableLogger.setWriteInto(tableModel);
                paneLogger.setWriteInto(textPane1);

                handler.setProblem(getMainTask());
                handler.setLog(paneLogger, tableLogger);
                handler.makeReady();
                handler.startSolving();
                handler.finish();
                setSecondVolumes();
                nextCards();

            }
        });
        automationCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAutomation();
                btnNextMove.setEnabled(!automation);
            }
        });
        btnStopMove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnNextMove.setEnabled(false);
                btnStopMove.setEnabled(false);
                paneLogger.write("Omitted all extra tasks for solver.\n" +
                        "You may exit by clicking cross button after end message");
                handler.finish();
            }
        });
        needExtraFilesCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spinnerHowManyFiles.setEnabled(!spinnerHowManyFiles.isEnabled());
                ((JSpinner.DefaultEditor)(spinnerHowManyFiles.getEditor())).getTextField().setEditable(false);
            }
        });

        btnNextMove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extraRuns--;
                handler.runNext();
                if (extraRuns == 0) {
                    btnNextMove.setEnabled(false);
                    paneLogger.write("All the tasks were set.\n Please click finish button.");
                    //handler.finish();
                }
            }
        });
    }

    public JPanel getContentPane() {
        return mainPanel;
    }
    private void nextCards() {
        ((CardLayout)(mainPanel.getLayout())).next(mainPanel);
    }
    private void processControlSetEnabled(boolean enabled) {
        btnNextMove.setEnabled(enabled);
        btnStopMove.setEnabled(enabled);
    }
    private void setAutomation() {
        processControlSetEnabled(automation);
        automation = !automation;
    }
    private void setSecondVolumes() {
        JFrame root = (JFrame)getRoot(confirmButton);
        root.setBounds(100, 100, 900, 300);
        root.revalidate();
    }
    private SolutionControllerTaskDO getMainTask () {
        SolutionControllerTaskDO ret = new SolutionControllerTaskDO();
        ret.setFileInfo(InputFileDataConstructor.getDTO(fileChooseTextField.getText()));
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
        if (!automation) {
            ret.setHowManyToProceed(1);
        }
        ret.setSolver(getSolverId(getSelectedButtonText(solverChooseBtnGroup)));
        return ret;
    }
    private String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }
    private int getSolverId(String buttonName) {
        if (buttonName.equals("Greedy Solver")){
            return SolutionControllerTaskDO.GREEDY_SOLVER;
        }
        else{
            return SolutionControllerTaskDO.MA_SOLVER;
        }
    }
}
