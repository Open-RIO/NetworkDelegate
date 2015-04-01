package jaci.openrio.delegate;

import java.net.ServerSocket;

/**
 * The thread responsible for handling new connections to a Slave socket. These connections
 * are handed off to the {@link jaci.openrio.delegate.DelegateServer} to be processed and, in turn,
 * are sent to the appropriate {@link jaci.openrio.delegate.BoundDelegate} instance to be handled.
 *
 * @author Jaci
 */
public class ThreadWatchSlave extends Thread {

    ServerSocket socket;
    DelegateServer server;

    public ThreadWatchSlave(ServerSocket slave, DelegateServer server) {
        this.server = server;
        this.socket = slave;
        this.setName("Delegate|Slave:Watcher Thread");
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
