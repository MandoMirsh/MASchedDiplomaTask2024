package Main;

import Main.loggers.SoutLogger;
import Main.models.ProblemModel;
import Main.models.TaskModel;
import Main.readers.RCPFileReader;
import Main.solvers.GreedySolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

import javax.swing.*;
import java.sql.*;
import java.util.Properties;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class MainClass {
    public static void main(String[] args) {

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            //nimbus not available, fall back to system settings
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                //not really happening, otherwise we have bigger problems
            }
        }


        JFrame frame = new JFrame("DiplomaPracticTask");
        frame.setContentPane(new MainForm3().getContentPane());
        //frame.setContentPane(new MainHandler().getContentPane());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setBounds(100, 100, 720, 135);
        frame.revalidate();
        frame.setVisible(true);

    }
}