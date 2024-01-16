package Main.checkers;

import Main.DTO.InputFileDTO;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
public class SMFileChecker implements FileChecker {

    private String filePath;
    private static final int SEPARATOR_INPUT_COUNT = 7;
    @Override
    public void setFile(String filename) {
        filePath = filename;
    }

    @Override
    public void setFile(InputFileDTO fileInfo) {
        String filename = fileInfo.getPath() + "j" + fileInfo.getProblemClass() + fileInfo.getDecNum() + "_" +
                fileInfo.getPosition() + "." + fileInfo.getExtension();
        this.setFile(filename);
    }

    @Override
    public boolean check() {
        File file = new File((filePath));
        return file.exists() && !file.isDirectory();
    }

    @Override
    public boolean formatCheck() {
        int separatorCount = 0;
        int jobNum, resNum;
        String sepRegEx = "[*]{72}";
        Scanner fileScanner, lineScanner;
        if (!check()){
            return false;
        }
        if (!filePath.toUpperCase().endsWith("SM")){
            return false;
        }
        File file = new File(filePath);
        try {
            fileScanner = new Scanner(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // might be a bit rough, but should be sufficient in case
            // most common case: someone deleted file
            // just after we managed to see that it is here
            return false;
        }
        if (!checkRegExpOnly(fileScanner,sepRegEx)) {
            return false;
        }
        else {
            separatorCount++;
        }
        if (!(skipLines(fileScanner, 2))){
            return false;
        }
        if (!checkRegExpOnly(fileScanner, sepRegEx)) {
            return false;
        }
        else {
            separatorCount++;
        }
        if (!skipLine(fileScanner)) {
            return false;
        }
        if (!fileScanner.hasNextLine()) {
            return false;
        }
        jobNum = getIntAfterColon(fileScanner.nextLine());
        if (!skipLines(fileScanner, 2)) {
            return false;
        }
        if (!fileScanner.hasNextLine()) {
            return false;
        }
        resNum = getIntAfterColon(fileScanner.nextLine());

        if (!skipLines(fileScanner, 2)) {
            return false;
        }
        if (!checkRegExpOnly(fileScanner, sepRegEx)) {
            return false;
        }
        else {
            separatorCount++;
        }
        if (!skipLines(fileScanner, 2)) {
            return false;
        }

        if (!fileScanner.hasNextLine()) {
            return false;
        }
        lineScanner = new Scanner(fileScanner.nextLine());

            if (!lineScanner.hasNextInt()){
                return false;
            }
            lineScanner.nextInt();

            if (!lineScanner.hasNextInt()) {
                return false;
            }
            if (lineScanner.nextInt() != (jobNum - 2)) {
                return false;
            }
        if (!checkRegExpOnly(fileScanner, sepRegEx)) {
            return false;
        }
        else {
            separatorCount++;
        }
        if (!skipLines(fileScanner, 2)) {
            return false;
        }
        if (!skipLines(fileScanner, jobNum)) {
            return false;
        }
        if (!checkRegExpOnly(fileScanner, sepRegEx)) {
            return false;
        }
        else
        {
            separatorCount++;
        }
        if (!skipLines(fileScanner, 3)) {
            return false;
        }
        if (!skipLines(fileScanner, jobNum)) {
            return false;
        }
        if (!checkRegExpOnly(fileScanner, sepRegEx)) {
            return false;
        }
        else
        {
            separatorCount++;
        }
        if (!skipLines(fileScanner, 2)) {
            return false;
        }
        if (!fileScanner.hasNextLine()) {
            return false;
        }
        lineScanner = new Scanner(fileScanner.nextLine());
            for (int i = 0; i <resNum; i++) {
                if (lineScanner.hasNextInt()){
                    lineScanner.nextInt();
                }
                else {
                    return false;
                }
            }
        if (!checkRegExpOnly(fileScanner, sepRegEx)){
            return false;
        }
        else {
            separatorCount++;
        }
        return (separatorCount == SEPARATOR_INPUT_COUNT);
    }
    private boolean checkRegExpOnly(Scanner fileScanner, String regExp) {
        if (!fileScanner.hasNextLine()) {
            return false;
        }
        return fileScanner.nextLine().matches(regExp);
    }
    private boolean skipLines(Scanner fileScanner, int howMany) {
        int i = 0;
        while (skipLine(fileScanner) && (i < howMany)) {
            i++;
        }
        return true;
    }
    private  boolean skipLine(Scanner fileScanner) {
        if (!fileScanner.hasNextLine()) {
            return false;
        }
        fileScanner.nextLine();
        return true;
    }
    private int getIntAfterColon (String lineFrom) {
        Scanner lineScanner = new Scanner (lineFrom.split(":")[1]);
        return lineScanner.nextInt();
    }
}

