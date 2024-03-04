package Main.checkers;

import Main.DataObjects.InputFileDTO;

public interface FileChecker {
    void setFile(String filename);
    void setFile(InputFileDTO fileInfo);
    boolean check();
    boolean formatCheck();
}
