package jaci.openrio.delegate;

import sun.security.provider.MD5;

import java.net.Socket;
import java.security.MessageDigest;
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
     * Retrieve the Delegate ID for this Delegate. This is the unique identifier
     */
    public String getDelegateID() {
        return delegateID;
    }

    public void bindClient(Socket socket) {
        sockets.add(socket);
        System.err.println(delegateID + " : " + socket.getLocalPort());
    }

    public void removeClient(Socket socket) {
        sockets.remove(socket);
    }

    public Vector<Socket> getConnectedSockets() {
        return sockets;
    }

}
