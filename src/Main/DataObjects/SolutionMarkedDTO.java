package Main.DataObjects;

public class SolutionMarkedDTO {
    private int problemClass, decNum, position, answer, knownBestAnswer,mark;
    private String solver;
    public int getProblemClass() {
        return problemClass;
    }

    public void setProblemClass(int problemClass) {
        this.problemClass = problemClass;
    }

    public int getDecNum() {
        return decNum;
    }

    public void setDecNum(int decNum) {
        this.decNum = decNum;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public int getKnownBestAnswer() {
        return knownBestAnswer;
    }

    public void setKnownBestAnswer(int knownBestAnswer) {
        this.knownBestAnswer = knownBestAnswer;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public void setSolver(String name) {
        solver = name;
    }
    public String getSolver() {
        return solver;
    }


}
