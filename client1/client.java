import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;

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
                // Get name of file
                String nameOfFile = GetFileName.listOfName.iterator().next();
                GetFileName.listOfName.remove(nameOfFile);
                GetFileName.nameIgnored.add(nameOfFile);

                // Send file name using TCP
                // https://stackoverflow.com/questions/15649972/how-do-i-send-file-name-with-file-using-sockets-in-java
                File file = new File(nameOfFile);
                Socket socket = null;
                try {
                    socket = new Socket("localhost", 10005);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedOutputStream out = null;
                try {
                    out = new BufferedOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    try (DataOutputStream d = new DataOutputStream(out)) {
                        d.writeUTF(nameOfFile);
                        Files.copy(file.toPath(), d);
                        System.out.println("name of file: ");
                        System.out.println(file.toPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Send file using UDP
                System.out.println("连接服务器成功,开始发送文件......");
                var udp = new udpFileClient();
                udp.sendFile(nameOfFile);
                System.out.println("ending");
                udp = null;
//                if (udp.presendFile()) {
//                    udp.sendFile(nameOfFile);
//                }
//                byte b[] = new byte[1024];
//                DatagramSocket dsoc = null;
//                try {
//                    dsoc = new DatagramSocket();
//                } catch (SocketException e) {
//                    e.printStackTrace();
//                }
//                FileInputStream f = null;
//                try {
//                    f = new FileInputStream(nameOfFile);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                int i = 0;
//                while (true) {
//                    try {
//                        if (!(f.available() != 0)) break;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        b[i] = (byte) f.read();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    i++;
//                }
//                try {
//                    f.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    dsoc.send(new DatagramPacket(b, i, InetAddress.getLocalHost(), 10008));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }
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