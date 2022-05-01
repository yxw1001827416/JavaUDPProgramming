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

        Thread.sleep(1000);

        Runnable runnableRemoveFileName = new RemoveFileName();
        Thread threadRemoveFileName = new Thread(runnableRemoveFileName);
        threadRemoveFileName.start();
    }
}

class SendFile implements Runnable {
    int serverPort = 16667;
    DatagramPacket datagramPacket = null;
    byte[] successMark = "success data mark".getBytes();
    byte[] overMark = "over mark".getBytes();
    DatagramSocket socket = new DatagramSocket();

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
                UDPTransferFile udpTransferFile = new UDPTransferFile("./clientDoc/" + nameOfFile, serverPort, datagramPacket, socket, successMark, overMark);
                udpTransferFile.transfer();
            }
        }
    }
}

class GetFileName implements Runnable {
    // How to Implement set in Java
    // https://javahungry.blogspot.com/2013/08/how-sets-are-implemented-internally-in.html
    public static HashSet<String> nameSet = new HashSet<String>();
    public static HashSet<String> copyOfNameSet = new HashSet<String>();

    public static ArrayList<String> nameIgnored = new ArrayList<String>();

    @Override
    public void run() {
        nameIgnored.add(".DS_Store");

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            File curDir = new File("./clientDoc/");
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
                    copyOfNameSet.add(f.getName());
                }
            if (f.isFile()) {
                if (!nameIgnored.contains(f.getName())) {
                    nameSet.add(f.getName());
                    copyOfNameSet.add(f.getName());
                }
            }
        }
    }
}

class RemoveFileName implements Runnable {
    public static HashSet<String> currRemoveSet = new HashSet<String>();

    @Override
    public void run() {
        int serverPort = 16667;
        DatagramPacket datagramPacket = null;
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            HashSet<String> toBeRemoved = new HashSet<String>();
            toBeRemoved.addAll(GetFileName.copyOfNameSet);
            currRemoveSet.clear();

            File curDir = new File("./clientDoc/");

            getAllFiles(curDir, currRemoveSet);

            getAllFiles(curDir, currRemoveSet);
            System.out.println("curr " + currRemoveSet);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            toBeRemoved.removeAll(currRemoveSet);
            System.out.println("to be removed " + toBeRemoved);
            if (!toBeRemoved.isEmpty()) {
                // Get name at prevRemoveSet
                String fileName = toBeRemoved.iterator().next();
                GetFileName.copyOfNameSet.remove(fileName);
                System.out.println("Remove file name " + fileName);
                // Use UDP transfer name
                UDPTransferName udpTransferName = new UDPTransferName(fileName, "remove", serverPort, datagramPacket, socket);
                udpTransferName.transfer();
            }
        }
    }

    private static void getAllFiles(File curDir, HashSet<String> fileName) {
        File[] filesList = curDir.listFiles();
        for (File f : filesList) {
            if (f.isDirectory())
                fileName.add(f.getName());
            if (f.isFile()) {
                fileName.add(f.getName());
            }
        }
    }
}