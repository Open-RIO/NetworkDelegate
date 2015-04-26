package jaci.openrio.delegate;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    ConnectionCallback callback;

    Security.Password password;

    protected BoundDelegate(String delegateID) {
        this.delegateID = delegateID;
        this.sockets = new Vector<>();
    }

    public void callback(ConnectionCallback callback) {
        this.callback = callback;
    }

    /**
     * Retrieve the Delegate ID for this Delegate. This is the unique identifier for the Delegate
     * that clients will use to connect to this object.
     */
    public String getDelegateID() {
        return delegateID;
    }

    /**
     * Called before a Delegate can be bound and verified. This is where passwords are checked
     */
    public boolean prebind(Socket socket, OutputStream out) {
        if (password != null) {
            try {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                out.write("VERIFY\n".getBytes());
                int blength = in.readInt();
                byte[] barray = new byte[blength];
                in.read(barray);
                if (password.matches(barray))
                    return true;
                else return false;
            } catch (IOException e) {
                return false;
            }
        } else
            return true;
    }

    /**
     * Bind a new Client to this delegate. This is after client authorization occurs and when the
     * client socket is ready to be handled by the user of this delegate
     */
    public void bindClient(Socket socket) {
        sockets.add(socket);
        if (callback != null)
            callback.onClientConnect(socket, this);
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

    /**
     * Set a password for the Delegate to require to establish a connection. Keep in mind the password must be hashed
     * before being sent over the network to the Delegate. This is to ensure network viewers don't see your password.
     *
     * NOTE: This is a very, very basic form of password encryption. Do not trust this in large projects, this is for
     * simple projects that need a way to confirm the connection is the one it wants. It's more of an Identification
     * system, ensuring the user at the other end is correct, rather than a password-secure lock and key. Similarly,
     * this method is vulnerable to 'reflection'. You shouldn't be using Java for things where security is of the utmost
     * importance.
     */
    public void setPassword(String password, Security.HashType algorithm) {
        this.password = new Security.Password(password, algorithm);
    }

    /**
     * Shortcut to the {@link #setPassword(String, Security.HashType)} method, using SHA-256 as the algorithm.
     */
    public void setPassword(String password) {
        setPassword(password, Security.HashType.SHA256);
    }

    /**
     * An interface used for when a Client connects to a BoundDelegate.
     *
     * @author Jaci
     */
    public static interface ConnectionCallback {

        /**
         * Called when a Client connects to a BoundDelegate. This allows for easy detection of
         * connections to a delegate.
         *
         * @param clientSocket The socket of the Client
         * @param delegate The Delegate this callback is triggered on.
         */
        public void onClientConnect(Socket clientSocket, BoundDelegate delegate);

    }

}
