package Main.models;
import Main.DTO.InputFileDTO;
import Main.DTO.SolutionControllerTaskDTO;
import Main.Marker;
import Main.loggers.LogHandler;
import Main.readers.FileReader;
import Main.readers.MockReader;
import Main.readers.RCPFileReader;
import Main.readers.SMFileReader;
import Main.solvers.GreedySolver;
import Main.solvers.MockSolver;
import Main.solvers.Solver;
import Main.checkers.*;
public class SolutionHandlerModel {
    private Solver currentSolver;
    private ProblemModel currentProblem;
    private FileChecker currentChecker;
    private FileReader currentReader;
    private SolutionControllerTaskDTO commonTask;
    public static final int SOL_SET_TASK_PLEASE = 0, SOL_READY = 1, SOL_IN_PROGRESS = 2, SOL_CHECKS = 3;
    private LogHandler log, results;
    private String solverName;
    public void setProblem(SolutionControllerTaskDTO task) {
        commonTask = task;
    }
    public void startSolving() {

        currentChecker.setFile(fullFileName(commonTask.getFileInfo()));
        if(!currentChecker.check()){
            log.write("Wrong file format!");
            log.write(fullFileName(commonTask.getFileInfo()));
        }
        else{
            currentProblem = currentReader.readFile(fullFileName(commonTask.getFileInfo()));
            currentSolver.setTask(currentProblem);
            currentSolver.runSolver();
                while(currentSolver.getSolutionStatus() == 0);
            if (currentSolver.getSolutionStatus() > 3) {
                log.write("Unknown error while solving!");
            }
            else {
                int result = currentSolver.getLongevity();
                Marker.setResult(result);
                int set = commonTask.getFileInfo().getProblemClass();
                int resultMark = Marker.getSolutionMark(set,commonTask.getFileInfo().getDecNum(),
                                                            commonTask.getFileInfo().getPosition());
                String resultToLog = "j" + set + "0" + commonTask.getFileInfo().getDecNum() + "_"
                                        + commonTask.getFileInfo().getPosition()+ "," + solverName + ","
                                        + result +"," + (Marker.getAnswer(commonTask.getFileInfo().getDecNum(),
                                            commonTask.getFileInfo().getPosition())) + "," + resultMark;
                results.write(resultToLog);
            }

        }

    }
    public void makeReady() {
        setSolver(commonTask.getSolverType());
        setCheckerReader(commonTask.getFileInfo().getExtension());
        currentSolver.setLogger(log);
    }
    public void runNext(){
        nextTask();
        currentChecker.setFile(fullFileName(commonTask.getFileInfo()));
        if(!currentChecker.check()){
            log.write("Wrong file format!");
            log.write(fullFileName(commonTask.getFileInfo()));
        }
        else{
            currentProblem = currentReader.readFile(fullFileName(commonTask.getFileInfo()));
            currentSolver.setTask(currentProblem);
            currentSolver.runSolver();
            int solutionStatus = currentSolver.getSolutionStatus();
            while(solutionStatus < 3 && solutionStatus >= 0)
                solutionStatus = currentSolver.getSolutionStatus();
            if (solutionStatus != 3) {
                log.write("Unknown error while solving!");
            }
            else {
                int result = currentSolver.getLongevity();
                Marker.setResult(result);
                int set = commonTask.getFileInfo().getProblemClass();
                int resultMark = Marker.getSolutionMark(set,commonTask.getFileInfo().getDecNum(),
                        commonTask.getFileInfo().getPosition());
                String resultToLog = "j" + set + "0" + commonTask.getFileInfo().getDecNum() + "_"
                        + commonTask.getFileInfo().getPosition()+ "," + solverName + ","
                        + result +"," + (Marker.getAnswer(commonTask.getFileInfo().getDecNum(),
                        commonTask.getFileInfo().getPosition())) + "," + resultMark;
                results.write(resultToLog);
            }

        }
    }
    private void nextTask(){
        if (commonTask.getFileInfo().getPosition() == 10) {
            commonTask.getFileInfo().setPosition(1);
            int decnum = commonTask.getFileInfo().getDecNum() + 1;
            commonTask.getFileInfo().setDecNum(decnum);
        }
        else
        {
            int pos = commonTask.getFileInfo().getPosition() + 1;
            commonTask.getFileInfo().setPosition(pos);
        }
    }


    public void setLog (LogHandler logPlace, LogHandler resultPlace) {
            log = logPlace;
            results = resultPlace;
    }
    private void setSolver(int solverId){
        switch(solverId) {
            case SolutionControllerTaskDTO.GREEDY_SOLVER -> {currentSolver = new GreedySolver(); solverName = "Greedy";}//GreedySolver();
            case SolutionControllerTaskDTO.CPLEX_SOLVER -> {currentSolver = new MockSolver(); solverName = "CPLEX";}//CPLEXSolver();
            case SolutionControllerTaskDTO.MA_SOLVER -> {currentSolver = new MockSolver(); solverName = "Multi-Agent";}//MASolver();
            default -> currentSolver = new MockSolver();
        }
    }
    private void setCheckerReader(String extention){
        if (extention.toUpperCase().equals("RCP")) {
            currentChecker = new RCPFileChecker();
            currentReader = new RCPFileReader();
        }
        else {
            currentChecker = new SMFileChecker();
            currentReader = new SMFileReader();
        }

    }
    private String fullFileName (InputFileDTO fileInfo) {
        String filename = fileInfo.getPath() + "j" + fileInfo.getProblemClass() + "0" + fileInfo.getDecNum() + "_" +
                fileInfo.getPosition() + "." + fileInfo.getExtension();
        return filename;
    }

}

