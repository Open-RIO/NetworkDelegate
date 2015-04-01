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

public class DelegateServer {

    int masterPort;
    int[] slavePorts;
    ServerSocket masterSocket;
    ServerSocket[] slaveSockets;

    int lastPortIndex;

    volatile Vector<BoundDelegate> delegates;
    volatile Vector<ClientID> clientHash;

    public DelegateServer(int master, int... slaves) {
        this.masterPort = master;
        this.slavePorts = slaves;
        delegates = new Vector<>();
        clientHash = new Vector<>();
    }

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

    public int requestPort() {
        if (lastPortIndex == slavePorts.length) lastPortIndex = 0;
        return slavePorts[lastPortIndex++];
    }

    public BoundDelegate requestDelegate(String id) {
        id = id.replace(" ", "-");
        BoundDelegate delegate = getDelegate(id);
        if (delegate != null)
            return delegate;

        delegate = new BoundDelegate(id);
        delegates.add(delegate);
        return delegate;
    }

    public BoundDelegate getDelegate(String id) {
        id = id.replace(" ", "-");
        for (BoundDelegate d : delegates)
            if(d.getDelegateID().equals(id))
                return d;
        return null;
    }

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
