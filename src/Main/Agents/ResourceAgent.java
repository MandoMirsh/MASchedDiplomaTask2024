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

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.util.Comparator.*;

public class ResourceAgent extends Agent {
    private
    ArrayList<MASolverContractDetails> contractDetails = new ArrayList<>();
    ArrayList<Integer> contractMarks = new ArrayList<>();
    ArrayList<RCPContract> contractShortList = new ArrayList<>();
    int bestContractPointer = -1, bestContractValue = -4000;
    int contractPointer = 0;
    final static int TESTS_ENABLED = 1, TESTS_DISABLED = 0;
    final static int  NO_PROGRAM = 0,JUST_TELL_PARAMS = 1, FIND_OTHER_RESOURCES = 2,
                        GET_FIRST_CONTRACT = 3, GET_ALL_CONTRACTS = 4,
            CHECK_SATISFACTION_COMPUTATION = 5, ALWAYS_SEND_ACCEPT = 6;
    int testMode = TESTS_ENABLED, testProgram = GET_ALL_CONTRACTS;
    ArrayList<AID> resources = new ArrayList<>();
    int timer, tickPeriod = 1000;
    int resName, resVolume, resTypeCount;
    int satisfaction = 0, contractConflictPoint = 0;
    ArrayList<Integer> unsharedResources = new ArrayList<>();
    private final int RES_TYPE_COUNT = 4;
    MessageTemplate proposal = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
    @Override
    public void setup() {
        getStartingParams();
        setServices();
        if (testMode == TESTS_ENABLED) {
            System.out.println(this.getName() + ": resource #" + resName + "; res Volume: " + resVolume + "; resources total: "+ resTypeCount);
        }
        if ((testMode != TESTS_ENABLED) || (testProgram != JUST_TELL_PARAMS)) {
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
                        resources.add(x.getName());
                    }
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
            MASolverContractDetails newContractDetails = checknewContract();
            if (newContractDetails.getContract() != null) {
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Res #" + resName + ": got new contract from job #"
                            + newContractDetails.getContract().getJobName());
                }
                if ((testMode == TESTS_ENABLED) && ((testProgram == GET_FIRST_CONTRACT) || (testProgram == GET_ALL_CONTRACTS))){
                    System.out.println("Res #" + resName + ": contract details are: "
                            + newContractDetails.getContract().toString());
                    if (testProgram == GET_FIRST_CONTRACT){
                        myAgent.addBehaviour(sayTestsFinished);
                        myAgent.removeBehaviour(waitForFirstContract);
                    }
                }
                else {
                    if ((testMode == TESTS_ENABLED) && (testProgram == ALWAYS_SEND_ACCEPT)){
                        System.out.println("Res #" + resName + ": accepted contract according to the test scenario.");
                        sendAccept(newContractDetails.getContacts());
                    }
                    else {
                        if (contractIsPossible(newContractDetails.getContract())){
                            if (testMode == TESTS_ENABLED) {
                                System.out.println("Res #" + resName + ": contract #"
                                        + newContractDetails.getContract().getJobName() + " is possible right now");
                            }
                            contractDetails.add(newContractDetails);
                        }
                        else {
                            if (testMode == TESTS_ENABLED) {
                                System.out.println("Res #" + resName + ": contract #"
                                        + newContractDetails.getContract().getJobName() + " is not possible right now");
                            }

                        }
                    }
                    myAgent.addBehaviour(timer1);
                    myAgent.addBehaviour(waitForAdditionalContracts);
                    myAgent.removeBehaviour(waitForFirstContract);
                }
            }
        }
    };

    Behaviour waitForAdditionalContracts = new CyclicBehaviour() {
        @Override
        public void action() {
             MASolverContractDetails newContractDetails = checknewContract();
            if (newContractDetails != null) {
                if (testMode == TESTS_ENABLED) {
                    System.out.println("Res #" + resName + ": got new contract from job #"
                            + newContractDetails.getContract().getJobName());
                }
                if (contractIsPossible(newContractDetails.getContract())){
                    if (testMode == TESTS_ENABLED) {
                        System.out.println("Res #" + resName + ": contract #"
                                + newContractDetails.getContract().getJobName() + " is possible right now");
                    }
                    contractDetails.add(newContractDetails);
                }
                else{
                    sendReject(contractConflictPoint,newContractDetails.getContacts());
                }

                timer++;
            }
        }
    };
    MASolverContractDetails checknewContract(){
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

        int contractStart = contract.getStart();
        //int contractFinish = contractStart + contract.getLongevity() - 1;
        int scheduleFinish = unsharedResources.size();
        int intersectionLen = scheduleFinish - contractStart + 1;
        if (contractStart > scheduleFinish){
            return ret;
        }
        for ( int i = 0; i < intersectionLen; i++){
            if (unsharedResources.get(contractStart + i) < contract.getResNeed()) {
                return false;
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

    Behaviour renewAllMarks = new CyclicBehaviour() {
        @Override
        public void action() {

        }
    };
    Behaviour timer1 = new WakerBehaviour(this,tickPeriod * 10) {
        @Override
        protected void onWake() {
            super.onWake();
            myAgent.addBehaviour(timer2);
        }
    };
    Behaviour timer2 = new TickerBehaviour(this, tickPeriod) {
        @Override
        protected void onTick() {

            if (timer == 0) {
                myAgent.addBehaviour(markContracts);
                myAgent.removeBehaviour(waitForAdditionalContracts);
                myAgent.removeBehaviour(timer2);
            }
            else {
                timer--;
            }
        }
    };
    RCPContract getContractFromMessage(String message){
        RCPContract ret = new RCPContract();
        String[] s1 = message.split(",");
        ret.setJobName(s1[0]);
        ret.setStart(Integer.parseInt(s1[1]));
        ret.setLongevity(Integer.parseInt(s1[2]));
        ret.setResNeed(Integer.parseInt(s1[3]));
        return ret;
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
        int scheduleTimeAddition = contractLongevity - scheduleFinish + contractStart - 1;
        for ( int i = 0; i < scheduleTimeAddition; i++){
            unsharedResources.add(resVolume);
        }
        for ( int i = 0; i < contractLongevity; i++){
            int subtractFrom = unsharedResources.get(contractStart + i);
            int changeTo = subtractFrom - contract.getResNeed();
            unsharedResources.set(contractStart + i,changeTo);
        }
    }

    Behaviour markContracts = new CyclicBehaviour() {
        @Override
        public void action() {


        }
    };
    int contractSatisfaction(RCPContract contract) {
        int ret = 0;
        int contractFinish = contract.getStart() + contract.getLongevity() - 1;
        int scheduleFinish = unsharedResources.size();
        ret -= (scheduleFinish - contractFinish) * resVolume;
        ret += contract.getLongevity() * contract.getResNeed();
        return ret;
    }

    Behaviour vote = new OneShotBehaviour() {
        @Override
        public void action() {
        }
    };

}
