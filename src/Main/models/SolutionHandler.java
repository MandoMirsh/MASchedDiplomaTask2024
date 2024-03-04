package Main.models;

import Main.DataObjects.SolutionControllerTaskDO;
import Main.loggers.LogHandler;

public interface SolutionHandler {
    void setProblem(SolutionControllerTaskDO task);
    void setLog(LogHandler logPlace, LogHandler resultPlace);
    void makeReady();
    void startSolving();
    void runNext();
    void finish();
}
