package Main.loggers;

import javax.swing.table.DefaultTableModel;

public class TableLogger implements LogHandler{

    private DefaultTableModel writeTo;
    private String separator;
    @Override
    public void write(String logEntry) {
        writeTo.addRow(splitLogEntry(logEntry));
    }
    public void setSeparator(String sep) {
        separator = sep;
    }
    public void setWriteInto(DefaultTableModel table) {
        writeTo = table;
    }
    private String[] splitLogEntry(String logEntry) {
        return logEntry.split(separator);
    }
}
