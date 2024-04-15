package Main.Agents;

import Main.TestAuthorizationParamsStorage;
import Main.models.ProblemModel;
import Main.models.TaskModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

import static java.lang.Math.random;
import static java.time.LocalDateTime.now;

public class InitAgent extends Agent {
    Connection conn;
    private final int STARTUP_TEST = 1, TESTS_ENABLED = 1, TESTS_DISABLED = 0,
                    DATABASE_CONNECTIVITY_TEST = 2, SINGLE_RESOURCE_CREATION_TEST = 3, ALL_RESOURCE_CREATION_TEST = 4,
                    RESOURCES_CHECK_TEST = 5, SINGLE_JOB_CREATION_TEST = 6, ALL_JOBS_CREATION_TEST = 7,
                    SOURCE_FOLLOWERS_INITIAL_LIST_TEST = 8, SOURCE_FOLLOWERS_FIND_TEST = 9,
                    SOURCE_FOLLOWERS_MESSAGE_SEND_TEST = 10,
                    NET_START_TEST = 11, TEST_FIRST_RESULT = 12, TEST_GETTING_NEXT_PROJECT = 13,
                    SINGLE_RESOURCE_RECONFIGURATION_TEST = 14, ALL_RESOURCE_RECONFIGURATION_TEST = 15,
                    SINGLE_JOB_RECONFIGURATION_TEST = 16, ALL_JOB_RECONFIGURATION_TEST = 17;
    int testMode = TESTS_ENABLED, testProgram = TEST_GETTING_NEXT_PROJECT;
    ProblemModel currentProblem;
    ObjectMapper objectMapper = new ObjectMapper();
    int howManyJobs = 30, howManyResources = 4;

    int predecessorsFinished = 0, predecessorsToWait = 0;
    int projectId = 30101, projectsLeft = 1;
    String getTaskFromDBString = "select task from public.tasks\n" +
            "where id = ?";
    String insertResultsToDB = "insert into  public.results (id, result)\n"
            + "values ( ? , ? )";
    int sourceSuccessorsPointer = 0, successorInformationPointer = 0;
    int resConfigurationPointer = 0, jobConfigurationPointer = 1;
    int currentSinkPredecessorsFinish = -1;
    int sinkPredecessorsNonActive = 0, sinkPredecessorsTotal = 0;
    String agentNameBase = "";
    int generatedAgentsCount = 0;
    static final String LIST_CHECKER_CLASS = "Main.Agents.DFListChecker";
    ArrayList<Integer> sourceSuccessors = new ArrayList<>();
    ArrayList<AID> sourceSuccessorsAddresses = new ArrayList<>();
    ArrayList<AID> resourceAgents = new ArrayList<>();
    ArrayList<AID> jobAgents = new ArrayList<>();

    @Override
    public void setup(){
        setDBConnectionInfo();
        getRunParams();
        if (testMode == TESTS_ENABLED) System.out.println(ZonedDateTime.now().toString() + ": " +
                this.getAID().toString() + "started successfully");
        if (testMode == TESTS_ENABLED) System.out.println("Params extracted: " + projectId + " " + projectsLeft + " " + howManyJobs);
        if ((testMode == TESTS_ENABLED) && (testProgram == STARTUP_TEST)) {
            this.addBehaviour(shutTheNet);
        }
        else {
            this.addBehaviour(getFirstProjectDetails);
        }

        setServices();
    }
    private void getRunParams(){
        Object[] args = getArguments();
        projectId = Integer.parseInt(args[0].toString());
        projectsLeft = Integer.parseInt(args[1].toString());
        setJobNumber();
    }
    private void setJobNumber() {
        final int THIRD_PARAM_DIGITS = 10000, K = 10;
        howManyJobs = projectId / THIRD_PARAM_DIGITS * K;
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
        sd.setName("job1");
        dfd.addServices(sd);
        sd = new ServiceDescription();
        sd.setType("job");
        sd.setName("job" + (howManyJobs + 2));
        dfd.addServices(sd);
        sd = new ServiceDescription();
        sd.setType("controller");
        sd.setName("project controller");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    Behaviour getFirstProjectDetails = new OneShotBehaviour() {
        @Override
        public void action() {
            currentProblem = getProblem(projectId);
            if (testMode == TESTS_ENABLED) {
                System.out.println("Got  project " + projectId);
            }
            if (currentProblem.getTasks()!= null) {
                TaskModel source = currentProblem.getTasks().get(0);
                sourceSuccessors = source.getSuccessors();
            }

            if (testMode == TESTS_ENABLED) {
                System.out.println("Projects Left :" + projectsLeft);
            }
            if ((testMode == TESTS_ENABLED) &&(testProgram == DATABASE_CONNECTIVITY_TEST)) {
                if (currentProblem.getRnum() != 0) {
                    System.out.println("Database connectivity test passed");
                }
                else{
                    System.out.println("Database connectivity test failed");
                }
                myAgent.addBehaviour(shutTheNet);
            }
            else{
                myAgent.addBehaviour(resourceCreation);
            }
        }
    };


    int getNextId(int prevId) {
    int ret = 0;
    final int SECOND_PARAM_DIGITS = 100, FINAL_NUMBER_IN_DECADE = 10;
    if (prevId % SECOND_PARAM_DIGITS == FINAL_NUMBER_IN_DECADE) {
        ret = (prevId / SECOND_PARAM_DIGITS + 1) * SECOND_PARAM_DIGITS + 1;
    }
    else {
        ret = prevId + 1;
    }

    return ret;
    };
    ProblemModel getProblem(int problemId) {
        ProblemModel ret = convertJSONtoProblemModel(getProblemFromDB(problemId));
        return ret;
    }
    String getProblemFromDB ( int problemId) {
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
    ProblemModel convertJSONtoProblemModel(String JSON){
        ProblemModel ret = new ProblemModel();
        try {
            ret = objectMapper.readValue(JSON, ProblemModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    Behaviour resourceCreation = new CyclicBehaviour() {
        @Override
        public void action() {
            createResource(resConfigurationPointer++);
            if (testMode == TESTS_ENABLED) {
                System.out.println("Created resource " + resConfigurationPointer + " successfully.");
            }
            if ((testMode == TESTS_ENABLED) && (testProgram == SINGLE_RESOURCE_CREATION_TEST)){
                myAgent.addBehaviour(waitAndShutTheNet);
                myAgent.removeBehaviour(resourceCreation);
            }
            if (resConfigurationPointer == howManyResources){
                if ((testMode == TESTS_ENABLED) && (testProgram == ALL_RESOURCE_CREATION_TEST)){
                    myAgent.addBehaviour(waitAndShutTheNet);
                }
                else{
                    myAgent.addBehaviour(checkResourceRegistration);
                }
                myAgent.removeBehaviour(resourceCreation);
            }
        }
    };
    void createResource(int resId) {
        int resVolume = getResourceVolume(resId);
        ContainerController cc = getContainerController();
        try {
            AgentController ac = cc.createNewAgent(generateNextAgentNickname(),
                    "Main.Agents.ResourceAgent",
                    new Object[]{((Integer)getHowManyResources()).toString(), ((Integer)(++resId)).toString(), ((Integer)resVolume).toString()});
            ac.start();
            Thread.sleep(500);
        } catch (StaleProxyException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        //System.out.println("STUD: created resource " + ++resId);
    }
    int getHowManyResources(){
        return howManyResources;
    }
    int getResourceVolume(int resId){
        return currentProblem.getRes(++resId);
    }

    Behaviour checkResourceRegistration = new CyclicBehaviour() {
        @Override
        public void action() {
            if (getResourceCount() == howManyResources) {
                if ((testMode == TESTS_ENABLED) && (testProgram == RESOURCES_CHECK_TEST)) {
                    myAgent.addBehaviour(successfullyTested);
                }
                else {
                    myAgent.addBehaviour(jobCreation);
                }
                myAgent.removeBehaviour(checkResourceRegistration);
            }
            else {
                if (testMode == TESTS_ENABLED) {
                    System.out.println("not enough resources proceeded with registration yet");
                }
            }
        }
    };
    private int getResourceCount(){
        int ret = 4;
        //STUD:
        ret = getResources().length;
        //return their count;
        return ret;
    };
    private AID[] getResources(){
        AID[] ret = new AID[1];
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("res");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            ret = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                ret[i] = result[i].getName();
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        return ret;
    }

    Behaviour jobCreation = new CyclicBehaviour() {
        @Override
        public void action() {
            if (jobConfigurationPointer < howManyJobs + 1) {
                createJobAgent(jobConfigurationPointer);
                jobConfigurationPointer++;
                if ((testMode == TESTS_ENABLED) && (testProgram == SINGLE_JOB_CREATION_TEST)) {
                    myAgent.addBehaviour(waitAndShutTheNet);
                    myAgent.removeBehaviour(jobCreation);
                }
            }
            else {
                if ((testMode == TESTS_ENABLED) && (testProgram == ALL_JOBS_CREATION_TEST)) {
                    myAgent.addBehaviour(waitAndShutTheNet);
                }
                else {
                    myAgent.addBehaviour(getSourceSuccessors);
                }
                myAgent.removeBehaviour(jobCreation);
            }
        }
    };
    private void createJobAgent(int jobPointer){
        String jobParams = makeJobParams(getJobConfiguration(jobPointer));
        ContainerController ac = getContainerController();
        try {
                AgentController ag = ac.createNewAgent(generateNextAgentNickname(),
                        "Main.Agents.JobAgent",
                        new Object[]{((Integer)(jobPointer + 1)).toString(),jobParams});
                ag.start();
                Thread.sleep(500);
            //System.out.println(this.getAgentState().toString());
            //System.out.println(ag.getState().toString());
        } catch (StaleProxyException | InterruptedException e) {
                throw new RuntimeException(e);
        }

    }
    private String generateNextAgentNickname(){
        if (agentNameBase.isEmpty()) {
            agentNameBase = String.valueOf(random());
        }
        return agentNameBase + generatedAgentsCount++;
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
    TaskModel getJobConfiguration(int jobnum) {
        TaskModel ret = currentProblem.getTasks().get(jobnum);
        return ret;
    }
    Behaviour getSourceSuccessors = new CyclicBehaviour() {
        @Override
        public void action() {
            if ((testMode == TESTS_ENABLED) && (testProgram == SOURCE_FOLLOWERS_INITIAL_LIST_TEST)){
                System.out.println("SourceFollowers: "+  sourceSuccessors);
                myAgent.addBehaviour(successfullyTested);
            }
            if (sourceSuccessorsPointer == sourceSuccessors.size()) {
                if ((testMode == TESTS_ENABLED) && (testProgram == SOURCE_FOLLOWERS_FIND_TEST)) {
                    myAgent.addBehaviour(successfullyTested);
                }
                else{
                    myAgent.addBehaviour(startingNet);
                }
                myAgent.removeBehaviour(getSourceSuccessors);
            }
            else {
                    findNextSuccessor();
                    if ((testMode == TESTS_ENABLED) && (testProgram == SOURCE_FOLLOWERS_FIND_TEST)) {
                        System.out.println("Current found source successors addresses: " + sourceSuccessorsAddresses);
                    }
            }

        }
    };
    void findNextSuccessor(){
        AID[] ret;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("job");
        sd.setName("job" + sourceSuccessors.get(sourceSuccessorsPointer));
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0){
                sourceSuccessorsAddresses.add(result[0].getName());
                sourceSuccessorsPointer++;
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    Behaviour startingNet = new CyclicBehaviour() {
        @Override
        public void action() {
            if (successorInformationPointer < sourceSuccessorsAddresses.size()){
                sendImPredecessor(getSuccessor(successorInformationPointer));
                sendImReady(getSuccessor(successorInformationPointer),-1);
                successorInformationPointer++;
                if ((testMode == TESTS_ENABLED) && (testProgram == SOURCE_FOLLOWERS_MESSAGE_SEND_TEST)){
                    myAgent.addBehaviour(successfullyTested);
                    myAgent.removeBehaviour(startingNet);
                }
            }
            else {
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Sent activation to all source successors");
                }
                if ((testMode == TESTS_ENABLED) && (testProgram == NET_START_TEST)) {
                    myAgent.addBehaviour(successfullyTested);
                }
                else {
                    myAgent.addBehaviour(waitForSinkPredecessors);
                }
                myAgent.removeBehaviour(startingNet);
            }

        }
    };
    private void sendImPredecessor(AID successor){
        ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
        mes.setContent("predecessor 1");
        mes.addReceiver(successor);
        send(mes);
    }
    AID getSuccessor(int pointer) {
        return sourceSuccessorsAddresses.get(pointer);
    }
    private void sendImReady(AID successor, int finish) {
        ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
        mes.setContent("finished 1 " + finish);
        mes.addReceiver(successor);
        send(mes);
    }
    Behaviour waitForSinkPredecessors = new TickerBehaviour(this, 3000) {
        @Override
        public void onTick() {
            if (testMode == TESTS_ENABLED){
                System.out.println("Sink is trying to get message from predecessors");
            }
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                String[] s1 = msg.getContent().split(" ");
                if (Objects.equals(s1[0], "finished")){
                    if (testMode == TESTS_ENABLED) {
                        System.out.println("Sink: got message that job #" + s1[1]
                                + " finished.");
                    }
                    if (currentSinkPredecessorsFinish < Integer.parseInt(s1[2])){
                        currentSinkPredecessorsFinish = Integer.parseInt(s1[2]);
                        if(testMode == TESTS_ENABLED) {
                            System.out.println("Sink: new predecessors' latest finish: "
                                    + currentSinkPredecessorsFinish);
                        }
                    }
                    sinkPredecessorsNonActive--;
                    if (sinkPredecessorsNonActive == 0){
                        myAgent.addBehaviour(countDownTillGettingNextProject);
                    }
                }
                if (Objects.equals(s1[0], "predecessor")){
                    if (testMode == TESTS_ENABLED){
                        System.out.println("Sink: got new predecessor: "
                                + s1[1]);
                    }
                    if ((sinkPredecessorsTotal != 0) && (sinkPredecessorsNonActive == 0)){
                        myAgent.removeBehaviour(countDownTillGettingNextProject);
                    }
                    sinkPredecessorsNonActive++;
                    sinkPredecessorsTotal++;
                }
            }
            else{
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Sink: got no new predecessors telling");
                }
            }
        }
    };

    Behaviour countDownTillGettingNextProject = new WakerBehaviour(this, 10500) {
        @Override
        protected void onWake() {
            super.onWake();
            if (testMode == TESTS_ENABLED) {
                System.out.println("Sink: finished waiting for all the predecessors");
            }
            myAgent.addBehaviour(sayCurrentResult);
            myAgent.removeBehaviour(waitForSinkPredecessors);
        }
    };
    Behaviour sayCurrentResult =  new OneShotBehaviour() {
        @Override
        public void action() {
            System.out.println("Project #" + projectId + ": solved. Total Solution time: "
                    + (currentSinkPredecessorsFinish + 1));
            if ((testMode == TESTS_ENABLED) && (testProgram == TEST_FIRST_RESULT)) {
                myAgent.addBehaviour(waitAndShutTheNet);
            }
            else {
                myAgent.addBehaviour(getNextProject);
            }
        }
    };
    Behaviour getNextProject = new OneShotBehaviour() {
        @Override
        public void action() {
            nextProject();
            if (projectsLeft == 0) {
                if (testMode == TESTS_ENABLED){
                    System.out.println("No projects left to solve");
                }
                myAgent.addBehaviour(shutTheNet);
            }
            else {
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Got next project: " + projectId);
                }
                if ((testMode != TESTS_ENABLED)||(testProgram != TEST_GETTING_NEXT_PROJECT)){
                    myAgent.addBehaviour(netReconfigurationPreparement);
                }
                else{
                    myAgent.addBehaviour(successfullyTested);
                }
            }
        }
    };
    void nextProject(){
        projectId = getNextId(projectId);
        projectsLeft--;
    }

    Behaviour netReconfigurationPreparement = new OneShotBehaviour() {
        @Override
        public void action() {
            resConfigurationPointer = 0;
            jobConfigurationPointer = 1;
            currentSinkPredecessorsFinish = -1;
            myAgent.addBehaviour(resourceReconfiguration);
        }
    };
    Behaviour resourceReconfiguration = new CyclicBehaviour() {
        @Override
        public void action() {
            if (resConfigurationPointer == howManyResources) {
                myAgent.addBehaviour(jobReconfiguration);
                myAgent.removeBehaviour(resourceReconfiguration);
            }
            else{
                sendResourceConfiguration(resConfigurationPointer++);
            }
        }
    };
    //TODO:
    void sendResourceConfiguration(int resPointer) {

    }
    Behaviour jobReconfiguration = new CyclicBehaviour() {
        @Override
        public void action() {
            if (jobConfigurationPointer == howManyJobs + 1) {
                myAgent.addBehaviour(startingNet);
                myAgent.removeBehaviour(jobReconfiguration);
            }
            else {
                sendJobReconfiguration(jobConfigurationPointer++);
            }

        }
    };
    //TODO:
    void sendJobReconfiguration(int jobPointer) {

    }
    Behaviour waitAndShutTheNet = new WakerBehaviour(this, 4000) {
        @Override
        protected void onWake() {
            super.onWake();
            myAgent.addBehaviour(shutTheNet);
        }
    };
    Behaviour successfullyTested = new OneShotBehaviour() {
        @Override
        public void action() {
            System.out.println("All the tests were done. Now Shutting down");
            myAgent.addBehaviour(waitAndShutTheNet);
        }
    };
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
        System.out.println("Successfully shut down controllerAgent");
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
}
