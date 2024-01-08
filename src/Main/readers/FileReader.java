package Main.readers;

import Main.DTO.InputFileDTO;
import Main.models.Model;
import Main.models.ProblemModel;

public interface FileReader {
    public ProblemModel readFile(String fileName);
}
