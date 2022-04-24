import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class server {
    public static void main(String args[]) throws IOException {
        // UDP
        DatagramSocket dsoc = new DatagramSocket(10008);
        // TCP
        ServerSocket server = new ServerSocket(10005);


        while (true) {
            // TCP get name of file
            Socket socket = server.accept();
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            String fileName = null;
            try (DataInputStream d = new DataInputStream(in)) {
                fileName = d.readUTF();
                Files.copy(d, Paths.get(fileName));
                System.out.println(fileName);
            }
            String nameOfFile = fileName;

            // UDP get file
            var udp = new udpFileServer();
            udp.runServer();
            udp = null;

//            // UDP
//            // https://4cnotes.blogspot.com/2011/12/java-file-transfer-using-udp.html?m=1
//            byte b[] = new byte[3072];
//            File file = new File(nameOfFile);
//            String content = "";
//
//            // Todo wrap up in while
//            DatagramPacket dp = new DatagramPacket(b, b.length);
//            dsoc.receive(dp);
//            content = content.concat(new String(dp.getData(), 0, dp.getLength()));
//
//
//            // Write file
//            // https://mkyong.com/java/how-to-write-to-file-in-java-fileoutputstream-example/
//            try (FileOutputStream fop = new FileOutputStream(file)) {
//
//                // if file doesn't exists, then create it
//                if (!file.exists()) {
//                    file.createNewFile();
//                }
//
//                // get the content in bytes
//                byte[] contentInBytes = content.getBytes();
//
//                fop.write(contentInBytes);
//                fop.flush();
//                fop.close();
//
//                System.out.println("Done");
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }
}