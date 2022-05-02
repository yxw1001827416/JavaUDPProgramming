import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

public class server {
    public static void main(String[] args) throws IOException {
        Runnable runnableReceivePortNumber = new ReceivePortNumber();
        Thread threadReceivePortNumber = new Thread(runnableReceivePortNumber);
        threadReceivePortNumber.start();

        DatagramPacket datagramPacket;
        byte[] successMark = "success data mark".getBytes();
        byte[] overMark = "over mark".getBytes();
        DatagramSocket socket = new DatagramSocket(16667);

        while (true) {
            try {
                // UDP obtain name
                byte[] nameBuf = new byte[1024];
                datagramPacket = new DatagramPacket(nameBuf, 0, 1024);
                socket.receive(datagramPacket);
                String msg = new String(nameBuf, 0, datagramPacket.getLength());
                String[] arrOfStr = msg.split(",");
                System.out.println(Arrays.toString(arrOfStr));
                String nameOfFile = arrOfStr[0];
                String flagOfFile = arrOfStr[1];

                // UDP obtain file
                if (flagOfFile.equals("add")) {
                    int reciveCount = 0;
                    int readSize = 0;
                    byte[] fileBuf = new byte[1024];
                    datagramPacket = new DatagramPacket(fileBuf, 0, 1024);
                    socket.receive(datagramPacket);
                    FileOutputStream fileOutputStream = new FileOutputStream(nameOfFile);
                    while ((readSize = datagramPacket.getLength()) != 0) {
                        msg = new String(fileBuf, 0, readSize);
                        System.out.println(msg);
                        if (check(overMark, fileBuf)) {
                            System.out.println("over mark");
                            break;
                        }
                        fileOutputStream.write(fileBuf, 0, readSize);
                        fileOutputStream.flush();
                        datagramPacket.setData(successMark, 0, successMark.length);
                        System.out.println("successMark" + successMark);
                        socket.send(datagramPacket);
                        datagramPacket.setData(fileBuf, 0, fileBuf.length);
                        socket.receive(datagramPacket);
                        System.out.println("reciveCount:" + reciveCount++);
                    }
                    fileOutputStream.close();
                    Files.move(Paths.get(nameOfFile), Paths.get("./serverDoc/" + nameOfFile), StandardCopyOption.REPLACE_EXISTING);

                    // UDP broadcast file
                    byte[] portBuf = new byte[1024];
                    datagramPacket = new DatagramPacket(portBuf, 0, 1024);
                    socket.receive(datagramPacket);
                    String msgPort = new String(portBuf, 0, datagramPacket.getLength());
                    int msgInt = Integer.parseInt(msgPort);
                    ArrayList<Integer> sendFileTOPortNumber = new ArrayList<Integer>(ReceivePortNumber.portNumberArray);
                    sendFileTOPortNumber.remove(Integer.valueOf(msgInt));
                    System.out.println("broadcast ");
                    for (int counter = 0; counter < sendFileTOPortNumber.size(); counter++) {
                        int serverPortNew = sendFileTOPortNumber.get(counter);
                        DatagramPacket datagramPacketNew = null;
                        byte[] successMarkNew = "success data mark".getBytes();
                        byte[] overMarkNew = "over mark".getBytes();
                        DatagramSocket socketNew = new DatagramSocket();
                        // Use UDP transfer name
                        UDPTransferName udpTransferName = new UDPTransferName(nameOfFile, "add", serverPortNew, datagramPacketNew, socketNew);
                        udpTransferName.transfer();

                        // Use UDP transfer file
                        UDPTransferFile udpTransferFile = new UDPTransferFile("./serverDoc/" + nameOfFile, serverPortNew, datagramPacketNew, socketNew, successMarkNew, overMarkNew);
                        udpTransferFile.transfer();
                    }
                } else if (flagOfFile.equals("remove")) {
                    // https://www.runoob.com/java/file-delete.html
                    try {
                        System.out.println("remove " + nameOfFile);
                        File file = new File("./serverDoc/" + nameOfFile);
                        if (file.delete()) {
                            System.out.println(file.getName() + " 文件已被删除！");
                        } else {
                            System.out.println("文件删除失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // UDP broadcast file
                    byte[] portBuf = new byte[1024];
                    datagramPacket = new DatagramPacket(portBuf, 0, 1024);
                    socket.receive(datagramPacket);
                    String msgPort = new String(portBuf, 0, datagramPacket.getLength());
                    int msgInt = Integer.parseInt(msgPort);
                    ArrayList<Integer> sendFileTOPortNumber = new ArrayList<Integer>(ReceivePortNumber.portNumberArray);
                    sendFileTOPortNumber.remove(Integer.valueOf(msgInt));
                    for (int counter = 0; counter < sendFileTOPortNumber.size(); counter++) {
                        int serverPortNew = sendFileTOPortNumber.get(counter);
                        DatagramPacket datagramPacketNew = null;
                        byte[] successMarkNew = "success data mark".getBytes();
                        byte[] overMarkNew = "over mark".getBytes();
                        DatagramSocket socketNew = new DatagramSocket();
                        System.out.println("Transfer remove name ");
                        // Use UDP transfer name
                        UDPTransferName udpTransferName = new UDPTransferName(nameOfFile, "remove", serverPortNew + 1000, datagramPacketNew, socketNew);
                        udpTransferName.transfer();
                    }
                } else if (flagOfFile.equals("modify")) {
                    System.out.println(" ");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean check(byte[] send, byte[] receive) {
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

class ReceivePortNumber implements Runnable {
    public static ArrayList<Integer> portNumberArray = new ArrayList<Integer>();
    DatagramPacket datagramPacket;
    DatagramSocket socket = new DatagramSocket(16669, InetAddress.getByName("localhost"));

    ReceivePortNumber() throws SocketException, UnknownHostException {
    }

    @Override
    public void run() {
        while (true) {
            // UDP obtain name
            byte[] buf = new byte[1024];
            datagramPacket = new DatagramPacket(buf, 0, 1024);
            try {
                socket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String msg = new String(buf, 0, datagramPacket.getLength());
            int msgInt = Integer.parseInt(msg);
            portNumberArray.add(msgInt);
            System.out.println("portNumberArray" + portNumberArray);
        }
    }
}