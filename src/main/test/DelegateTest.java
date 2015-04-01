import jaci.openrio.delegate.DelegateClient;
import jaci.openrio.delegate.DelegateServer;

import java.io.IOException;

public class DelegateTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        DelegateServer server = new DelegateServer(5000, 5001, 5002, 5003, 5004, 5005);
        server.requestDelegate("TestDelegate1");
        server.requestDelegate("TestDelegate2");
        server.requestDelegate("TestDelegate3");
        server.requestDelegate("TestDelegate4");
        server.requestDelegate("TestDelegate5");
        server.requestDelegate("TestDelegate6");
        server.requestDelegate("TestDelegate7");
        server.requestDelegate("TestDelegate8");
        server.requestDelegate("TestDelegate9");
        server.launchDelegate();

        Thread.sleep(2000);

        DelegateClient client = new DelegateClient("localhost", 5000, "TestDelegate1");
        client.connect();
        client = new DelegateClient("localhost", 5000, "TestDelegate1");
        client.connect();
        client = new DelegateClient("localhost", 5000, "TestDelegate2");
        client.connect();
        client = new DelegateClient("localhost", 5000, "TestDelegate3");
        client.connect();
        client = new DelegateClient("localhost", 5000, "TestDelegate4");
        client.connect();
        client = new DelegateClient("localhost", 5000, "TestDelegate5");
        client.connect();
        client = new DelegateClient("localhost", 5000, "TestDelegate6");
        client.connect();
        client = new DelegateClient("localhost", 5000, "TestDelegate7");
        client.connect();
        client = new DelegateClient("localhost", 5000, "TestDelegate8");
        client.connect();
        client = new DelegateClient("localhost", 5000, "TestDelegate9");
        client.connect();
    }

}
