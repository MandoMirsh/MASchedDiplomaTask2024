package Main.models;

import Main.DataObjects.SolutionControllerTaskDO;
import Main.loggers.LogHandler;

public interface SolutionHandler {
    int GREEDY_SOLVER = 1, MULTI_AGENT_V1 = 2;
    void setProblem(SolutionControllerTaskDO task);
    void setLog(LogHandler logPlace, LogHandler resultPlace);
    void makeReady();
    void startSolving();
    void runNext();
    void setMarkerMode(int markerMode);
    void finish();
}
