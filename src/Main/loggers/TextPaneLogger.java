package Main.loggers;


import javax.swing.*;

public class TextPaneLogger implements LogHandler {

    private JTextPane writeTo;
    @Override
    public void write(String logEntry) {
        writeTo.setText(writeTo.getText() + logEntry + "\n");
    }
    public void setWriteInto( JTextPane pane ) {
        writeTo = pane;
    }
}
