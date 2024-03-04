package Main.models;

import java.util.ArrayList;

public class TaskModel {

    private int selfNumber, timeNeed;
    private ArrayList<Integer> resourceNeed = new ArrayList<>();
    private ArrayList<Integer> successors = new ArrayList<>();

    public void setResourceNeed( int resVolume, int resPos) {
        resourceNeed.set(resPos-1, resVolume);
    }
    public void setResourceNeed( int resVolume) {
        resourceNeed.add(resVolume);
    }
    public int getResourceNeed(int resPos) {
        return resourceNeed.get(resPos-1);
    }
    public ArrayList<Integer> getResourceNeeds() { return  new ArrayList<Integer>(resourceNeed);}

    public void setTimeNeed( int time) { timeNeed = time; }
    public int getTimeNeed(){
        return timeNeed;
    }

    public TaskModel(int selfPos) {
        this.selfNumber = selfPos;
    }
    public TaskModel(){
        this.selfNumber = 0;
    }
    public void setSelfPos(int pos) {
        this.selfNumber = pos;
    }
    public void setSucsessor( int sucPos) {
        successors.add(sucPos);
    }
    public void setSuccessors(ArrayList<Integer> sucs) {
        successors = new ArrayList<Integer>();
        successors.addAll(sucs);
    }
    public ArrayList<Integer> getSuccessors() {
        return successors;
    }
    public void setResourceNeeds(ArrayList<Integer> resourceNeeds) {
        resourceNeed = new ArrayList<Integer>();
        resourceNeed.addAll(resourceNeeds);
    }

}
