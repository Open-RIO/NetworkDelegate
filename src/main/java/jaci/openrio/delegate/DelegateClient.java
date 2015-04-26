package jaci.openrio.delegate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * The Client-Side implementation of a Delegate Connection. Implement this on your Client
 * connection. This class is responsible for contacting the Master socket on the delegate
 * server and handling the response. Upon response by the Master Socket, the DelegateClient
 * will disconnect and connect to the port it was assigned to with the UUID it has retrieved.
 * From here on, all handling is left up to the user.
 *
 * @see jaci.openrio.delegate.DelegateServer
 *
 * @author Jaci
 */
public class DelegateClient {

    String hostname;
    int masterPort;
    String delegate;

    Socket socket;

    int allocatedPort;
    String clientUUID;

    Security.Password password;

    /**
     * Create a new Delegate Client
     * @param hostname The hostname of the Delegate Server
     * @param masterPort The 'Master Port' of the Delegate Server. This is the port
     *                   we connect on to retrieve our UUID and port assignment
     * @param delegateID The Delegate ID we wish to connect to. This is also known
     *                   as the 'Module' we want to connect to on the server
     */
    public DelegateClient(String hostname, int masterPort, String delegateID) {
        this.hostname = hostname;
        this.masterPort = masterPort;
        this.delegate = delegateID.replace(" ", "-");
    }

    /**
     * Set a password for the Delegate to send to the server if it requires a password.
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
        this.setPassword(password, Security.HashType.SHA256);
    }

    /**
     * Begin Connection to the Delegate Server. This method will handle connecting to the Master
     * Socket, retrieving a response, disconnecting fromm the Master Socket and connecting to our
     * assigned port with the UUID. This method is not concurrent, if you wish to have this method
     * run in a new Thread, you will have to do that before calling this method.
     * @throws IOException A socket connection error, or an error detected by the Delegate Server.
     */
    public void connect() throws IOException {
        socket = new Socket(hostname, masterPort);
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output.writeBytes("REQUEST " + delegate +"\n");
        String response = reader.readLine();

        String[] split = response.split(" ");
        allocatedPort = Integer.parseInt(split[0]);
        if (allocatedPort == -1)
            throw new IOException("Delegate Master Connection Error: " + response);

        clientUUID = split[1];

        socket.close();
        socket = new Socket(hostname, allocatedPort);
        output = new DataOutputStream(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        output.writeBytes("TUNNEL " + clientUUID + "\n");
        String res = reader.readLine();
        if (res.equals("VERIFY")) {
            if (password == null)
                throw new IOException("Password Required to Connect to Delegate");
            output.writeInt(password.hash.length);
            output.write(password.hash);
            res = reader.readLine();
        }
        if (!res.equals("SUCCESS"))
            throw new IOException("Delegate Slave Connection Error: " + res);
        reader = null;
    }

    /**
     * Get the connected socket
     */
    public Socket getSocket() {
        return socket;
    }

}
