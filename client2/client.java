import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class client {
    public static void main(String[] args) throws Exception {
        Runnable runnableSendPortNumber = new SendPortNumber();
        Thread threadSendPortNumber = new Thread(runnableSendPortNumber);
        threadSendPortNumber.start();

        Thread.sleep(1000);

        Runnable runnableReceiveFileSynchronization = new ReceiveFileSynchronization();
        Thread threadReceiveFileSynchronization = new Thread(runnableReceiveFileSynchronization);
        threadReceiveFileSynchronization.start();

        Thread.sleep(1000);

        Runnable runnableGetFileName = new GetFileName();
        Thread threadGetFileName = new Thread(runnableGetFileName);
        threadGetFileName.start();

        Thread.sleep(1000);

        Runnable runnableSendFile = new SendFile();
        Thread threadSendFile = new Thread(runnableSendFile);
        threadSendFile.start();

        Runnable runnableRemoveFileName = new RemoveFileName();
        Thread threadRemoveFileName = new Thread(runnableRemoveFileName);
        threadRemoveFileName.start();

        Thread.sleep(1000);

        Runnable runnableRemoveFileSynchronization = new RemoveFileSynchronization();
        Thread threadRemoveFileSynchronization = new Thread(runnableRemoveFileSynchronization);
        threadRemoveFileSynchronization.start();
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

                // Use UDP transfer currPortNumber
                String msg = String.valueOf(SendPortNumber.socket.getLocalPort());
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
            while (!toBeRemoved.isEmpty()) {
                // Get name at prevRemoveSet
                String fileName = toBeRemoved.iterator().next();
                toBeRemoved.remove(fileName);
                GetFileName.copyOfNameSet.remove(fileName);
                System.out.println("Remove file name " + fileName);
                // Use UDP transfer name
                UDPTransferName udpTransferName = new UDPTransferName(fileName, "remove", serverPort, datagramPacket, socket);
                udpTransferName.transfer();

                // Use UDP transfer currPortNumber
                String msg = String.valueOf(SendPortNumber.socket.getLocalPort());
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
    }

    private static void getAllFiles(File curDir, HashSet<String> fileName) {
        File[] filesList = curDir.listFiles();
        for (File f : filesList) {
            if (f.isDirectory())
                if (!f.getName().equals(".DS_Store")) {
                    fileName.add(f.getName());
                }
            if (f.isFile()) {
                if (!f.getName().equals(".DS_Store")) {
                    fileName.add(f.getName());
                }
            }
        }
    }
}

class SendPortNumber implements Runnable {
    int serverPort = 16669;
    DatagramPacket datagramPacket = null;
    public static DatagramSocket socket;

    static {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String msg = String.valueOf(socket.getLocalPort());
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

class ReceiveFileSynchronization implements Runnable {
    DatagramPacket datagramPacket;
    byte[] successMark = "success data mark".getBytes();
    byte[] overMark = "over mark".getBytes();
    DatagramSocket socket = SendPortNumber.socket;

    ReceiveFileSynchronization() throws SocketException, UnknownHostException {
    }

    @Override
    public void run() {
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
                    Files.move(Paths.get(nameOfFile), Paths.get("./clientDoc/" + nameOfFile), StandardCopyOption.REPLACE_EXISTING);
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

class RemoveFileSynchronization implements Runnable {
    DatagramPacket datagramPacket;
    DatagramSocket socket = new DatagramSocket(SendPortNumber.socket.getLocalPort() + 1000);

    RemoveFileSynchronization() throws SocketException {
    }


    @Override
    public void run() {
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

            if (flagOfFile.equals("remove")) {
                // https://www.runoob.com/java/file-delete.html
                try {
                    System.out.println("remove " + nameOfFile);
                    File file = new File("./clientDoc/" + nameOfFile);
                    if (file.delete()) {
                        System.out.println(file.getName() + " 文件已被删除！");
                    } else {
                        System.out.println("文件删除失败！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}