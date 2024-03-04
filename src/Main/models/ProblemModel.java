package Main.models;

import java.util.ArrayList;

public class ProblemModel {
    private ArrayList<Integer> rNum = new ArrayList<>();
    private ArrayList<TaskModel> tasks = new ArrayList<>();
    public void setRes(int num, int volume) {
        rNum.add(num - 1, volume);
    }
    public void setRes(int resVol) {
        rNum.add(resVol);
    }
    public int getRnum() {
        return rNum.size();
    }
    public int getRes(int resPos) {
        return rNum.get(resPos - 1);
    }
    public ArrayList<Integer> getResources() {
        return new ArrayList<Integer>(rNum);
    }
    public void setResources(ArrayList<Integer> resources) {
        rNum= new ArrayList<Integer>();
        rNum.addAll(resources);
    }
    public void addTask(TaskModel task, int taskId) {
        tasks.add(taskId, task);
    }
    public ArrayList<TaskModel> getTasks() {
        return new ArrayList<TaskModel>(tasks);
    }

    /**
     * ONLY FOR JACKSON COMPATIBILITY
     * @param rnum
     */
    public void setRnum(int rnum){
    }
}
