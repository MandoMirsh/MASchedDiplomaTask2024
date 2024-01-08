package Main.checkers;

import Main.DTO.InputFileDTO;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class RCPFileChecker implements FileChecker {
    //rcp file:
    private String filePath;
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
        File file = new File(filePath);
        //Если файла нет - возвращаем false
        return file.exists() && !file.isDirectory();
    }
    public boolean formatCheck() {
        int m, n,nn;
        Scanner fileScanner, lineScanner;
        //Если файл присутствует и он верного формата - возвращаем true
        if (!check())
            return false;
        if (!(filePath.toUpperCase().endsWith("RCP")))
            return false;
        //проверяем непосредственно формат
        File file = new File(filePath);
        try {
            fileScanner = new Scanner(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // might be a bit rough, but should be sufficient in case
            // someone deleted file
            // just after we managed to see that it is here
            return false;
        }
        //empty
        if (!fileScanner.hasNextLine())
            return false;
        lineScanner = new Scanner(fileScanner.nextLine());
            if (!lineScanner.hasNextInt())
                return false;
            m = lineScanner.nextInt();

            if (!lineScanner.hasNextInt())
                return false;
            n = lineScanner.nextInt();
            if (lineScanner.hasNext())
                return false;
        if (!fileScanner.hasNextLine())
            return false;
        lineScanner = new Scanner(fileScanner.nextLine());
            for ( int i = 0; i < n; i ++) {
                if (!lineScanner.hasNextInt())
                    return false;
                lineScanner.nextInt();
            }
            if (lineScanner.hasNext())
                return false;
        for (int i = 0; i < m; i ++) {
            if (!fileScanner.hasNextLine())
                return false;
            lineScanner = new Scanner(fileScanner.nextLine());
            for (int j = 0; j < n; j++) {
                if(!lineScanner.hasNextInt())
                    return false;
                lineScanner.nextInt();
            }
            if (!lineScanner.hasNextInt())
                return false;
            nn = lineScanner.nextInt();
            for ( int j = 0; j < nn; j++) {
                if(!lineScanner.hasNextInt())
                    return false;
                lineScanner.nextInt();
            }
            return (!fileScanner.hasNext());
        }




        return true;
    }
    public RCPFileChecker() {

    }
    public RCPFileChecker(String filename) {
            filePath = filename;
    }
}
