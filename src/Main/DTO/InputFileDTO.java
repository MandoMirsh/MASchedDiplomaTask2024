package Main.DTO;

public class InputFileDTO {
    private int problemClass, decNum, position;
    private String extension, path = "";

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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
