import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    final ChessGame game;
    ServerSocket serverSocket = null;
    boolean hasBlack = false;

    GameServer() {
        game = new ChessGame();
    }

    public void initServer() {
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(2333);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void serverBegin() {
        new Thread(() -> {
            synchronized (game) {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        // System.out.println("Get Request");
                        new Thread(() -> processMsg(socket)).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void processMsg(Socket socket) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ChessMsg msg;
            try {
                msg = (ChessMsg) objectInputStream.readObject();
                if(msg.type != MsgType.UPDATE)
                    System.out.printf("type:%s x:%d y:%d chess:%s\r\n",msg.type.toString(),msg.x,msg.y,msg.chess.toString());
                switch (msg.type) {
                    case JOIN -> {
                        if(!hasBlack){
                            hasBlack = true;
                            objectOutputStream.writeObject(new ChessMsg(MsgType.SETCHESS,-1,-1,Chess.BLACK));
                            System.out.println("Set Black");
                        }else{
                            objectOutputStream.writeObject(new ChessMsg(MsgType.SETCHESS,-1,-1,Chess.WHITE));
                            System.out.println("Set White");
                        }
                    }
                    case UPDATE -> {
                        objectOutputStream.writeObject(new ChessMsg(MsgType.UPDATE,-1,-1,Chess.EMPTY));
                    }
                    case PLACE -> {
                        game.placeChess(msg.x, msg.y, msg.chess);
                        objectOutputStream.writeObject(new ChessMsg(MsgType.PLACED, msg.x, msg.y, msg.chess));
                        objectOutputStream.writeObject(new ChessMsg(MsgType.TURN, -1, -1, game.getNextTurn()));
                    }
                    case REQUIRE_REWIND -> {
                        objectOutputStream.writeObject(new ChessMsg(MsgType.REQUIRE_REWIND, 0, 0, msg.chess));
                    }
                    case ACCEPT_REWIND -> {
                        game.rewind();
                        objectOutputStream.writeObject(new ChessMsg(MsgType.ACCEPT_REWIND, -1, -1, game.getNextTurn()));
                        objectOutputStream.writeObject(new ChessMsg(MsgType.TURN, -1, -1, game.getNextTurn()));
                    }
                    case REJECT_REWIND -> {
                        objectOutputStream.writeObject(new ChessMsg(MsgType.REJECT_REWIND, -1, -1, game.getNextTurn()));
                        objectOutputStream.writeObject(new ChessMsg(MsgType.TURN, -1, -1, game.getNextTurn()));
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
