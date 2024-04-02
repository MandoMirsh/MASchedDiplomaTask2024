package Main.Agents;

import Main.DataObjects.RCPContract;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;

public class ResourceAgent extends Agent {
    private
    ArrayList<RCPContract> contracts = new ArrayList<>();
    ArrayList<Integer> contractMarks = new ArrayList<>();
    ArrayList<AID> resources = new ArrayList<>();
    int timer, tickPeriod = 1000;
    int resName;
    private final int RES_TYPE_COUNT = 4;
    @Override
    public void setup() {
        setServices();
    }
    Behaviour getNewContracts = new CyclicBehaviour() {
        @Override
        public void action() {

        }
    };
    Behaviour findResources = new OneShotBehaviour() {
        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("res");
            template.addServices(sd);
            try{
                DFAgentDescription[] result = DFService.search (myAgent, template);
                if (result.length < RES_TYPE_COUNT) {
                    myAgent.addBehaviour(findResources);
                }
                else{
                    for (DFAgentDescription x:result) {
                        resources.add(x.getName());
                    }
                }
            }
            catch(FIPAException fe) {
                fe.printStackTrace();
            }
        }
    };
    Behaviour renewAllMarks = new OneShotBehaviour() {
        @Override
        public void action() {

        }
    };
    Behaviour timer1 = new TickerBehaviour(this, tickPeriod) {
        @Override
        protected void onTick() {
            timer--;
            if (timer == 0) {
                myAgent.addBehaviour(vote);
            }
        }
    };
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
