package Main.TestMains;

import Main.TestAuthorizationParamsStorage;
import Main.models.TaskModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Properties;

public class InitAgentTests {
    static ObjectMapper objectMapper = new ObjectMapper();
    static String getTaskFromDBString = "select task from public.tasks\n" +
            "where id = ?";
    static Connection conn;
    public static void main(String[] args) {

        //jobParamsCreationTest();
        initAgentFullTest();
        //getJSONfromDBTest();
        //mockTest();
    }
    private static void mockTest(){
        jade.Boot.main( new String[]{"-gui","something:Main.Agents.MockAgent(120101,1)"});
        //"something:Main.Agents.MockAgent(90101,1)"
        //"something:Main.Agents.MockAgent(60101,1)"
    }

    private static void getJSONfromDBTest(){
        setDBConnectionInfo();
        System.out.println(getProblemFromDB(30101));
    }

    static String getProblemFromDB ( int problemId) {
        String ret = "";
        try {
            PreparedStatement ps = conn.prepareStatement(getTaskFromDBString);
            ps.setInt(1,problemId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }
    static public void setDBConnectionInfo(){
        Properties dbProp = TestAuthorizationParamsStorage.getAuthorization();
        try {
            conn = DriverManager.getConnection(TestAuthorizationParamsStorage.dbConnectTestParams(), dbProp);
        } catch (SQLException e) {
            System.err.println(ZonedDateTime.now().toString() + ": " +
                    "encountered a problem on start of connection: " + e.toString());
        }
    }
    private static void initAgentFullTest() {
        jade.Boot.main( new String[]{"-gui","-agents","Init:Main.Agents.InitAgent(30101,2)"});
    }

    private static void jobParamsRetrieval() {

    }
    private static void jobParamsCreationTest() {
        String taskJSON = "{\"timeNeed\":8,\"successors\":[6,11,15],\"resourceNeeds\":[4,0,0,0]}";
        String taskJSON0 = "{\"timeNeed\":0,\"successors\":[2,3,4],\"resourceNeeds\":[0,0,0,0]}";
        try {
            TaskModel task = objectMapper.readValue(taskJSON0,TaskModel.class);
            System.out.println(makeJobParams(task));
            testJobParamsRetrieval(makeJobParams(task));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


    }
    private static String makeJobParams (TaskModel task){
        StringBuilder ret = new StringBuilder();
        ArrayList<Integer> resources = task.getResourceNeeds(), followers = task.getSuccessors();
        String sep1 = ",", sep2 = ";";
        ret.append(task.getTimeNeed()).append(sep2);
        for (int i = 0; i < resources.size()-1; i++){
            ret.append(resources.get(i)).append(sep1);
        }
        ret.append(resources.get(resources.size()-1)).append(sep2);
        for (int i = 0; i < followers.size()-1; i++) {
            ret.append(followers.get(i)).append(sep1);
        }
        ret.append(followers.get(followers.size()-1));


        return ret.toString();
    }
    private static void testJobParamsRetrieval(String params){
        int timeNeed;
        ArrayList<Integer> resVolumes = new ArrayList<>();
        ArrayList<Integer> followers = new ArrayList<>();

        String [] s1 = ((String)params).split(";");
        timeNeed = Integer.parseInt(s1[0]);
        String [] s2 = s1[1].split(",");
        for ( int i = 0; i < s2.length; i++) {
            resVolumes.add(Integer.parseInt(s2[i]));
        }
        s2 = s1[2].split(",");
        for ( int i = 0; i < s2.length; i++) {
            followers.add(Integer.parseInt(s2[i]));
        }

        System.out.println("job parameters: time: " + timeNeed + "; resources needed: " + resVolumes + "; followers: "+ followers );
    }

}
