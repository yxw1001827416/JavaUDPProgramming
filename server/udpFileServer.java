import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;


public class udpFileServer {
    private String filename;
    private int clientPort = 16666;
    private InetSocketAddress clientIp = null;
    private DatagramSocket socket = null;
    private DatagramPacket datagramPacket = null;
    private byte[] successMark = "success data mark".getBytes();
    private byte[] overMark = "over mark".getBytes();

    public udpFileServer() {
        try {
            clientPort = 16666;
            clientIp = new InetSocketAddress(InetAddress.getByName("localhost"), clientPort);
            socket = new DatagramSocket(16667, InetAddress.getByName("localhost"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) {
//        System.out.println("UDP FILE SERVER START...");
//        udpFileServer udpFileServer = new udpFileServer();
//        udpFileServer.runServer();
//    }

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

    public void runServer() {
        try {
            byte[] buf = new byte[1024];
            datagramPacket = new DatagramPacket(buf, 0, 1024, clientIp);
            socket.receive(datagramPacket);
            String msg = new String(buf, 0, datagramPacket.getLength());
            System.out.println("客服端ip:" + datagramPacket.getAddress() + " port:" + datagramPacket.getPort() + " -》 " + msg);
            msg = msg.substring(12);
            for (int i = msg.length() - 1; i >= 0; i--) {
                if (msg.charAt(i) == '.') {
                    filename = msg.substring(0, i) + "(1)" + msg.substring(i);
                    break;
                }
            }
            System.out.println("新文件名:" + filename);
            datagramPacket.setData(("server: can transport file").getBytes());
            socket.send(datagramPacket);

            File file = new File(filename);
            if (file.exists()) {
                file.delete();
                System.out.println("文件已存在，删除后新创建");
            }
            int reciveCount = 0;
            int readSize = 0;
            datagramPacket = new DatagramPacket(buf, 0, 1024);
            socket.receive(datagramPacket);
            System.out.println("reciveCount:" + reciveCount++);
            File f = new File(msg);
            System.out.println(f.getName());
            FileOutputStream fileOutputStream = new FileOutputStream(f.getName());
            while ((readSize = datagramPacket.getLength()) != 0) {
                msg = new String(buf, 0, readSize);
                System.out.println(msg);
                if (check(overMark, buf)) {
                    System.out.println("over mark");
                    break;
                }
                fileOutputStream.write(buf, 0, readSize);
                fileOutputStream.flush();
                datagramPacket.setData(successMark, 0, successMark.length);
                System.out.println("successMark" + successMark);
                socket.send(datagramPacket);
                datagramPacket.setData(buf, 0, buf.length);
                socket.receive(datagramPacket);
                System.out.println("reciveCount:" + reciveCount++);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }


    }

}
