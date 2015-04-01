package jaci.openrio.delegate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.UUID;
import java.util.Vector;

/**
 * The DelegateServer will serve clients using a Master Socket. The Client requests a Delegate ID, and then
 * this class is responsible for assigning a Port and a Unique ID (UUID) to the Client. This is, ideally, used
 * for situations where there may be more connections required than ports available. For example, there may be 10
 * different connections that need to be open on the server, but on 5 ports are forwarded. This server is responsible
 * for making sure each port gets 2 connections each, while still allowing for the Developer to treat the sockets as if
 * they were individual connections.
 *
 * The lifecycle of the DelegateServer is as follows:
 * -Client Connects to Master Socket
 * -Client sends Socket request (REQUEST <delegate_id>)
 * -Master Socket sends back a port to connect to (slave port) and a UUID for the Client
 * -Client disconnects from Master Socket
 * -Client connects to slave port provided
 * -Client sends UUID
 * -Server sends back response and sends Socket to the appropriate Delegate {@link BoundDelegate}
 *
 * Keep in mind that it is the CLIENT's responsibility to disconnect, as they may send a REQUEST more than
 * once to save on Network traffic or CPU time for multiple connections with a single endpoint
 *
 * @see jaci.openrio.delegate.DelegateClient
 * @see jaci.openrio.delegate.BoundDelegate
 *
 * @author Jaci
 */
public class DelegateServer {

    int masterPort;
    int[] slavePorts;
    ServerSocket masterSocket;
    ServerSocket[] slaveSockets;

    int lastPortIndex;

    volatile Vector<BoundDelegate> delegates;
    volatile Vector<ClientID> clientHash;

    /**
     * Create a new Delegate Server. This will not launch the server socket, and only prepares it.
     * @param master The 'master' socket to connect to
     * @param slaves The slave sockets as a list. Every socket must be defined
     */
    public DelegateServer(int master, int... slaves) {
        this.masterPort = master;
        this.slavePorts = slaves;
        delegates = new Vector<>();
        clientHash = new Vector<>();
    }

    /**
     * An alias to the constructor of this class, but will instead use every port in the range
     * of start-end inclusive
     * @param master The 'master' socket to connect to
     * @param start The start of the slave socket range (inclusive)
     * @param end The end of the slave socket range (inclusive)
     */
    public static DelegateServer createRange(int master, int start, int end) {
        int[] array = new int[end + 1 - start];
        for (int i = 0; i < array.length; i++)
            array[i] = start + i;
        return new DelegateServer(master, array);
    }

    /**
     * Launch the Delegate and all the sockets. This method will block until each socket is opened,
     * and will return immediately after. Processing of the socket data is done in new Threads.
     */
    public void launchDelegate() throws IOException {
        masterSocket = new ServerSocket(masterPort);
        slaveSockets = new ServerSocket[slavePorts.length];
        for (int i = 0; i < slavePorts.length; i++)
            slaveSockets[i] = new ServerSocket(slavePorts[i]);

        ThreadWatchMaster watcher = new ThreadWatchMaster(masterSocket, this);
        watcher.start();

        for (ServerSocket socket : slaveSockets) {
            ThreadWatchSlave watchSlave = new ThreadWatchSlave(socket, this);
            watchSlave.start();
        }
    }

    /**
     * Request the next available port for use.
     */
    public int requestPort() {
        if (lastPortIndex == slavePorts.length) lastPortIndex = 0;
        return slavePorts[lastPortIndex++];
    }

    /**
     * Request a Delegate Instance with the given Unique ID. This will create a new Delegate if the
     * ID is not in use, or will return an existing one if already registered.
     * @see jaci.openrio.delegate.BoundDelegate
     * @param id The unique ID for the delegate. This should be unique to your connection. A good example
     *           would be 'com.yourname.yourproject-ConnectionID', to avoid conflicts with other potential
     *           delegates. Make sure this value is constant, as it is what the Client uses to connect.
     */
    public BoundDelegate requestDelegate(String id) {
        id = id.replace(" ", "-");
        BoundDelegate delegate = getDelegate(id);
        if (delegate != null)
            return delegate;

        delegate = new BoundDelegate(id);
        delegates.add(delegate);
        return delegate;
    }

    /**
     * Retrieve a registered delegate instance, or null if none exists
     * @param id The Unique ID of the Delegate.
     */
    public BoundDelegate getDelegate(String id) {
        id = id.replace(" ", "-");
        for (BoundDelegate d : delegates)
            if(d.getDelegateID().equals(id))
                return d;
        return null;
    }

    /**
     * Respond to a message received on the Master Thread. This is responsible for returning
     * the correct response to send to the client connected on this Thread
     * @param message The message received
     */
    public String respondMaster(String message) {
        if (message.startsWith("REQUEST")) {
            String[] args = message.split(" ");
            if (args.length != 2)
                return "-1 Invalid Argument Count (should be: <delegate_id>)";

            String id = args[1];
            BoundDelegate delegate = getDelegate(id);
            if (delegate == null)
                return "-1 Delegate Does Not Exist.";

            String token = UUID.randomUUID().toString();
            int port = requestPort();
            ClientID cid = new ClientID(port, token, id);
            clientHash.add(cid);
            return port + " " + token;
        }
        return "-1 Invalid Request.";
    }

    /**
     * Observe input from a Client on a Slave Thread. This is responsible for handling the Client's
     * connection details and handling them off to the appropriate {@link jaci.openrio.delegate.BoundDelegate}
     * @param socket The socket of the client
     * @throws IOException
     */
    public void observeClient(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream output = socket.getOutputStream();
        String request = reader.readLine();
        if (request.startsWith("TUNNEL")) {
            String[] args = request.split(" ");
            String hash = args[1];
            boolean valid = false;
            for (ClientID cid : clientHash) {
                if (cid.uuid.equals(hash)) {
                    if (cid.targetPort == socket.getLocalPort()) {
                        BoundDelegate delegate = getDelegate(cid.delegateID);
                        if (delegate != null) {
                            delegate.bindClient(socket);
                            output.write("SUCCESS\n".getBytes());
                        }
                    } else
                        output.write("ERROR: Wrong Port.\n".getBytes());
                    valid = true;
                }
            }
            if (!valid)
                output.write("ERROR: Invalid Client Hash.\n".getBytes());
        } else
            output.write("ERROR: Invalid Request.\n".getBytes());
    }
}
