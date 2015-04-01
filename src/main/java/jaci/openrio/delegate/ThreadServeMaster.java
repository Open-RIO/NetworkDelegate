package jaci.openrio.delegate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * The Thread Responsible for Serving a Client connected to the Main Thread.
 * This includes reading and writing to the socket concurrently to serve the Client
 * with their response
 *
 * @author Jaci
 */
public class ThreadServeMaster extends Thread {

    Socket client;
    DelegateServer server;

    public ThreadServeMaster(Socket client, DelegateServer server) {
        this.client = client;
        this.server = server;
        this.setName("Delegate|Master:Server Thread");
    }

    @Override
    public void run() {
        try {
            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while (true) {
                String response = server.respondMaster(reader.readLine());
                output.writeBytes(response + "\n");
            }
        } catch (Exception e) {
        }
    }

}
