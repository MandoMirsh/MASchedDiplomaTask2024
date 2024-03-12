package Main.workers;

import Main.DataConstructors.TaskInfoDataConstructor;
import Main.DataObjects.ResultDTO;
import Main.DataObjects.TaskInfo;
import Main.Marker;
import Main.TestAuthorizationParamsStorage;

import javax.swing.*;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;

public class MarkerWorker extends SwingWorker<Void, Void> {

    private ArrayBlockingQueue<ResultDTO> output;
    private ArrayBlockingQueue<Integer> input;
    Connection conn;
    public void setInputQueue(ArrayBlockingQueue<Integer> in) {
        input = in;
    }
    public void setOutputQueue(ArrayBlockingQueue<ResultDTO> out) {
        output = out;
    }
    int id, res, mark, answer;
    @Override
    protected Void doInBackground() throws Exception {
        prep();
        while(true){
            id = input.take();
            if (id == ReaderWorker.SIG_STOP) {
                sendStop();
                return null;
            }

            res = getResult(id);

            Marker.setResult(res);

            TaskInfo buf = TaskInfoDataConstructor.getFromSeries(id);

            mark = Marker.getSolutionMark(buf.getSet(), buf.getDec(), buf.getPosition());
            answer = Marker.getAnswer(buf.getDec(), buf.getPosition());

            sendResult(id,res,mark,answer);
        }
    }
    private int getResult(int id){
        final String prep = "select result from public.results\n" + "where id = ?";
        int res = 0;
        PreparedStatement ps;
        ResultSet rs;
        try{
            ps = conn.prepareStatement(prep);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            rs.next();
            res = rs.getInt(1);
        }
        catch (SQLException e) {
            System.err.println("Marker got an exception!:" + e.toString());
            //throw new RuntimeException(e);
        }
        return res;
    }
    private void prep(){
        Properties dbProp = TestAuthorizationParamsStorage.getAuthorization();
        try {
            conn = DriverManager.getConnection(TestAuthorizationParamsStorage.dbConnectTestParams(), dbProp);
        } catch (SQLException e) {
            System.err.println("marker got an SQL connection Error!");
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void done() {
        closeConnection();
        super.done();
    }

    private void closeConnection(){
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("Reader worker could not close result set");
        }
        catch (Throwable e) {
            System.err.println("Reader worker encountered a problem with JDBC driver upon closing connection");
        }
    }

    private void sendResult(int tID, int res, int mark, int answer) {
        ResultDTO toSend = new ResultDTO();

        toSend.taskID = tID;
        toSend.taskResult = res;
        toSend.mark = mark;
        toSend.taskEstimate = answer;

        output.add(toSend);
    }
    private void sendStop(){
        ResultDTO toSend = new ResultDTO();

        toSend.taskID = ReaderWorker.SIG_STOP;
        toSend.taskResult = ReaderWorker.SIG_STOP;
        toSend.mark = ReaderWorker.SIG_STOP;
        toSend.taskEstimate = ReaderWorker.SIG_STOP;

        output.add(toSend);
    }

}
