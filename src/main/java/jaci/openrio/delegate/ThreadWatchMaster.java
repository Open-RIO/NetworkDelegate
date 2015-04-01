package jaci.openrio.delegate;

import java.net.ServerSocket;
import java.net.Socket;

public class ThreadWatchMaster extends Thread {

    ServerSocket socket;
    DelegateServer server;

    public ThreadWatchMaster(ServerSocket master, DelegateServer server) {
        this.socket = master;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket client = socket.accept();
                ThreadServeMaster serv = new ThreadServeMaster(client, server);
                serv.start();
            }
        } catch (Exception e) {}
    }

}
