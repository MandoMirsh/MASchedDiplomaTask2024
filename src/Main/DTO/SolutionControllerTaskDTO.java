package Main.DTO;

import Main.loggers.LogHandler;

public class SolutionControllerTaskDTO {
    public static final int MA_SOLVER = 2,GREEDY_SOLVER = 1, CPLEX_SOLVER = 3;
    public InputFileDTO getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(InputFileDTO fileInfo) {
        this.fileInfo = fileInfo;
    }

    public int getHowManyToProceed() {
        return howManyToProceed;
    }

    public void setHowManyToProceed(int howManyToProceed) {
        this.howManyToProceed = howManyToProceed;
    }
    public void setSolver(int solverId) {
        whichSolverToUSe = solverId;
    }
    public int getSolverType() { return whichSolverToUSe;}
    private InputFileDTO fileInfo;
    private int howManyToProceed;
    private int whichSolverToUSe;

}
