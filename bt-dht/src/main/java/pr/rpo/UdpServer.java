package pr.rpo;

import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpServer {
    private DatagramSocket ds;

    public void init() {
        try {
            ds = new DatagramSocket(9300);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void listen() {}

    public void send() {}
}
