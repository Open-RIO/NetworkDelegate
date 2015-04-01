package jaci.openrio.delegate;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * The Thread responsible for handling connection of new Clients to the Master Socket. When
 * a new Client connects, a new Thread is launched in turn, that is responsible for handling
 * that connection to the Master Socket, {@link jaci.openrio.delegate.ThreadServeMaster}
 *
 * @author Jaci
 */
public class ThreadWatchMaster extends Thread {

    ServerSocket socket;
    DelegateServer server;

    public ThreadWatchMaster(ServerSocket master, DelegateServer server) {
        this.socket = master;
        this.server = server;
        this.setName("Delegate|Master:Watcher Thread");
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
