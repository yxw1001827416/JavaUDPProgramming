import java.io.*;
import java.net.*;
import java.util.Arrays;

public class server {
    public static void main(String args[]) throws IOException {

        DatagramPacket datagramPacket;
        byte[] successMark = "success data mark".getBytes();
        byte[] overMark = "over mark".getBytes();

        DatagramSocket socket = new DatagramSocket(16667, InetAddress.getByName("localhost"));

        while (true) {
            try {
                // UDP obtain name
                byte[] buf = new byte[1024];
                datagramPacket = new DatagramPacket(buf, 0, 1024);
                socket.receive(datagramPacket);
                String msg = new String(buf, 0, datagramPacket.getLength());
                String[] arrOfStr = msg.split(",");
                System.out.println(Arrays.toString(arrOfStr));
                String nameOfFile = arrOfStr[0];
                String flagOfFile = arrOfStr[1];

                // UDP obtain file
                if (flagOfFile.equals("add")) {
                    int reciveCount = 0;
                    int readSize = 0;
                    datagramPacket = new DatagramPacket(buf, 0, 1024);
                    socket.receive(datagramPacket);
                    FileOutputStream fileOutputStream = new FileOutputStream(nameOfFile);
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
                } else if (flagOfFile.equals("remove")) {
                    // https://www.runoob.com/java/file-delete.html
                    try {
                        System.out.println("remove " + nameOfFile);
                        File file = new File(nameOfFile);
                        if (file.delete()) {
                            System.out.println(file.getName() + " 文件已被删除！");
                        } else {
                            System.out.println("文件删除失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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