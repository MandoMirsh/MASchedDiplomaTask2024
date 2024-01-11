package Main.solvers;

import Main.loggers.LogHandler;
import Main.models.ProblemModel;
import Main.models.TaskModel;

import java.util.ArrayList;

public class GreedySolver implements Solver{
    private ProblemModel currentTask;
    private int solutionStatus = SOLUTION_UNSET;
    private LogHandler logPlace;
    public static final int SOLUTION_SET = 1, SOLUTION_STARTED = 2, SOLUTION_FINISHED = 3, SOLUTION_RUN_ERROR = 4,SOLUTION_UNSET = 0;
    private static final int TASK_BLOCKED = 3, TASK_READY = 2, TASK_SET = 1;
    private static final int SUPPORTED_SOLUTION_LEN = 500;
    private ArrayList<Integer> taskStatuses, predecessorsToGo, startPlaceSearch;
    private ArrayList<ArrayList<Integer>> resAvail = new ArrayList<ArrayList<Integer>>();
    @Override
    public void setTask(ProblemModel task) {
            currentTask = task;
            setUp();
            logPlace.write("Solver set up.");
    }

    @Override
    public void runSolver() {

        logPlace.write("Solver running.");
        solutionStatus = SOLUTION_STARTED;
        while (!solverFinished()) {
            solverStep();
        }
        solutionStatus = SOLUTION_FINISHED;

    }

    private void solverStep() {
        int currentJob = chooseNextTask();
        int taskPlace = findTaskPlace(currentJob);
        int currentJobTime = currentTask.getTasks().get(currentJob).getTimeNeed();
        logPlace.write("Trying to find a place for a job number: " + currentJob);
        setTaskPlace(taskPlace,currentJob);
        reserveResource(taskPlace, taskPlace + currentTask.getTasks().get(currentJob).getTimeNeed(),
                        currentTask.getTasks().get(currentJob).getResourceNeed());
        taskStatuses.set(currentJob,TASK_SET);
        //Update Successors' predecessors unready numbers: minus one to every one
        updateSuccessors(taskPlace + currentJobTime, currentTask.getTasks().get(currentJob).getSuccessors());
        updateTaskStatuses();
    }
    private boolean solverFinished(){
        return (taskStatuses.get(taskStatuses.size()-1) == TASK_SET);
    }

    @Override
    public int getSolutionStatus() {
            return solutionStatus;
    }

    @Override
    public int getLongevity() {
        return startPlaceSearch.get(startPlaceSearch.size()-1);
    }

    @Override
    public void setLogger(LogHandler logger) {
        logPlace = logger;
    }
    private void setUp() {
        ArrayList<TaskModel> tasks = currentTask.getTasks();
        //logPlace.write("" + tasks.size());
        taskStatuses = new ArrayList<Integer>();
        predecessorsToGo = new ArrayList<Integer>();
        startPlaceSearch = new ArrayList<Integer>();
        logPlace.write("firstSetUps ready");
        for ( int i = 0; i < tasks.size();i++){
            taskStatuses.add(TASK_BLOCKED);
            predecessorsToGo.add(0);
            startPlaceSearch.add(0);
        }
        taskStatuses.set(0,TASK_READY);
        logPlace.write("next SetUps ready");
        //set up how many predecessors need to proceed
        for ( int i = 0; i < tasks.size(); i++) {
            ArrayList<Integer> successors = tasks.get(i).getSuccessors();
            if (successors.size() > 0){
                addPredecessor(successors);
            }

        }
        logPlace.write("Pre-final set-ups ready");
        //set up resources at all supported length
        setAvailResources(currentTask.getResources());
        solutionStatus = SOLUTION_SET;
        logPlace.write("SetUp ended");
    }
    private int chooseNextTask() {
        for ( int i = 0; i < taskStatuses.size();i++) {
            if (taskStatuses.get(i) == TASK_READY) {
                return i;
            }
        }
     return 0;//No Need Except for Compiler. Code is unreachable when all is OK!!!
    }
    private int findTaskPlace(int taskNum) {
        int possibleStart = startPlaceSearch.get(taskNum);
        int timeNeed = currentTask.getTasks().get(taskNum).getTimeNeed();
        ArrayList<Integer> resToReserve = currentTask.getTasks().get(taskNum).getResourceNeed();
        int clearUpto = possibleStart + 1;
        while (clearUpto != possibleStart) {
            clearUpto = checkAvail(possibleStart, possibleStart + timeNeed, resToReserve);
            if (clearUpto != possibleStart) {
                possibleStart = clearUpto + 1;
            }
        }
        return possibleStart;
    }
    private void setTaskPlace(int start, int taskNum){
            startPlaceSearch.set(taskNum,start);
    }
    private void updateTaskStatuses(){
        for (int i = 0; i < taskStatuses.size();i++) {
            if ((taskStatuses.get(i) == TASK_BLOCKED) && (predecessorsToGo.get(i) == 0)) {
                taskStatuses.set(i,TASK_READY);
            }
        }
    }
    private int checkAvail(int start, int end, ArrayList<Integer> resources){
        int i = end;
        while ((checkAvail(i,resources)) && (i > start)) i--;
        return i;
    }
    private boolean checkAvail(int pos, ArrayList<Integer> resources){
        boolean ret = true;
        for ( int i = 0; i < resources.size();i++) {
            ret &= (resources.get(i) <= resAvail.get(pos).get(i));
        }
        return ret;
    }
    private void updateSuccessors (int jobFinish, ArrayList<Integer> successors) {
        tryUpdateStartPlace(jobFinish,successors);
        updatePredecessors(successors);
    }
    private void tryUpdateStartPlace(int jobFinish, ArrayList<Integer> successors){
        for (Integer successor : successors) {
            if (startPlaceSearch.get(successor  - 1) < jobFinish + 1) {
                setTaskPlace( jobFinish + 1, successor - 1);
            }
        }
    }
    private void updatePredecessors ( ArrayList<Integer> successors) {
        for( Integer successor:successors) {
            int predInactiveNum = predecessorsToGo.get(successor - 1);
            predInactiveNum--;
            predecessorsToGo.set(successor - 1, predInactiveNum);
        }
    }
    private void addPredecessor(ArrayList<Integer> successors) {
        for (Integer successor: successors) {
            int pred = predecessorsToGo.get(successor-1) + 1;
            predecessorsToGo.set(successor - 1,pred);
        }
    }
    private void reserveResource(int timeStart, int timeFinish, ArrayList<Integer> resNeed) {
        int timeLen = timeFinish - timeStart;
        for ( int i = 0; i < timeLen; i++){
            reserveResource(timeStart + i, resNeed);
        }
    }
    private void reserveResource (int timePos, ArrayList<Integer> resNeed) {
        ArrayList<Integer> resByDay = resAvail.get(timePos);
        for( int i = 0; i < resNeed.size(); i++) {
            int toSub = resNeed.get(i);
            int subFrom = resByDay.get(i);
            resByDay.set(i, subFrom - toSub);
        }
        resAvail.set(timePos,resByDay);
    }
    private void setAvailResources(ArrayList<Integer> resourceValues) {
        StringBuilder logWrite = new StringBuilder("Project Resources: ");
        ArrayList<Integer> resourcesToSet;
        for ( Integer x :resourceValues){
            logWrite.append(x).append(" ");
        }
        logPlace.write(logWrite.toString());
        resAvail = new ArrayList<ArrayList<Integer>>();
         for (int i = 0; i < SUPPORTED_SOLUTION_LEN;i++) {
             resourcesToSet = new ArrayList<Integer>(resourceValues);
             resAvail.add(resourcesToSet);
         }
    }
}
