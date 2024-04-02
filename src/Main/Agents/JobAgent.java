package Main.Agents;

import Main.DataObjects.RCPContract;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;
import java.util.Arrays;

public class JobAgent extends Agent {
    int jobNumber = 1;
    ArrayList<String> followerNames;
    ArrayList<AID> followerAddresses;

    RCPContract myContract = new RCPContract();


    @Override
    public void setup(){

        getParameters();
        System.out.println(jobNumber);
        setServices();

    }

    @Override
    public void takeDown(){

    }

    private void setServices(){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("job");
        sd.setName("job" + jobNumber);
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
        ArrayList<String> s2 = new ArrayList<String>(Arrays.asList(s1));
    }
    private void reconfigureFromString(String str){
        String[] s1 = str.split(";");
        String[] s2 =  s1[1].split(",");
        ArrayList<String> s3 = new ArrayList<String> (Arrays.asList(s1));
    }
}
