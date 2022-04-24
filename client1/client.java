import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class client {
    public static void main(String args[]) throws Exception {
        Runnable runnableGetFileName = new GetFileName();
        Thread threadGetFileName = new Thread(runnableGetFileName);
        threadGetFileName.start();

        Thread.sleep(1000);

        Runnable runnableSendFile = new SendFile();
        Thread threadSendFile = new Thread(runnableSendFile);
        threadSendFile.start();
    }
}

class SendFile implements Runnable {

    String fileName;
    int serverPort = 16667;
    DatagramPacket datagramPacket = null;
    byte[] successMark = "success data mark".getBytes();
    byte[] overMark = "over mark".getBytes();
    DatagramSocket socket = new DatagramSocket(16666);


    SendFile() throws SocketException {
    }


    @Override
    public void run() {

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!GetFileName.listOfName.isEmpty()) {
                System.out.println("This is sendFile: " + GetFileName.listOfName);
                // transfer name of file
                String nameOfFile = GetFileName.listOfName.iterator().next();
                GetFileName.listOfName.remove(nameOfFile);
                GetFileName.nameIgnored.add(nameOfFile);

                // Use UDP transfer name
                String name = nameOfFile;
                String flag = "add";
                String msg = name + " " + flag;
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

                // transfer file
                fileName = nameOfFile;
                try {
                    RandomAccessFile accessFile = new RandomAccessFile(fileName, "r");
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

class GetFileName implements Runnable {

    // How to Implement set in Java
    // https://javahungry.blogspot.com/2013/08/how-sets-are-implemented-internally-in.html
    public static HashSet<String> listOfName = new HashSet<String>();

    public static ArrayList<String> nameIgnored = new ArrayList<String>();

    @Override
    public void run() {
        nameIgnored.add("client.class");
        nameIgnored.add("SendFile.class");
        nameIgnored.add("client.java");
        nameIgnored.add("GetFileName.class");
        nameIgnored.add(".DS_Store");
        nameIgnored.add("udpFileClient.class");
        nameIgnored.add("udpFileClient.java");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            File curDir = new File(".");
            getAllFiles(curDir);
            System.out.println("This is GetFileName " + listOfName);
        }
    }

    // https://stackoverflow.com/questions/15482423/how-to-list-the-files-in-current-directory/15482517
    private static void getAllFiles(File curDir) {

        File[] filesList = curDir.listFiles();
        for (File f : filesList) {
            if (f.isDirectory())
                if (!nameIgnored.contains(f.getName())) {
                    listOfName.add(f.getName());
                }
            if (f.isFile()) {
                if (!nameIgnored.contains(f.getName())) {
                    listOfName.add(f.getName());
                }
            }
        }
    }
}