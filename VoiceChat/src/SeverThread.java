import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class SeverThread implements Runnable{
    Socket socket;
    ArrayList<Socket> sockets = new ArrayList<>();
    String name;
    public SeverThread(Socket socket,ArrayList<Socket> sockets) {
        this.socket = socket;
        this.sockets = sockets;
    }

    public void run(){
        while (true)
            accepmessage();
    }

    public void accepmessage() {
        try {
            InputStream reader = socket.getInputStream();
            byte[] bytes = new byte[1024];
            while (reader.read(bytes)!= -1) {
                sendmessage(bytes);
                bytes = new byte[1024];
            }
        } catch (Exception e) {
            try {
                socket.close();
            } catch (IOException ioException) {
            }
            sockets.remove(socket);
        }
    }
    public void sendmessage(byte[] message){
        OutputStream write1 = null;
        for (Socket socket1 : sockets) {
            try {
                if (socket1.equals(this.socket))
                    continue;//如果是当前用户发出的声音，则跳过，否则会产生回音
                write1 = socket1.getOutputStream();
                write1.write(message);
            } catch (IOException e) {
            }
        }
    }
}