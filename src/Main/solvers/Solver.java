package Main.solvers;

import Main.models.ProblemModel;

public interface Solver {
    public void setTask (ProblemModel task);
    //give solver a task
    public void runSolver();
    //time to run solver
    public int getSolutionStatus();
    //in progress or ready. Might be boolean?
    public int getLongevity ();
    //0 when not ready, but also can be zero if no time needed
}
