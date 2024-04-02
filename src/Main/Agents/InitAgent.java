package Main.Agents;

import Main.TestAuthorizationParamsStorage;
import Main.models.ProblemModel;
import Main.models.TaskModel;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.domain.JADEAgentManagement.KillContainer;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Properties;

public class InitAgent extends Agent {
    Connection conn;
    ProblemModel currentProblem;
    int howManyJobs = 30;
    int projectId, projectsLeft;
    String getTaskFromDBString = "select task from public.tasks\n" +
            "where id = ?";
    static final String LIST_CHECKER_CLASS = "Main.Agents.DFListChecker";
    @Override
    public void setup(){
        setDBConnectionInfo();
        System.out.println(ZonedDateTime.now().toString() + ": " +
                this.getAID().toString() + "started successfully\n"
        + "Trying to add behaviour");
        this.addBehaviour(tellMeReady);
        setServices();
    }
    public void setDBConnectionInfo(){
        Properties dbProp = TestAuthorizationParamsStorage.getAuthorization();
        try {
            conn = DriverManager.getConnection(TestAuthorizationParamsStorage.dbConnectTestParams(), dbProp);
        } catch (SQLException e) {
            System.err.println(ZonedDateTime.now().toString() + ": " +
                    this.getAID().toString() +
                    "encountered a problem on start of connection: " + e.toString());
        }
    }
    private void setServices(){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("job");
        sd.setName("job0");
        dfd.addServices(sd);
        sd = new ServiceDescription();
        sd.setType("job");
        sd.setName("job" + (howManyJobs + 1));
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    Behaviour shutTheNet = new OneShotBehaviour() {
        @Override
        public void action() {
            //ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            //msg.addReceiver(this.myAgent.getAMS());
            //msg.setContent(new KillContainer());
            Codec codec = new SLCodec();
            Ontology jmo = JADEManagementOntology.getInstance();
            getContentManager().registerLanguage(codec);
            getContentManager().registerOntology(jmo);
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(getAMS());
            msg.setLanguage(codec.getName());
            msg.setOntology(jmo.getName());
            try {
                getContentManager().fillContent(msg, new Action(getAID(), new ShutdownPlatform()));
                send(msg);
            }
            catch (Exception e) {}
        }
    };
    Behaviour tellMeReady = new OneShotBehaviour() {
        @Override
        public void action() {
            System.out.println(ZonedDateTime.now().toString() + ": " +
                    this.myAgent.getAID().toString() + "started successfully\n");
                    //+ "Now trying to shut the net down");
            this.myAgent.addBehaviour(getAnyMessage);

            //this.myAgent.addBehaviour(shutTheNet);
        }
    };
    Behaviour informRecieving =  new CyclicBehaviour() {
        @Override
        public void action() {

        }
    };
    Behaviour shutMeSelf =  new OneShotBehaviour(){
        @Override
        public void action(){

        }
    };
    Behaviour getAnyMessage = new CyclicBehaviour(){
        @Override
        public void action(){
            ACLMessage msg = receive();
            if (msg!=null) {
                JOptionPane.showMessageDialog(null,"Message received: " +  msg.getContent()
                + "\nNowShuttingDown");
                this.myAgent.addBehaviour(shutTheNet);
                this.myAgent.removeBehaviour(getAnyMessage);
            }
        }
    };
    @Override
    protected void takeDown() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println(ZonedDateTime.now().toString() +": " +
                    this.getAID().toString() + "encountered a problem on shutdown: " + e.toString());
        }
        System.out.println("Trying to deregister myself");
        deregister();
        System.out.println("Successfully shutted down myself");
        super.takeDown();
    }
    private void deregister() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

    }
    private void getRunParams(){
        Object[] args = getArguments();
        projectId = Integer.parseInt(args[0].toString());
        projectsLeft = Integer.parseInt(args[1].toString());
    }
    private String makeJobParams (TaskModel task){
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
    Behaviour getNextProject = new OneShotBehaviour() {
        @Override
        public void action() {

        }
    };
}
