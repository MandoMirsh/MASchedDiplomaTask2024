package Main.Agents;

import Main.DataObjects.MASolverContractDetails;
import Main.DataObjects.RCPContract;
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

import static java.time.LocalDateTime.now;

public class ResourceAgent extends Agent {
    private
    ArrayList<MASolverContractDetails> contractDetails = new ArrayList<>();
    ArrayList<Integer> contractMarks = new ArrayList<>();
    ArrayList<RCPContract> contractShortList = new ArrayList<>();
    int bestContractPointer = -1, bestContractValue = -4000;
    int contractPointer = 0;
    boolean anyContractReceived = false;
    final static int TESTS_ENABLED = 1, TESTS_DISABLED = 0;
    final static int  NO_PROGRAM = 0, JUST_TELL_PARAMS = 1, FIND_OTHER_RESOURCES = 2,
                        GET_FIRST_CONTRACT = 3, GET_ALL_CONTRACTS = 4,
             ALWAYS_SEND_ACCEPT = 5, ALWAYS_SEND_REJECT = 6,
            TEST_CONTRACT_LIST_FORMATION = 7,
            CHECK_SATISFACTION_COMPUTATION = 8, CHECK_VOTE_SEND_RECEIVE = 9,
            CHECK_FIRST_CONTRACT_APPLICATION = 10, CHECK_CONTRACT_POSSIBILITY_AFTER_VOTE_OUTCOME = 11,
            TEST_FULL_RUN = 12;
    int testMode = TESTS_ENABLED, testProgram = TEST_FULL_RUN;
    ArrayList<AID> resources = new ArrayList<>();
    int timer, tickPeriod = 1000;
    int resName, resVolume, resTypeCount;
    int satisfaction = 0, contractConflictPoint = 0, possibleFraudTimes = 0;
    int acceptedContracts = 0;
    ArrayList<Integer> unsharedResources = new ArrayList<>();
    private final int RES_TYPE_COUNT = 4;
    MessageTemplate proposal = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
    MessageTemplate resInform;
    int resourceMessagePointer;
    ArrayList<Integer> votes = new ArrayList<>();
    String messageToResources = "";
    @Override
    public void setup() {
        getStartingParams();
        setServices();
        if (testMode == TESTS_ENABLED) {
            System.out.println(this.getName() + ": resource #" + resName + "; res Volume: " + resVolume
                    + "; resources total: "+ resTypeCount);
        }
        if ((testMode != TESTS_ENABLED) || (testProgram != JUST_TELL_PARAMS)) {
            resInform = MessageTemplate.MatchSender(getAID());
            this.addBehaviour(findResources);
        }
    }
    private void getStartingParams(){
        Object[] args = getArguments();
        resTypeCount = Integer.parseInt((String)args[0]);
        resName = Integer.parseInt((String) args[1]);
        resVolume = Integer.parseInt((String) args[2]);
    }
    private void setServices(){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("res");
        sd.setName("res" + resName);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    Behaviour findResources = new CyclicBehaviour() {
        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("res");
            template.addServices(sd);
            try{
                DFAgentDescription[] result = DFService.search (myAgent, template);
                if (result.length == RES_TYPE_COUNT) {
                    for (DFAgentDescription x:result) {
                        resInform = MessageTemplate.or(resInform,MessageTemplate.MatchSender(x.getName()));
                        resources.add(x.getName());
                    }
                    resInform = MessageTemplate.and(resInform, MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                    Collections.sort(resources);
                    if(testMode == TESTS_ENABLED) {
                        System.out.println("resource #" + resName + ": found all other resources. ResourceLIst is:"
                                + resources);
                    }

                    if ((testMode == TESTS_ENABLED) && (testProgram == FIND_OTHER_RESOURCES)){
                        myAgent.addBehaviour(sayTestsFinished);
                    }
                    else{
                        myAgent.addBehaviour(waitForFirstContract);
                    }
                    myAgent.removeBehaviour(findResources);
                }
            }
            catch(FIPAException fe) {
                fe.printStackTrace();
            }
        }
    };
    Behaviour sayTestsFinished = new OneShotBehaviour() {
        @Override
        public void action() {
            System.out.println("resource #"+ resName + ": finished all the tests scheduled.");
        }
    };
    Behaviour waitForFirstContract = new TickerBehaviour(this, tickPeriod / 2) {
        @Override
        protected void onTick() {
            MASolverContractDetails newContractDetails = checkNewContract();
            if (newContractDetails.getContract() != null) {
                anyContractReceived = true;
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Res #" + resName + ": got new contract from job #"
                            + newContractDetails.getContract().getJobName());
                }
                if ((testMode == TESTS_ENABLED) &&
                        ((testProgram == GET_FIRST_CONTRACT) || (testProgram == GET_ALL_CONTRACTS))){
                    System.out.println("Res #" + resName + ": contract details are: "
                            + newContractDetails.getContract().toString());
                    if (testProgram == GET_FIRST_CONTRACT){
                        myAgent.addBehaviour(sayTestsFinished);
                        myAgent.removeBehaviour(waitForFirstContract);
                    }
                }
                else {
                    if ((testMode == TESTS_ENABLED) &&
                            ((testProgram == ALWAYS_SEND_ACCEPT)||(testProgram == ALWAYS_SEND_REJECT))){
                        if (testProgram == ALWAYS_SEND_ACCEPT){
                            System.out.println("Res #" + resName
                                    + ": accepted contract according to the test scenario.");
                            sendAccept(newContractDetails.getContacts());
                        }
                        else{
                            System.out.println("Res #" + resName
                                    + ": rejected contract according to the test scenario.");
                            sendReject(newContractDetails.getContract().getStart(),newContractDetails.getContacts());
                        }
                    }
                    else {
                        if (contractIsPossible(newContractDetails.getContract())){
                            if (testMode == TESTS_ENABLED) {
                                System.out.println("Res #" + resName + ": contract #"
                                        + newContractDetails.getContract().getJobName() + " is possible right now");
                            }
                            addContractDetails(newContractDetails);
                        }
                        else {
                            if (testMode == TESTS_ENABLED) {
                                System.out.println("Res #" + resName + ": contract #"
                                        + newContractDetails.getContract().getJobName() + " is not possible right now");
                            }
                            sendReject(contractConflictPoint,newContractDetails.getContacts());
                        }
                        addTimer1();
                        //myAgent.addBehaviour(waitForAdditionalContracts);
                        additionalContractWaiting();
                        myAgent.removeBehaviour(waitForFirstContract);
                    }
                }
            }
        }
    };

    Behaviour waitForAdditionalContracts = new CyclicBehaviour() {
        @Override
        public void action() {
            MASolverContractDetails newContractDetails = checkNewContract();
            if (newContractDetails.getContract() != null) {
                anyContractReceived = true;
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Res #" + resName + ": got new contract from job #"
                            + newContractDetails.getContract().getJobName());
                }
                if (contractIsPossible(newContractDetails.getContract())){
                    if (testMode == TESTS_ENABLED) {
                        System.out.println("Res #" + resName + ": contract #"
                                + newContractDetails.getContract().getJobName() + " is possible right now");
                    }
                    addContractDetails(newContractDetails);
                }
                else{
                    sendReject(contractConflictPoint,newContractDetails.getContacts());
                }
                timer++;
            }
        }
    };

    private void additionalContractWaiting(){
        waitForAdditionalContracts = new TickerBehaviour(this, tickPeriod / 10 ) {
            @Override
            protected void onTick() {
                MASolverContractDetails newContractDetails = checkNewContract();
                if (newContractDetails.getContract() != null) {
                    if (testMode == TESTS_ENABLED) {
                        System.out.println("Res #" + resName + ": got new contract from job #"
                                + newContractDetails.getContract().getJobName());
                    }
                    if (contractIsPossible(newContractDetails.getContract())){
                        if (testMode == TESTS_ENABLED) {
                            System.out.println("Res #" + resName + ": contract #"
                                    + newContractDetails.getContract().getJobName() + " is possible right now");
                        }
                        addContractDetails(newContractDetails);
                    }
                    else{
                        sendReject(contractConflictPoint,newContractDetails.getContacts());
                    }
                    timer++;
                }
            }
        };
        addBehaviour(waitForAdditionalContracts);
    }

    MASolverContractDetails checkNewContract(){
        MASolverContractDetails ret = new MASolverContractDetails();
        ret.setContract(null);
        ret.setContacts(null);
        ACLMessage mes = receive(proposal);
        if (mes != null) {
            ret.setContract(getContractFromString(mes.getContent()));
            ret.setContacts(mes.getSender());
        }

        return ret;
    }

    RCPContract getContractFromString(String contractString) {
        RCPContract ret = new RCPContract();
        String [] s2 = contractString.split(",");
        ret.setJobName(s2[0]);
        ret.setStart(Integer.parseInt(s2[1]));
        ret.setLongevity(Integer.parseInt(s2[2]));
        ret.setResNeed(Integer.parseInt(s2[3]));
        return ret;
    }
    boolean contractIsPossible(RCPContract contract) {
        boolean ret = true;
        if (unsharedResources.isEmpty()) return ret;
        int contractStart = contract.getStart();
        //int contractFinish = contractStart + contract.getLongevity() - 1;
        int scheduleFinish = unsharedResources.size();
        int intersectionLen = scheduleFinish - contractStart;
        if (contractStart > scheduleFinish){
            return ret;
        }
        for ( int i = 0; i < intersectionLen; i++){
            if (unsharedResources.get(contractStart + i) < contract.getResNeed()) {
                contractConflictPoint = contractStart + i;
                ret = false;
            }
        }

        return ret;
    }

    void sendReject(int lastNotMatching, AID receiver){
        ACLMessage msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
        msg.setContent(((Integer)lastNotMatching).toString());
        msg.addReceiver(receiver);
        send(msg);
    }
    void addContractDetails(MASolverContractDetails newContractDetails){
        if (contractIsNew(newContractDetails)) {
            contractDetails.add(newContractDetails);
        }
        else{

            int place =  searchContractPlaceByContractor(Integer.parseInt(
                    newContractDetails.getContract().getJobName()));
            if (contractDetails.get(place).getContract().getStart() < newContractDetails.getContract().getStart()) {
                contractDetails.set(place,newContractDetails);
            }
        }
    }

    boolean contractIsNew(MASolverContractDetails contractRecord) {
        return searchContractPlaceByContractor(Integer.parseInt(contractRecord.getContract().getJobName())) == -1;
    }

    Behaviour timer1;

    private void addTimer1(){
        timer1 = new WakerBehaviour(this,tickPeriod * 2L) {
            @Override
            protected void onWake() {
                super.onWake();
                addTimer2();
            }
        };
        addBehaviour(timer1);
    }
    Behaviour timer2;
    private void addTimer2(){
        timer2 = new TickerBehaviour(this, tickPeriod / 2) {
            @Override
            protected void onTick() {

                if (timer == 0) {
                    System.out.println("Round starts");
                    searchForBestContract();
                    myAgent.removeBehaviour(waitForAdditionalContracts);
                    myAgent.removeBehaviour(timer2);
                }
                else {
                    timer--;
                }
            }
        };
        addBehaviour(timer2);
    }

    private void searchForBestContract(){
        addBehaviour(markContracts);
        contractPointer = 0;
        bestContractValue = -4000;
    }


    Behaviour markContracts = new CyclicBehaviour() {
        @Override
        public void action() {
            if (contractPointer == contractDetails.size()) {
                if (contractDetails.isEmpty()){
                    addTimer3();
                    myAgent.addBehaviour(waitForFirstContract);
                }
                else{
                    myAgent.addBehaviour(voteStart);
                }
                myAgent.removeBehaviour(markContracts);
            }
            else{
                if ((testMode == TESTS_ENABLED) && ((testProgram == TEST_CONTRACT_LIST_FORMATION)
                        || (testProgram == CHECK_SATISFACTION_COMPUTATION))) {
                    if (testProgram == TEST_CONTRACT_LIST_FORMATION) {
                        System.out.println("Res #" + resName + ": contract in list: "
                                + contractDetails.get(contractPointer).getContract().toString());
                    }
                    else {
                        System.out.println("Res #" + resName + ": contract from "
                                + contractDetails.get(contractPointer).getContract().getJobName() +" got marked as: "
                                + contractSatisfaction(contractDetails.get(contractPointer).getContract()));
                    }
                }
                else {
                    int currentContractCheckedSatisfaction =
                            contractSatisfaction(contractDetails.get(contractPointer).getContract());
                    if (currentContractCheckedSatisfaction > bestContractValue) {
                        bestContractPointer = contractPointer;
                        bestContractValue = currentContractCheckedSatisfaction;
                    }
                }
                contractPointer++;
            }

        }
    };
    int contractSatisfaction(RCPContract contract) {
        int ret = 0;
        int contractFinish = contract.getStart() + contract.getLongevity() - 1;
        int scheduleFinish = unsharedResources.size();
        if (scheduleFinish < contractFinish) {
            ret += (scheduleFinish - contractFinish - 1) * resVolume;
        }
        ret += contract.getLongevity() * contract.getResNeed();
        return ret;
    }

    Behaviour voteStart = new OneShotBehaviour() {
        @Override
        public void action() {
            String myVote = contractDetails.get(bestContractPointer).getContract().getJobName();
            sendToResources(myVote);
            receiveVotesFromResources();
        }
    };
    void sendToResources(String message){
        messageToResources = message;
        resourceMessagePointer = 0;
        addBehaviour(sendToAllResources);
    }
    Behaviour sendToAllResources = new CyclicBehaviour() {
        @Override
        public void action() {
            if (resourceMessagePointer == resources.size()) {
                myAgent.removeBehaviour(sendToAllResources);
            }
            else {
                sendInform(resources.get(resourceMessagePointer++),messageToResources);
            }
        }
    };
    void sendInform(AID sendTo, String message) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(message);
        msg.addReceiver(sendTo);
        send(msg);
    }
    void receiveVotesFromResources(){
        votes.clear();
        addBehaviour(getResourceVotes);
    }
    Behaviour getResourceVotes = new CyclicBehaviour() {
        @Override
        public void action() {
            if (votes.size() == resources.size()) {
                myAgent.addBehaviour(decideFirstRound);
                myAgent.removeBehaviour(getResourceVotes);
                //System.out.println("Res #" + resName + ": got all votes" + now());
            }
            else{
                String anotherMessage = receiveResourceOpinion();
                if (!anotherMessage.isEmpty()){
                    votes.add(Integer.parseInt(anotherMessage));
                }
            }
        }
    };
    String receiveResourceOpinion(){
        StringBuilder ret = new StringBuilder();
        ACLMessage mes = receive(resInform);
        if (mes != null) {
            ret.append(mes.getContent());
        }
        return ret.toString();
    }

    Behaviour decideFirstRound =  new OneShotBehaviour(){
        @Override
        public void action(){
            if ((testMode == TESTS_ENABLED) && (testProgram == CHECK_VOTE_SEND_RECEIVE)){
                System.out.println("Res #" + resName + ": got these vote results: " + votes + "by " + now());
            }
            else{
                MASolverContractDetails winnerContract = getContractByContractor(getWinnerContractor());
                sendAccept(winnerContract.getContacts());
                applyContract(winnerContract.getContract());
                acceptedContracts++;
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Res #" + resName + ": accepted " + acceptedContracts + " contracts already.");
                    if (acceptedContracts % 10 == 0){
                        System.out.println("Res # " + resName + ": current schedule: " + unsharedResources);
                    }
                }
                if ((testMode == TESTS_ENABLED) && (testProgram == CHECK_FIRST_CONTRACT_APPLICATION)){
                    System.out.println("Res #" + resName + ": applied chosen contract. Now satisfaction is "
                            + satisfaction + " schedule length = " + unsharedResources.size()+
                            " and schedule's free resource quantity is: " + unsharedResources);
                }
                else {
                    checkCurrentContractPossibilities();
                }
            }
        }
    };
    MASolverContractDetails getContractByContractor(int contractor) {
        int contractPlace = searchContractPlaceByContractor(contractor);
        if (contractPlace == -1) return null;
        return contractDetails.remove(contractPlace);
    }
    int searchContractPlaceByContractor(int contractor){
        int ret = -1;
        int i = 0;
        while (i < contractDetails.size()){
            if (Integer.parseInt(contractDetails.get(i).getContract().getJobName()) == contractor)
                ret = i;
            i++;
        }
        return ret;
    }
    int getWinnerContractor(){
        Collections.sort(votes);
        int i = 1;
        int currentBest = votes.get(0), current = currentBest, bestVotes = 1, currentVotes = 1;
        while ( i < votes.size()){
            if (votes.get(i) == current) {
                if (++currentVotes>bestVotes){
                    currentBest = current;
                    bestVotes = currentVotes;
                }
            }
            else {
                current = votes.get(i);
                currentVotes = 1;
            }
            i++;
        }

        if (bestVotes < (resTypeCount / 2)){
            possibleFraudTimes++;
        }
        return currentBest;
    }
    void sendAccept(AID receiver) {
        ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        msg.addReceiver(receiver);
        send(msg);
    }
    void applyContract (RCPContract contract) {
        int contractStart = contract.getStart();
        int contractLongevity = contract.getLongevity();
        int contractFinish = contractStart + contractLongevity - 1;
        int scheduleFinish = unsharedResources.size();
        int scheduleTimeAddition = contractLongevity - scheduleFinish + contractStart;

        satisfaction += contractSatisfaction(contract);

        for ( int i = 0; i < scheduleTimeAddition; i++){
            unsharedResources.add(resVolume);
        }

        for ( int i = 0; i < contractLongevity; i++){
            int subtractFrom = unsharedResources.get(contractStart + i);
            int changeTo = subtractFrom - contract.getResNeed();
            unsharedResources.set(contractStart + i,changeTo);
        }

    }
    private void checkCurrentContractPossibilities(){
        contractPointer = 0;
        addBehaviour(renewPossibilities);
    }
    Behaviour renewPossibilities = new CyclicBehaviour() {
        @Override
        public void action() {
            if (contractPointer == contractDetails.size()){
                if ((testMode == TESTS_ENABLED) && (testProgram == CHECK_CONTRACT_POSSIBILITY_AFTER_VOTE_OUTCOME)){
                    System.out.println("Res #" + resName +": finished reviewing contracts");
                }
                else{
                    myAgent.addBehaviour(waitForFirstContract);
                }
                addTimer3();
                myAgent.removeBehaviour(renewPossibilities);
            }
            else {
                if ((testMode == TESTS_ENABLED) && (testProgram == CHECK_CONTRACT_POSSIBILITY_AFTER_VOTE_OUTCOME)){
                    if (contractIsPossible(contractDetails.get(contractPointer).getContract())){
                        System.out.println("Res #" + resName + ": contract from "
                                + contractDetails.get(contractPointer).getContract().getJobName()
                                + " is still possible");
                    }
                    else {
                        System.out.println("Res #" + resName + ": contract from "
                                + contractDetails.get(contractPointer).getContract().getJobName()
                                + " is not possible now");
                    }
                }
                else{
                    if (!contractIsPossible(contractDetails.get(contractPointer).getContract())){
                        removeUnsuitableContractDetails(contractPointer--);
                    }
                }
                contractPointer++;
            }
        }
    };
    Behaviour timer3;
    private void addTimer3(){
        anyContractReceived = false;
        timer3 = new WakerBehaviour(this, tickPeriod) {
            @Override
            protected void onWake() {
                super.onWake();
                if (!anyContractReceived) {
                    myAgent.removeBehaviour(waitForFirstContract);
                    searchForBestContract();
                }
                myAgent.removeBehaviour(timer3);
            }
        };
        addBehaviour(timer3);
    }
    private void removeUnsuitableContractDetails(int pointer) {
        MASolverContractDetails unsuitable = contractDetails.remove(pointer);
        sendReject(contractConflictPoint, unsuitable.getContacts());
    }
    private void reconfigureFromString(String params){

    }
}
