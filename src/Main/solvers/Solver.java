package Main.solvers;

import Main.loggers.LogHandler;
import Main.models.ProblemModel;

import java.sql.Connection;

public interface Solver {
    int SOLUTION_SET = 1, SOLUTION_STARTED = 2, SOLUTION_FINISHED = 3,
            SOLUTION_RUN_ERROR = 4,SOLUTION_NOT_SET = 0;
    public void setTask (ProblemModel task);
    //give solver a task
    public void setDBConnection(Connection conn);
    //where solver should look
    public void setTask(int taskId);
    //which id should solver choose
    public void runSolver();
    //time to run solver
    public int getSolutionStatus();
    //in progress or ready. Might be boolean?
    public int getLongevity ();
    //0 when not ready, but also can be zero if no time needed
    public void setLogger(LogHandler logger);
}
