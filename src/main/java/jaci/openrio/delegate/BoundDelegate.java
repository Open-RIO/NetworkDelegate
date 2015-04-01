package jaci.openrio.delegate;

import java.net.Socket;
import java.util.Vector;

/**
 * The container for a Delegated Connection. These are provided by the {@link jaci.openrio.delegate.DelegateServer} and serve
 * to contain the connections of each delegate. Once the clients are connected here, they are ready to be used as regular sockets
 * by the user.
 *
 * @author Jaci
 */
public class BoundDelegate {

    String delegateID;
    Vector<Socket> sockets;

    protected BoundDelegate(String delegateID) {
        this.delegateID = delegateID;
        this.sockets = new Vector<>();
    }

    /**
     * Retrieve the Delegate ID for this Delegate. This is the unique identifier for the Delegate
     * that clients will use to connect to this object.
     */
    public String getDelegateID() {
        return delegateID;
    }

    /**
     * Bind a new Client to this delegate. This is after client authorization occurs and when the
     * client socket is ready to be handled by the user of this delegate
     */
    public void bindClient(Socket socket) {
        sockets.add(socket);
    }

    /**
     * Remove a client from the delegate. This usually occurs when the Client is disconnected from
     * the delegate
     */
    public void removeClient(Socket socket) {
        sockets.remove(socket);
    }

    /**
     * Return a Vector of every socket used by this delegate. Each socket should be dealt with individually
     */
    public Vector<Socket> getConnectedSockets() {
        return (Vector<Socket>) sockets.clone();
    }

}
