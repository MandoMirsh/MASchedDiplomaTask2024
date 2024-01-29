package Main.models;

import Main.DTO.SolutionControllerTaskDTO;
import Main.loggers.LogHandler;

public interface SolutionHandler {
    void setProblem(SolutionControllerTaskDTO task);
    void setLog(LogHandler logPlace, LogHandler resultPlace);
    void makeReady();
    void startSolving();
    void runNext();
}
