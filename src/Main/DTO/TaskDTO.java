package Main.DTO;

import java.util.ArrayList;

public class TaskDTO {
    private Integer problemDec, problemNum, selfNum;
    private ArrayList<Integer> ResourceNeed = new ArrayList<>();
    private int timeNeed;
    public String getProblemDesc() {
        return problemDec.toString() + "_" + problemNum;
    }
    public int getTimeNeed() {
        return timeNeed;
    }
    public int getResNeed(int Respos) {
        return ResourceNeed.get(Respos-1);
    }
    public int getTaskNum() {
        return selfNum;
    }
    public void setTaskNum(int taskNum) {
        selfNum = taskNum;
    }
    public void setTimeNeed(int volume) {
        timeNeed = volume;
    }
    public void setResNeed (int resPos, int volume) {
        ResourceNeed.set(resPos-1,volume);
    }

}
