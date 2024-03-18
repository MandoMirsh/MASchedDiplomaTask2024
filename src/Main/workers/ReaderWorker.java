package Main.workers;

import Main.TestAuthorizationParamsStorage;
import Main.models.ProblemModel;
import Main.readers.FileReader;
import Main.readers.RCPFileReader;
import Main.readers.SMFileReader;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;

public class ReaderWorker extends SwingWorker<Void, Void> {
    public static final int RCP_EXT = 1, SM_EXT = 2, SIG_STOP = 0, SIG_STOP_WITH_NEXT = 1;
    //private BlockingQueue taskModelQueue =
    private StringBuilder filePath;
    private ArrayBlockingQueue<Integer> input;
    private ArrayBlockingQueue<Integer> output;
    private Connection conn = null;
    String insertString = "insert into public.tasks (id, task)\n" +
            "values ( ?, ?::JSON)";
    private int inputException;
    private FileReader reader;
    public void addOutputQueue(ArrayBlockingQueue<Integer> q){
        output = q;
    }
    public void addInputQueue(ArrayBlockingQueue<Integer> q){
        input = q;
    }
    public void setDBConnectionInfo(){
        Properties dbProp = TestAuthorizationParamsStorage.getAuthorization();
        try {
            conn = DriverManager.getConnection(TestAuthorizationParamsStorage.dbConnectTestParams(), dbProp);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public Void doInBackground() throws Exception {
        PreparedStatement ps = setupPS();
        ProblemModel pm;
        ObjectMapper om = new ObjectMapper();
        int currentTask;
        while (true) {
            currentTask = input.take();
            System.out.println("Got " + currentTask);
            if (currentTask == SIG_STOP) {
                return null;
            }
            if (currentTask == SIG_STOP_WITH_NEXT) {
                int nextNum = input.take() - 1;
                for(int i = 0; i < nextNum; i++){
                    output.add(SIG_STOP);
                }
                output.add(SIG_STOP_WITH_NEXT);
                return null;
            }

            pm = reader.readFile(getFilePath(currentTask));
            ps.setInt(1, currentTask);
            ps.setString(2, om.writeValueAsString(pm));
            ps.executeUpdate();
            output.put(currentTask);
        }
    }
    @Override
    protected void done() {
        closeConnection();
        super.done();
    }

    private void closeConnection(){
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("Reader worker could not close result set");
        }
        catch (Throwable e) {
            System.err.println("Reader worker encountered a problem with JDBC driver upon closing connection");
        }
    }
    public void setInputType(int inputType){
        inputException = inputType;
        if (inputType == RCP_EXT) {
            reader = new RCPFileReader();
        }
        else {
            reader = new SMFileReader();
        }
    }
    public void setFileLocation(String path) {
        filePath = new StringBuilder(path);
    }
    private String getFilePath(int fileId) {
        StringBuilder ret = new StringBuilder(filePath);
        ret.append("\\");
        ret.append('j');
        ret.append(fileId / 100 / 100);
        ret.append('0');
        ret.append(fileId / 100 % 100);
        ret.append('_');
        ret.append(fileId % 100);

        if (inputException == ReaderWorker.SM_EXT) {
            ret.append(".SM");
        }
        else {
            ret.append(".RCP");
        }
        return ret.toString();
    }
    private PreparedStatement setupPS() {
        PreparedStatement ps;
        try {
            ps = conn.prepareStatement(insertString);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ps;
    }
}
