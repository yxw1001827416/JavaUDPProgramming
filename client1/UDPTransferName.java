import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDPTransferName {
    private String nameOfFile;
    private String flag;
    private int serverPort;
    private DatagramPacket datagramPacket;
    private DatagramSocket socket;

    public UDPTransferName(String nameOfFile, String flag, int serverPort, DatagramPacket datagramPacket, DatagramSocket socket) {
        this.nameOfFile = nameOfFile;
        this.flag = flag;
        this.serverPort = serverPort;
        this.datagramPacket = datagramPacket;
        this.socket = socket;
    }

    public void transfer() {
        String name = nameOfFile;
        String flag = this.flag;
        String msg = name + "," + flag;

        try {
            datagramPacket = new DatagramPacket(msg.getBytes(), msg.length(), InetAddress.getByName("localhost"), serverPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
