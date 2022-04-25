import java.io.*;
import java.net.*;
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
            if (!GetFileName.nameSet.isEmpty()) {
                // Get name at nameSet
                String nameOfFile = GetFileName.nameSet.iterator().next();
                GetFileName.nameSet.remove(nameOfFile);
                GetFileName.nameIgnored.add(nameOfFile);

                // Use UDP transfer name
                UDPTransferName udpTransferName = new UDPTransferName(nameOfFile, "add", serverPort, datagramPacket, socket);
                udpTransferName.transfer();

                // Use UDP transfer file
                UDPTransferFile udpTransferFile = new UDPTransferFile(nameOfFile, serverPort, datagramPacket, socket, successMark, overMark);
                udpTransferFile.transfer();
            }
        }
    }
}

class GetFileName implements Runnable {
    // How to Implement set in Java
    // https://javahungry.blogspot.com/2013/08/how-sets-are-implemented-internally-in.html
    public static HashSet<String> nameSet = new HashSet<String>();

    public static ArrayList<String> nameIgnored = new ArrayList<String>();

    @Override
    public void run() {
        nameIgnored.add("GetFileName.class");
        nameIgnored.add("TransferFile.class");
        nameIgnored.add("TransferName.class");
        nameIgnored.add("client.class");
        nameIgnored.add(".DS_Store");
        nameIgnored.add("SendFile.class");
        nameIgnored.add("TransferFile.java");
        nameIgnored.add("TransferName.java");
        nameIgnored.add("client.java");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            File curDir = new File(".");
            getAllFiles(curDir);
        }
    }

    // https://stackoverflow.com/questions/15482423/how-to-list-the-files-in-current-directory/15482517
    private static void getAllFiles(File curDir) {
        File[] filesList = curDir.listFiles();
        for (File f : filesList) {
            if (f.isDirectory())
                if (!nameIgnored.contains(f.getName())) {
                    nameSet.add(f.getName());
                }
            if (f.isFile()) {
                if (!nameIgnored.contains(f.getName())) {
                    nameSet.add(f.getName());
                }
            }
        }
    }
}