package Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Marker {

    static File answers;
    private static final String ANSWERS_30 = "j30hrs.sm", ANSWERS_60 = "j60hrs.sm",
            ANSWERS_90 = "j90hrs.sm", ANSWERS_120 = "j120hrs.sm", ANSWERS_MOCK = "",
            DOC_DIR = "../Docs/Answers/";
    public static final int J30 = 3, J60 = 6, J90 = 9, J120 =12;
    private static final int SKIP_ANSWER_LIST_FIRST = 4;
    private static int res;
    private static int prefixLen = 3;

    public static void setResult ( int result) {
        res = result;
    }
    /*
    DONE
     */
    private static String answerFileName(String inputName) {
        /*
         * inputName has a presentation as: "jxn_m.res", where
         * j = 'j'
         * x  equals 30,60,90 or 120 and takes a long way back
         * n equals problem's decade
         * m is a position of a problem in a decade
         */
        if (inputName.startsWith("j30")) {
            prefixLen = 3;
            return ANSWERS_30;
        }
        if (inputName.startsWith("j60")){
            prefixLen = 3;
            return ANSWERS_60;
        }
        if (inputName.startsWith("j90")){
            prefixLen = 3;
            return ANSWERS_90;
        }
        if (inputName.startsWith("j120")) {
            prefixLen = 4;
            return ANSWERS_120;
        }
        return ANSWERS_MOCK;
    }
    private static  String answerFileName(int inputClass) {
        switch (inputClass) {
            case J30 -> {prefixLen = 3; return ANSWERS_30;}
            case J60 -> {prefixLen = 3; return ANSWERS_60;}
            case J90 -> {prefixLen = 3; return ANSWERS_90;}
            case J120 -> {prefixLen = 4; return ANSWERS_120;}
            default -> { return ANSWERS_MOCK;}
        }
    }
    private static int getNeededLineNumber(String inputName) {
        int decades = 0, position = 0;
        //decades = Integer.parseInt(inputName.substring(prefixLen, ));
        return SKIP_ANSWER_LIST_FIRST + (decades-1) * 10 + position;
    }
    private static int getNeededLineNumber (int decades, int position) {
        return SKIP_ANSWER_LIST_FIRST  + (decades-1) * 10 + position;
    }
    public static int  getAnswer(int decade, int number) {
        Scanner fileScanner;
        int ret = -273;//no bloody reason, except for compiler
        try {
            fileScanner = new Scanner(answers);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        };
        Scanner lineScanner = new Scanner ("");
        for (int i = 0; i < SKIP_ANSWER_LIST_FIRST; i++) {
            lineScanner = new Scanner(fileScanner.nextLine());
        }
        for (int i = 0; i < decade - 1; i ++) {
            for ( int j = 0 ; j < 10; j++){
                lineScanner = new Scanner(fileScanner.nextLine());
            }
        }
        for (int i = 0; i < number ; i++){
            lineScanner = new Scanner(fileScanner.nextLine());
        }
        for ( int i = 0; i < 3;  i ++) {
            ret = lineScanner.nextInt();
        }
        return ret;

    }
    public static int getSolutionMark(int problemSet, int dec, int pos) {
        answers = new File(DOC_DIR + answerFileName(problemSet));
        int answer = getAnswer(dec, pos);
        return res - answer;
    }
}
