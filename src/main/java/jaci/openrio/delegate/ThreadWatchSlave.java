package jaci.openrio.delegate;

import java.net.ServerSocket;

public class ThreadWatchSlave extends Thread {

    ServerSocket socket;
    DelegateServer server;

    public ThreadWatchSlave(ServerSocket slave, DelegateServer server) {
        this.server = server;
        this.socket = slave;
    }

    @Override
    public void run() {
        try {
            while (true) {
                server.observeClient(socket.accept());
            }
        } catch (Exception e) {}
    }

}
