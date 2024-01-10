package Main.models;

import java.util.ArrayList;

public class TaskModel {

    private int selfNumber, timeNeed;
    private ArrayList<Integer> resourceNeed = new ArrayList<>();
    private ArrayList<Integer> sucsessors = new ArrayList<>();

    public void setResourceNeed ( int resVolume, int resPos) {
        resourceNeed.set(resPos-1, resVolume);
    }
    public void setResourceNeed ( int resVolume) {
        resourceNeed.add(resVolume);
    }
    public int getResourceNeed (int resPos) {
        return resourceNeed.get(resPos-1);
    }
    public ArrayList<Integer> getResourceNeed() { return  new ArrayList<Integer>(resourceNeed);}
    public void setTimeNeed( int time) { timeNeed = time; }
    public int getTimeNeed(){
        return timeNeed;
    }

    public TaskModel(int selfPos) {
        this.selfNumber = selfPos;
    }
    public TaskModel(){
        this.selfNumber = 0;
    };
    public void setSelfPos(int pos) {
        this.selfNumber = pos;
    }
    public void setSucsessor ( int sucPos) {
        sucsessors.add(sucPos);
    }
    public ArrayList<Integer> getSuccessors() {
        return sucsessors;
    }
}
