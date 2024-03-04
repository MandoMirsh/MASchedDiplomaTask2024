package Main.readers;

import Main.models.ProblemModel;
import Main.models.TaskModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import java.io.File;

public class RCPFileReader implements FileReader {

    @Override
    public ProblemModel readFile(String fileName) {
        int m, n ,nn;
        Scanner fileScanner, lineScanner;
        ProblemModel ret = new ProblemModel();
        TaskModel taskBuffer;
        File file = new File(fileName);
        try {
            fileScanner = new Scanner(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lineScanner = new Scanner(fileScanner.nextLine());
            m = lineScanner.nextInt();
            n = lineScanner.nextInt();
        lineScanner = new Scanner(fileScanner.nextLine());
        for (int i = 0; i < n; i++) {
            ret.setRes(lineScanner.nextInt());
        }
        for ( int i = 0; i < m; i++) {
            taskBuffer = new TaskModel();
            lineScanner = new Scanner(fileScanner.nextLine());

            taskBuffer.setTimeNeed(lineScanner.nextInt());

            for (int j = 0; j <n; j++) {
                    taskBuffer.setResourceNeed(lineScanner.nextInt());
            }
            nn = lineScanner.nextInt();
            for ( int j = 0; j < nn; j++) {
                taskBuffer.setSucsessor(lineScanner.nextInt());
            }

            ret.addTask(taskBuffer, i);
        }

        return ret;
    }
}
