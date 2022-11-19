import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    Socket socket;
    BufferedOutputStream out = null;
    BufferedReader in = null;
    ClientType type = ClientType.WATCHER;
    Chess chess = Chess.EMPTY;
    GameFrame window;
    Thread updateThread;
    boolean gameStarted = false;

    public Client(GameFrame window){
        this.window = window;
    }

    private void sendMsg(ChessMsg msg){
            try {
                if(out == null)
                out = new BufferedOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        try {
            System.out.printf("[Send] Type:%s, x:%d, y:%d, Chess:%s\r\n",msg.type.toString(), msg.x, msg.y, msg.chess.toString());
            out.write((msg.toString()+"\r\n").getBytes(StandardCharsets.UTF_8));
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
        updateThread = new Thread(() -> {
            while (!socket.isClosed()) {
                update();
            }
        });
        updateThread.start();
    }
    public void setReady(boolean ready){
        sendMsg(new ChessMsg(MsgType.READY,ready?1:0,0,Chess.fromInt(type.toInt())));
    }
    public void place(int x, int y){
        sendMsg(new ChessMsg(MsgType.PLACE,x,y,chess));
    }
    public void exit(){
        sendMsg(new ChessMsg(MsgType.EXIT,0,0,Chess.fromInt(type.toInt())));
        try {
            socket.close();
            in=null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void update(){
        if(in == null){
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            processMsg(new ChessMsg(in.readLine()));
        } catch (IOException e) {
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
                if(chess == Chess.BLACK)
                    window.board.can_place = true;
                else
                    window.board.can_place = false;
                window.board.steps.clear();
                window.board.repaint();
            }
            case PLACED -> {
                window.board.addStep(msg.x,msg.y,msg.chess);
                if(msg.chess != chess){
                    window.board.can_place = true;
                }else{
                    window.board.can_place = false;
                }
            }
            case GAME_END -> {
                window.setTitle("GAME_END");
                window.board.can_place = false;
                System.out.printf("%s wins\r\n",msg.chess.toString());
                window.startBtn.setEnabled(true);
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
