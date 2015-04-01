package jaci.openrio.delegate;

public class ClientID {

    int targetPort;
    String uuid, delegateID;

    public ClientID(int targetPort, String uuid, String delegateID) {
        this.targetPort = targetPort;
        this.uuid = uuid;
        this.delegateID = delegateID;
    }

}
