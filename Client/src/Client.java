import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    Socket socket;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    ClientType type;
    Chess chess = Chess.EMPTY;
    MainWindow window;
    boolean gameStarted = false;

    public Client(MainWindow window){
        this.window = window;
    }

    private void sendMsg(ChessMsg msg){
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        try {
            System.out.printf("[Send] Type:%s, x:%d, y:%d, Chess:%s\r\n",msg.type.toString(), msg.x, msg.y, msg.chess.toString());
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Join(){
        try {
            socket = new Socket("127.0.0.1",2333);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMsg(new ChessMsg(MsgType.JOIN,-1,-1,Chess.EMPTY));
        startUpdate();
    }
    private void startUpdate(){
        new Thread(() -> {
            while(true)
            {
                update();
            }
        }).start();
    }
    public void setReady(boolean ready){
        sendMsg(new ChessMsg(MsgType.READY,ready?1:0,0,Chess.fromInt(type.toInt())));
    }
    public void place(int x, int y){
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(new ChessMsg(MsgType.PLACE,x,y,chess));
            objectOutputStream.flush();
            System.out.println("Send place");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void update(){
        if(in == null){
            try {
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            processMsg((ChessMsg) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void processMsg(ChessMsg msg)
    {
        System.out.printf("[Receive] Type:%s x:%d y:%d Chess:%s\r\n",msg.type.toString(),msg.x,msg.y,msg.chess.toString());
        switch (msg.type){
            case SET_CLIENT -> {
                type = ClientType.fromInt(msg.chess.toInt());
                window.setTitle(type.toString());
            }
            case SET_CHESS -> {
                if(chess == Chess.EMPTY){
                    chess = msg.chess;
                    window.setTitle(chess.toString());
                }
            }
            case START -> {
                window.board.game_start = true;
            }
            case PLACED -> {
                window.board.addStep(msg.x,msg.y,msg.chess);
            }
            case ACCEPT_REWIND -> {
                System.out.println("Rewind accepted");
            }
            case REJECT_REWIND -> {
                System.out.println("Rewind rejected");
            }
        }
    }
}
