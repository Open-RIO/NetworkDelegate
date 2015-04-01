package jaci.openrio.delegate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class DelegateClient {

    String hostname;
    int masterPort;
    String delegate;

    Socket socket;

    int allocatedPort;
    String clientID;

    public DelegateClient(String hostname, int masterPort, String delegateID) {
        this.hostname = hostname;
        this.masterPort = masterPort;
        this.delegate = delegateID.replace(" ", "-");
    }

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

        clientID = split[1];

        socket.close();
        socket = new Socket(hostname, allocatedPort);
        output = new DataOutputStream(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        output.writeBytes("TUNNEL " + clientID + "\n");
        String res = reader.readLine();
        if (!res.equals("SUCCESS"))
            throw new IOException("Delegate Slave Connection Error: " + res);
        reader = null;
    }

    public Socket getSocket() {
        return socket;
    }

}
