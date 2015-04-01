package jaci.openrio.delegate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ThreadServeMaster extends Thread {

    Socket client;
    DelegateServer server;

    public ThreadServeMaster(Socket client, DelegateServer server) {
        this.client = client;
        this.server = server;
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
