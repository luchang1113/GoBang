import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Sever implements Runnable{

    ArrayList<Socket> sockets = new ArrayList<>();
    int port;
    ServerSocket socket;

    public Sever(int port) {
        this.port = port;
        try {
            System.out.println("服务器ip"+InetAddress.getLocalHost()+"\n服务器端口号"+port);
            try {
                socket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        while (true){
            try {
                Socket accept = socket.accept();
                synchronized (accept){
                    sockets.add(accept);}
                new Thread(new SeverThread(accept,sockets)).start();
            } catch (IOException e) {
            }
        }
    }
    public static void main(String[] args) {
        new Thread(new Sever(8888)).start();
    }
}

