package Main.Agents;

import Main.DataObjects.RCPContract;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.util.Comparator.*;

public class ResourceAgent extends Agent {
    private
    ArrayList<RCPContract> contracts = new ArrayList<>();
    ArrayList<Integer> contractMarks = new ArrayList<>();
    ArrayList<RCPContract> contractShortList = new ArrayList<>();
    int bestContractPointer = -1, bestContractValue = -4000;
    int contractPointer = 0;
    final static int TESTS_ENABLED = 1, TESTS_DISABLED = 0;
    final static int  NO_PROGRAM = 0,JUST_TELL_PARAMS = 1, FIND_OTHER_RESOURCES = 2,
                        GET_FIRST_CONTRACT = 3, CHECK_SATISFACTION_COMPUTATION = 4;
    int testMode = TESTS_ENABLED, testProgram = FIND_OTHER_RESOURCES;
    ArrayList<AID> resources = new ArrayList<>();
    int timer, tickPeriod = 1000;
    int resName, resVolume, resTypeCount;
    int satisfaction = 0;
    ArrayList<Integer> unsharedResources = new ArrayList<>();
    private final int RES_TYPE_COUNT = 4;
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
            RCPContract newContract = checknewContract();
            if (newContract != null) {

            }
        }
    };

    Behaviour waitForAdditionalContracts = new CyclicBehaviour() {
        @Override
        public void action() {

        }
    };

    RCPContract checknewContract(){
        RCPContract ret = null;



        return ret;
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
                myAgent.addBehaviour(vote);
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

    int contractSatisfaction(RCPContract contract) {
        int ret = 0;
        int contractFinish = contract.getStart() + contract.getLongevity() - 1;
        int scheduleFinish = unsharedResources.size();

        return ret;
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

        }

        return ret;
    }
    Behaviour vote = new OneShotBehaviour() {
        @Override
        public void action() {
        }
    };
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
}
