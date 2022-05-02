import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UDPTransferFile {
    private String nameOfFile;
    private int serverPort;
    private DatagramPacket datagramPacket;
    private DatagramSocket socket;
    private byte[] successMark;
    private byte[] overMark;

    public UDPTransferFile(String nameOfFile, int serverPort, DatagramPacket datagramPacket, DatagramSocket socket, byte[] successMark, byte[] overMark) {
        this.nameOfFile = nameOfFile;
        this.serverPort = serverPort;
        this.datagramPacket = datagramPacket;
        this.socket = socket;
        this.successMark = successMark;
        this.overMark = overMark;
    }

    public void transfer() {
        try {
            RandomAccessFile accessFile = new RandomAccessFile(nameOfFile, "r");
            int readSize = -1;
            int sendCount = 0, sendGroup = 0;
            byte[] buf = new byte[1024];
            byte[] receiveBuf = new byte[1024];
            datagramPacket = new DatagramPacket(buf, 1024, InetAddress.getByName("localhost"), serverPort);
            while ((readSize = accessFile.read(buf, 0, buf.length)) != -1) {
                datagramPacket.setData(buf, 0, readSize);
                socket.send(datagramPacket);
                sendCount++;
                System.out.println("sendCount");
                while (true) {
                    System.out.println("while");
                    datagramPacket.setData(receiveBuf, 0, receiveBuf.length);
                    socket.receive(datagramPacket);
                    System.out.println("receiveBuf:" + receiveBuf + " VS successMark:" + successMark);
                    System.out.println("receiveBuf String:" + new String(receiveBuf, 0, receiveBuf.length, StandardCharsets.UTF_8) + " VS successMark String:" + new String(successMark));
                    if (check(successMark, receiveBuf)) {
                        sendGroup++;
                        break;
                    } else {
                        System.out.println("resend sendGroup:" + sendGroup + 1);
                        System.out.println("resend sendCount:" + sendCount++);
                        datagramPacket.setData(buf, 0, readSize);
                        socket.send(datagramPacket);
                    }
                }
            }
            System.out.println("over mark");
            datagramPacket.setData(overMark, 0, overMark.length);
            socket.send(datagramPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean check(byte[] send, byte[] receive) {
        if (receive == null || receive.length == 0) {
            return false;
        }
        for (int i = 0; i < Math.min(send.length, receive.length); i++) {
            if (send[i] != receive[i]) {
                return false;
            }
        }
        return true;
    }
}
