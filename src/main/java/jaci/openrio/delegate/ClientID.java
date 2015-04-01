package jaci.openrio.delegate;

/**
 * A container class for a Client's Authorization ID and the data that is carried with it, such
 * as the Target Port assigned to the client, and the Delegate ID it wishes to connect to
 *
 * @author Jaci
 */
public class ClientID {

    int targetPort;
    String uuid, delegateID;

    public ClientID(int targetPort, String uuid, String delegateID) {
        this.targetPort = targetPort;
        this.uuid = uuid;
        this.delegateID = delegateID;
    }

}
