package Main.solvers;

import Main.GlobalParamsHandler;
import Main.NameCreator;
import Main.TestAuthorizationParamsStorage;
import Main.loggers.LogHandler;
import Main.models.ProblemModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MASolver implements Solver{
    private static final int NO_TASK = -1;
    public static final int SOLUTION_SET = 1, SOLUTION_STARTED = 2, SOLUTION_FINISHED = 3;
    ProblemModel currentTask;
    LogHandler currentLogger;
    Connection databaseConn = null;
    boolean tempTaskinserted = false;
    static final String getResultFromDBString = "select result from public.results\n" +
            "where id = ?";
    static final String insertString = "insert into public.tasks (id, task)\n" +
            "values ( ?, ?::JSON)";
    static final String deleteTaskString = "delete from public.tasks\n" +
            "where id = ?";
    static final String deleteResult = "delete from results\n" +
            "where id = ?";

    int taskCode = NO_TASK;
    int currentLongevity = NO_TASK;
    private int solutionStatus = 0;
    @Override
    public void setTask(ProblemModel task) {
        ObjectMapper om = new ObjectMapper();
        currentTask = task;
        dbConnectionInit();
        if (tempTaskinserted) {
            try {
                deleteCurrentTaskFromDB();
            } catch (SQLException e) {
                currentLogger.write(ZonedDateTime.now().toString() + ": " + "MASolver" +
                        "encountered a problem wile setting a task: " + e.toString());
            }
        }
        try{
            PreparedStatement ps = databaseConn.prepareStatement(insertString);
            taskCode = getTempTaskCode();
            ps.setInt(1,taskCode);
            ps.setString(2, om.writeValueAsString(currentTask));
            ps.executeUpdate();
            tempTaskinserted = true;
            solutionStatus = SOLUTION_SET;
        }
        catch  (SQLException | JsonProcessingException e) {
            currentLogger.write(ZonedDateTime.now().toString() + ": " + "MASolver" +
                    "encountered a problem while setting a task: " + e.toString());
        }
    }

    @Override
    public void setDBConnection(Connection conn) {
        databaseConn = conn;
    }

    @Override
    public void setTask(int taskId) {
        taskCode = taskId;
        dbConnectionInit();
        solutionStatus = SOLUTION_SET;
    }

    @Override
    public void runSolver() {
        tryTreadWaiting(true);
    }
    void tryTreadWaiting(){
        tryTreadWaiting(false);
    }
    void tryTreadWaiting(boolean silent){
        final String FILE_NAME = NameCreator.generateTempFileName();//"MM" + System.currentTimeMillis() + ".temp";
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        Path newFilePath = Paths.get(FILE_NAME);
        solutionStatus = SOLUTION_STARTED;
        try {
            Files.createFile(newFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        currentLogger.write("Created solver log file successfully");
        File f = new File(FILE_NAME);
        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-classpath");
        command.add(GlobalParamsHandler.getClasspath());
        command.add(jade.Boot.class.getName());
        command.add("-gui");
        command.add("-agents");
        command.add("startAgent:Main.Agents.InitAgent(" + taskCode + ",1)" );
        ProcessBuilder pb = new ProcessBuilder(command);
        //pb.inheritIO();
        pb.redirectOutput(f);
        pb.redirectError(f);
        Process myProcess;
        try {
            myProcess = pb.start();
            myProcess.waitFor();
            getResult();
            if (tempTaskinserted){
                deleteResultFromDB();
                deleteCurrentTaskFromDB();
            }
        } catch (IOException | InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }


        if (silent){
            f.delete();
            currentLogger.write("Silent mode: Deleted solver log file");
        }
        else{
            currentLogger.write("MAsolver log at: " + FILE_NAME);
        }
        solutionStatus = SOLUTION_FINISHED;

    }

    @Override
    public int getSolutionStatus() {
        return solutionStatus;
    }


    private int getTempTaskCode() {
        if (currentTask == null)
            return 9990101;
        else {
            return (currentTask.getTasks().size() - 2) * 1000 + 9901;
        }
    }
    @Override
    public int getLongevity() {
        return currentLongevity;
    }

    @Override
    public void setLogger(LogHandler logger) {
        currentLogger = logger;
    }
    private void getResult() throws SQLException {
        PreparedStatement ps1 = databaseConn.prepareStatement(getResultFromDBString);
        ps1.setInt(1, taskCode);
        ResultSet rs = ps1.executeQuery();
        rs.next();
        currentLongevity = rs.getInt(1);
    }
    private void dbConnectionInit(){
        if (databaseConn == null){
            Properties dbProp = TestAuthorizationParamsStorage.getAuthorization();
            try {
                databaseConn = DriverManager.getConnection(TestAuthorizationParamsStorage.dbConnectTestParams(), dbProp);
            } catch (SQLException e) {
                currentLogger.write(ZonedDateTime.now().toString() + ": " + "MASolver" +
                        "encountered a problem on start of connection: " + e.toString());
            }
        }
    }
    private void deleteResultFromDB() throws SQLException {
        PreparedStatement ps = databaseConn.prepareStatement(deleteResult);
        ps.setInt(1,taskCode);
        ps.executeUpdate();
    }
    private void deleteCurrentTaskFromDB() throws SQLException {
        PreparedStatement ps = databaseConn.prepareStatement(deleteTaskString);
        ps.setInt(1,taskCode);
        ps.executeUpdate();
        tempTaskinserted = false;
        taskCode = NO_TASK;
    }

}
