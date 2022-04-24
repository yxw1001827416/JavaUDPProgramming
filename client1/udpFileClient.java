import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class udpFileClient {
    private String fileName;
    private int serverPort = 16667;
    private InetAddress serverIp = null; //localhost
    private DatagramSocket socket = null;  //16666
    private DatagramPacket datagramPacket = null;
    private byte[] successMark = "success data mark".getBytes();
    private byte[] overMark = "over mark".getBytes();

    public udpFileClient() {
        try {
            serverPort = 16667;
            serverIp = InetAddress.getByName("localhost");
            //创建一个本地任意UDP套接字
            socket = new DatagramSocket(16666);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean presendFile() {
        try {
            String msg = "client file:" + this.fileName;
            datagramPacket = new DatagramPacket(msg.getBytes(), msg.length(), serverIp, serverPort);
            socket.send(datagramPacket);
            datagramPacket = new DatagramPacket(new byte[1024], 1024);
            socket.receive(datagramPacket);
            msg = new String(datagramPacket.getData(),
                    0, datagramPacket.getLength());
            System.out.println("msg:" + msg);
            if (msg.equals("server: can transport file")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

    public void sendFile(String FileName) {
        fileName = FileName;
        try {
//            FileInputStream fileInputStream = new FileInputStream(fileName);
            RandomAccessFile accessFile = new RandomAccessFile(fileName, "r");
            int readSize = -1;
            int sendCount = 0, sendGroup = 0;
            byte[] buf = new byte[1024];
            byte[] receiveBuf = new byte[1024];
            String msg = null;
            datagramPacket = new DatagramPacket(buf, 1024, serverIp, serverPort);
            while ((readSize = accessFile.read(buf, 0, buf.length)) != -1) {
                datagramPacket.setData(buf, 0, readSize);
                socket.send(datagramPacket);
                sendCount++;
                while (true) {
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
        } finally {
//            socket.close();
        }
    }

//    public static void main(String[] args) {
//        long startTime = System.currentTimeMillis();
//        udpFileClient udpFileClient = new udpFileClient();
//        System.out.println("尝试连接服务端......");
//        if (udpFileClient.presendFile()) {
//            System.out.println("连接服务器成功,开始发送文件......");
//            udpFileClient.sendFile(udpFileClient.fileName);
//        } else {
//            System.out.println("网络不稳定,连接服务器失败....");
//        }
//    }

}

