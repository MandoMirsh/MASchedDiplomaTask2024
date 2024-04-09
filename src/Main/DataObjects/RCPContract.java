package Main.DataObjects;

public class RCPContract {
    //WARNING: ONLY FOR MA SOLVER
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getResNeed() {
        return resNeed;
    }

    public void setResNeed(int resNeed) {
        this.resNeed = resNeed;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLongevity() {
        return longevity;
    }

    public void setLongevity(int longevity) {
        this.longevity = longevity;
    }

    String jobName;
    int resNeed;
    int start;
    int longevity;
    @Override
    public String toString(){
        return "Job#" + jobName + ", resources needed: " + resNeed +
                ", time needed: " + longevity + ",starting at: " + start;
    }
}
