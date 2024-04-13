package Main.Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class JobAgent extends Agent {
    int jobNumber = 1;
    ArrayList<Integer> followers = new ArrayList<>();
    ArrayList<Integer> successors = new ArrayList<>();
    ArrayList<Integer> resVolumes = new ArrayList<>();
    ArrayList<AID> successorsAddresses = new ArrayList<>();
    ArrayList<AID> resAddresses = new ArrayList<>();
    int successorPointer = 0, resourcePointer = 0, followersPointer = 0;
    int timeNeed, currentPredecessorsFinish = -400, currentStartConstraint, approvalCount = 0,
        predecessorsNonActive = 0, predecessorsTotal = 0;

    private final int STARTUP_TEST = 1, TESTS_ENABLED = 1, TESTS_DISABLED = 0,
            TEST_FIND_FOLLOWERS = 2, TEST_FOLLOWERS_INFORM = 3, TEST_WAIT_FOR_FOLLOWERS = 4,
            TEST_CONTRACT_BASE_MAKING = 5,TEST_SEND_CONTRACT = 6, TEST_SEND_CONTRACT_AGAIN = 7,
            TEST_SEND_ALL_CONTRACTS = 8, TEST_GET_FIRST_RESPONSE = 9, TEST_FULL_RUN = 10,
            TEST_MAKE_UNCONSTRAINED_SOLUTION = 11;
    int testMode = TESTS_ENABLED, testProgram = TEST_FULL_RUN;
    String myContractBase;
    MessageTemplate sentByResource;
    MessageTemplate resourceAccept, resourceDecline;
    @Override
    public void setup(){

        getParameters();
        if (testMode == TESTS_ENABLED) {
            System.out.println(this.getName() + ": job #" + jobNumber +"; time required: " + timeNeed +
                    "; res Volumes: " + resVolumes + "; successors: "+ followers);
        }
        setServices();
        if ((testMode == TESTS_DISABLED) || (testProgram != STARTUP_TEST)) {
            this.addBehaviour(tickingFindSuccessors);
        }
        getResources();
        if (testMode == TESTS_ENABLED) {
            //System.out.println("job #" + jobNumber + ": found resource addresses. Number of resources found: "
            //                      + resAddresses.size());
        }
        generateTemplates();
    }

    private void generateTemplates(){
        MessageTemplate accept = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);

        MessageTemplate reject = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
        MessageTemplate tmp = MessageTemplate.MatchSender(resAddresses.get(0));
        for ( AID x:resAddresses){
            tmp = MessageTemplate.or(tmp,MessageTemplate.MatchSender(x));
        }
        resourceAccept = MessageTemplate.and(accept,tmp);
        resourceDecline = MessageTemplate.and(reject,tmp);
    }



    private void setServices(){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("job");
        sd.setName("job" + jobNumber);
        System.out.println(jobNumber + ": " + sd.getName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    private void getParameters(){
        Object args[] = getArguments();
        jobNumber = Integer.parseInt((String)args[0]);
        String [] s1 = ((String)args[1]).split(";");
        timeNeed = Integer.parseInt(s1[0]);
        String [] s2 = s1[1].split(",");
        for (String s : s2) {
            resVolumes.add(Integer.parseInt(s));
        }
        s2 = s1[2].split(",");
        for (String s : s2) {
            followers.add(Integer.parseInt(s));
        }
    }
    Behaviour tickingFindSuccessors = new TickerBehaviour(this, 1000) {
        @Override
        public void onTick() {
            if (followers.isEmpty()) {//(followersPointer == followers.size())
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Job #" + jobNumber + ": found all the successors: " + successorsAddresses);
                }
                if ((testMode == TESTS_ENABLED) && (testProgram == TEST_FIND_FOLLOWERS)){
                    this.myAgent.addBehaviour(sayTestsFinished);
                }
                else{
                    this.myAgent.addBehaviour(informSuccessors);
                }
                this.myAgent.removeBehaviour(tickingFindSuccessors);
            }
            else{
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Trying to find job #" + followers.get(0));
                }
                findNextSuccessor();
            }
        }
    };
    private void findNextSuccessor(){
        final String JOB_NAME_BASE = "job";
        int jobNumber = followers.get(0);
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(JOB_NAME_BASE);
        sd.setName(JOB_NAME_BASE + jobNumber);
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length != 0) {
                if (result[0] != null) {

                    followers.remove(0);
                    successors.add(jobNumber);
                    successorsAddresses.add(result[0].getName());

                }
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    Behaviour informSuccessors = new TickerBehaviour(this, 600) {
        @Override
        public void onTick() {
            if (successorPointer == successors.size()) {
                if ((testMode == TESTS_ENABLED) && (testProgram == TEST_FOLLOWERS_INFORM)){
                    this.myAgent.addBehaviour(sayTestsFinished);
                }
                else {
                    this.myAgent.addBehaviour(waitForPredecessors);
                }
                successorPointer = 0;
                myAgent.removeBehaviour(informSuccessors);
            }
            else{
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Job #" + jobNumber + ": saying hello to job #" + successors.get(successorPointer));
                }
                sendHello(successorsAddresses.get(successorPointer++));
            }
        }
    };
    private void sendHello(AID successor){
            ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
            mes.setContent("predecessor " + jobNumber);
            mes.addReceiver(successor);
            send(mes);
    }
    private boolean getResources() {
        boolean ret = false;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("res");
        template.addServices(sd);
        try{
            DFAgentDescription[] result = DFService.search (this, template);
            if (result.length == resVolumes.size()) {
                ret = true;
                for (DFAgentDescription x:result) {
                    resAddresses.add(x.getName());
                }
                Collections.sort(resAddresses);
            }

        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }
    Behaviour waitForPredecessors = new TickerBehaviour(this, 300) {
        @Override
        public void onTick() {

            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                String[] s1 = msg.getContent().split(" ");
                if (Objects.equals(s1[0], "finished")){
                    if (testMode == TESTS_ENABLED) {
                        System.out.println("job #" + jobNumber + ": got message that job #" + s1[1]
                                + " finished.");
                    }
                    if (currentPredecessorsFinish  < Integer.parseInt(s1[2])){
                        currentPredecessorsFinish = Integer.parseInt(s1[2]);
                        if(testMode == TESTS_ENABLED) {
                            System.out.println("job #" + jobNumber + ": new predecessors' latest finish: "
                                    + currentPredecessorsFinish);
                        }
                    }
                    predecessorsNonActive--;
                    if (predecessorsNonActive == 0){
                        myAgent.addBehaviour(countDownTillContractInitialization);
                    }
                }
                if (Objects.equals(s1[0], "predecessor")){
                    if (testMode == TESTS_ENABLED){
                        System.out.println("job #" + jobNumber + ": got new predecessor: "
                                + s1[1]);
                    }
                    if ((predecessorsTotal != 0) && (predecessorsNonActive == 0)){
                        myAgent.removeBehaviour(countDownTillContractInitialization);
                    }
                    predecessorsNonActive++;
                    predecessorsTotal++;
                }
            }
            else{
                if (testMode == TESTS_ENABLED) {
                    //System.out.println("Job #" + jobNumber + ": got no new predecessors telling");
                }
            }
        }
    };

    Behaviour countDownTillContractInitialization = new WakerBehaviour(this, 2000) {
        @Override
        protected void onWake() {
            super.onWake();
            if ((testMode == TESTS_ENABLED) && (testProgram == TEST_WAIT_FOR_FOLLOWERS)){
                System.out.println("job #" + jobNumber + ": finished waiting for all the predecessors");
                myAgent.addBehaviour(sayTestsFinished);
            }
            else{
                myAgent.addBehaviour(makeNewContractBase);
            }
            currentStartConstraint = currentPredecessorsFinish;
            myAgent.removeBehaviour(waitForPredecessors);

        }
    };
    Behaviour makeNewContractBase = new OneShotBehaviour() {
        @Override
        public void action() {
            if ((testMode == TESTS_ENABLED) && (testProgram == TEST_MAKE_UNCONSTRAINED_SOLUTION)){
                System.out.println("job #" + jobNumber + ": skipped to informing next jobs");
                myAgent.addBehaviour(sendMyFinish);
            }
            else{
                myContractBase = makeContractBase();
                if (testMode == TESTS_ENABLED) {
                    System.out.println(myContractBase);
                }
                if ((testMode == TESTS_ENABLED) && (testProgram == TEST_CONTRACT_BASE_MAKING)) {
                    myAgent.addBehaviour(sayTestsFinished);
                }
                else{
                    myAgent.addBehaviour(sendContracts);
                }
            }
        }
    };

    private String makeContractBase(){
        return jobNumber + "," + (currentStartConstraint + 1) + "," + timeNeed + ",";
    }

    Behaviour sendContracts = new CyclicBehaviour() {
        @Override
        public void action() {
            if (resourcePointer == resVolumes.size()){
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Job #" + jobNumber + ":sent all current contracts.");
                }
                if ((testMode == TESTS_ENABLED) && (testProgram == TEST_SEND_ALL_CONTRACTS)){
                    myAgent.addBehaviour(sayTestsFinished);
                }
                else{
                    myAgent.addBehaviour(waitForResponses);
                }
                myAgent.removeBehaviour(sendContracts);
            }
            else {
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Job #" + jobNumber + ": send contract to the Resource #"
                            + (resourcePointer + 1));
                }
                sendNextContract();

                if ((testMode == TESTS_ENABLED) &&
                        ((testProgram == TEST_SEND_CONTRACT)|| (testProgram == TEST_SEND_CONTRACT_AGAIN))){
                    if (testProgram == TEST_SEND_CONTRACT_AGAIN){
                        resourcePointer--;
                        sendNextContract();
                    }
                    myAgent.addBehaviour(sayTestsFinished);
                    myAgent.removeBehaviour(sendContracts);
                }
            }
        }
    };
    private void sendNextContract(){
        sendContract(resVolumes.get(resourcePointer),resAddresses.get(resourcePointer++));
    }
    private void sendContract(int resNeeded, AID contacts){
        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
        msg.setContent(myContractBase + resNeeded);
        msg.addReceiver(contacts);
        send(msg);
    }


    Behaviour waitForResponses = new CyclicBehaviour() {
        @Override
        public void action() {
            if (approvalCount == resVolumes.size()){
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Job #" + jobNumber + ": got all accepts needed. Now starting to send ");
                }
                myAgent.addBehaviour(sendMyFinish);
                myAgent.removeBehaviour(waitForResponses);
            }
            else {
                int startCorrection = tryToFindReject();
                if (startCorrection > currentStartConstraint){
                    if (testMode == TESTS_ENABLED){
                    System.out.println("Job #" + jobNumber + " got contract reject."
                            + " Contract start is to be after t = " + startCorrection);
                    }
                    if ((testMode == TESTS_ENABLED) && (testProgram == TEST_GET_FIRST_RESPONSE)){
                        myAgent.addBehaviour(sayTestsFinished);
                    }
                    else{
                        System.out.println("Job #" + jobNumber + ": restarting contract sending procedure.");
                        currentStartConstraint = startCorrection;
                        myAgent.addBehaviour(makeNewContractBase);

                    }
                    resourcePointer = 0;
                    myAgent.removeBehaviour(waitForResponses);
                }
                if(foundApprove()){
                    approvalCount++;
                    if (testMode == TESTS_ENABLED){
                        System.out.println("got contract approval. Total approval count: " + approvalCount);
                    }
                    if ((testMode == TESTS_ENABLED) && (testProgram == TEST_GET_FIRST_RESPONSE)){
                        myAgent.addBehaviour(sayTestsFinished);
                        myAgent.removeBehaviour(waitForResponses);
                    }
                }
            }
        }
    };
    int tryToFindReject(){
        int ret = -1;
        ACLMessage msg = receive(resourceDecline);
        if (msg != null) {
            ret = Integer.parseInt(msg.getContent());
        }
        return ret;
    }
    boolean foundApprove(){
        boolean ret = false;
        ACLMessage msg = receive(resourceAccept);
        if (msg!= null) {
            ret = true;
        }
        return ret;
    }

    Behaviour sendMyFinish = new CyclicBehaviour() {
        @Override
        public void action() {
            if (successorPointer == successorsAddresses.size()){
                if (testMode == TESTS_ENABLED){
                    System.out.println("job #" + jobNumber + ": finished informing successors about active contract");
                }

                myAgent.addBehaviour(waitState);

                myAgent.removeBehaviour(sendMyFinish);
            }
            else{
                sendImReady(successorsAddresses.get(successorPointer++),currentPredecessorsFinish + timeNeed);
            }

        }
    };

    private void sendImReady(AID successor, int finish) {
        ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
        mes.setContent("finished" + " " + jobNumber + " " + finish);
        mes.addReceiver(successor);
        send(mes);
    }
    Behaviour waitState = new CyclicBehaviour() {
        @Override
        public void action() {

        }
    };

    private void reconfigureFromString(String params){
        String [] s1 = params.split(";");
        timeNeed = Integer.parseInt(s1[0]);
        String [] s2 = s1[1].split(",");
        for (String s : s2) {
            resVolumes.add(Integer.parseInt(s));
        }
        s2 = s1[2].split(",");
        for (String s : s2) {
            followers.add(Integer.parseInt(s));
        }

    }
    Behaviour sayTestsFinished = new OneShotBehaviour() {
        @Override
        public void action() {
            System.out.println("All tests planned for job #" + jobNumber + " finished!");
        }
    };

    @Override
    public void takeDown(){

    }
}
