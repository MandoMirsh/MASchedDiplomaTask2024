package Main.readers;

import Main.models.ProblemModel;
import Main.models.TaskModel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class SMFileReader implements FileReader {
    @Override
    public ProblemModel readFile(String fileName) {
        ProblemModel ret = new ProblemModel();
        int jobNum, resNum;
        ArrayList<TaskModel> taskStorage = new ArrayList<>();
        Scanner fileScanner, lineScanner;
        TaskModel taskBuffer;
        File file = new File(fileName);
        try {
            fileScanner = new Scanner(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        skipLines(fileScanner, 5);
        jobNum = getIntAfterColon(fileScanner.nextLine());
        skipLines(fileScanner,2);
        resNum = getIntAfterColon(fileScanner.nextLine());
        skipLines(fileScanner,9);
        for (int i = 0; i < jobNum; i++) {
            taskBuffer = getNewTask(fileScanner.nextLine());
            taskStorage.add(taskBuffer);
        }
        skipLines(fileScanner, 4);
        for (int i = 0; i < jobNum; i++) {
            taskBuffer = taskStorage.get(i);
            lineScanner =  new Scanner (fileScanner.nextLine());
                lineScanner.nextInt();
                lineScanner.nextInt();
                taskBuffer.setTimeNeed(lineScanner.nextInt());
                for (int j = 0; j < resNum; j++) {
                    taskBuffer.setResourceNeed(lineScanner.nextInt());
                }
            taskStorage.set(i,taskBuffer);
        }
        skipLines(fileScanner,3);
        lineScanner = new Scanner( fileScanner.nextLine());
        for ( int i = 0 ; i < resNum; i ++) {
            ret.setRes(lineScanner.nextInt());
        }
        for (int i = 0; i < jobNum; i++) {
            ret.addTask(taskStorage.get(i),i);
        }
        return ret;
    }
    private void skipLines(Scanner fileScanner, int howMany) {
        for ( int i =0; i < howMany; i++){
            skipLine(fileScanner);
        }
    }
    private void skipLine(Scanner fileScanner) {
        fileScanner.nextLine();
    }
    private int getIntAfterColon (String lineFrom) {
        Scanner lineScanner = new Scanner (lineFrom.split(":")[1]);
        return lineScanner.nextInt();
    }
    private TaskModel getNewTask(String lineFrom) {
        int sucNum;
        TaskModel ret = new TaskModel();
        Scanner lineScanner = new Scanner (lineFrom);
        ret.setSelfPos(lineScanner.nextInt());
        lineScanner.nextInt();
        sucNum = lineScanner.nextInt();
        for ( int i = 0; i < sucNum; i++) {
            ret.setSucsessor(lineScanner.nextInt());
        }
        return ret;
    }
}
