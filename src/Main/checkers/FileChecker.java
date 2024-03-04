package Main.checkers;

import Main.DataObjects.InputFileDTO;

public interface FileChecker {
    void setFile(String filename);
    void setFile(InputFileDTO fileInfo);
    default boolean check(){
        return formatCheck();
    };
    boolean formatCheck();
}
