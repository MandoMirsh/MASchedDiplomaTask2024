package Main.workers;

import Main.DataConstructors.TaskInfoDataConstructor;
import Main.DataObjects.ResultDTO;
import Main.DataObjects.TaskInfo;
import Main.loggers.LogHandler;

import javax.swing.*;
import java.util.concurrent.ArrayBlockingQueue;

public class ReporterWorker extends SwingWorker<Void, Void> {
    private final String KILLED_WORKERS_MESSAGE = "Reporter: Finished all workers.";
    private LogHandler reportPlace, resultPlace;
    private ArrayBlockingQueue<ResultDTO> input;
    private String currentSolver = "";
    public void setLogs(LogHandler reportTo, LogHandler resultTo) {
        reportPlace = reportTo;
        resultPlace = resultTo;
    }
    public void setSolverInfo(int solverId) {
        currentSolver = solverName(solverId);
    }
    public void setInput(ArrayBlockingQueue<ResultDTO> in) {
        input = in;
    }
    @Override
    protected Void doInBackground() throws Exception {
        //reportPlace.write("Reporter started.");
        ResultDTO res = new ResultDTO();
        TaskInfo tInfo = new TaskInfo();
        while(true) {
            //reportPlace.write("Reporter: trying to take from input");
            res = input.take();
            //reportPlace.write("Reporter: input taken");
            if (res.taskID == ReaderWorker.SIG_STOP) {
                reportPlace.write(KILLED_WORKERS_MESSAGE);
                return null;
            }
            taskFinishAnnounce(res.taskID);
            tInfo = TaskInfoDataConstructor.getFromSeries(res.taskID);


            resultPlace.write(resultPrepare( taskName(tInfo.getSet(),tInfo.getDec(),tInfo.getPosition()), currentSolver,
                    res.taskEstimate, res.taskResult, res.mark));

        }
    }
    private void taskFinishAnnounce(int taskId){
        reportPlace.write("Task " + taskId + " finished");
    }

    private String resultPrepare(String taskName, String solverName, int answer, int result, int resultMark){
        return taskName + "," + solverName + ","
                + result +"," + answer + "," + resultMark;
    }

    private String taskName(int set,int dec,int pos) {
        return ("j" + set + "0" + dec + "_" + pos);
    }
    private String solverName(int solverCode) {
        return switch (solverCode){
            case 1 -> "Greedy Solver";
            case 2 -> "MA Solver v1";
            default -> "Mock Solver";
        };
    }
}
