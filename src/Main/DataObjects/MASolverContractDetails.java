package Main.DataObjects;

import jade.core.AID;

public class MASolverContractDetails {
    public RCPContract getContract() {
        return contract;
    }

    public void setContract(RCPContract contract) {
        this.contract = contract;
    }

    public AID getContacts() {
        return contacts;
    }

    public void setContacts(AID contacts) {
        this.contacts = contacts;
    }

    RCPContract contract;
    AID contacts;
    @Override
    public String toString(){
        return "[ " + contract.toString() + ", " + contacts.toString() + " ]";
    }
}

