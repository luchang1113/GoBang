import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {
    final ChessGame game;
    ServerSocket serverSocket = null;
    Socket masterSocket = null;
    Socket slaveSocket = null;
    List<Socket> watchSockets = new ArrayList<>();
    ObjectInputStream masterIn = null;
    ObjectInputStream slaveIn = null;
    ObjectOutputStream masterOut = null;
    ObjectOutputStream slaveOut = null;
    boolean masterReady = false;
    boolean slaveReady = false;

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
                        if (masterSocket == null) {
                            masterSocket = serverSocket.accept();
                            System.out.println("Master entered");
                            new Thread(() -> {
                                while (true) {
                                    try {
                                        masterIn = new ObjectInputStream(masterSocket.getInputStream());
                                        if(masterOut == null){
                                            masterOut = new ObjectOutputStream(masterSocket.getOutputStream());
                                        }
                                        try {
                                            ChessMsg msg = (ChessMsg) masterIn.readObject();
                                            processMsg(msg);
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        } else if (slaveSocket == null) {
                            slaveSocket = serverSocket.accept();
                            System.out.println("Slave entered");
                            new Thread(() -> {
                                while (true) {
                                    try {
                                        slaveIn = new ObjectInputStream(slaveSocket.getInputStream());
                                        if(slaveOut == null){
                                            slaveOut = new ObjectOutputStream(slaveSocket.getOutputStream());
                                        }
                                        try {
                                            ChessMsg msg = (ChessMsg) slaveIn.readObject();
                                            processMsg(msg);
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        } else {
                            watchSockets.add(serverSocket.accept());
                            System.out.printf("Watcher entered, now has %d watchers\r\n", watchSockets.size());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void sendMasterMsg(ChessMsg msg) {
        if (masterSocket != null) {
            try {
                System.out.printf("[Send Master] Type:%s, x:%d, y:%d, Chess:%s\r\n",msg.type.toString(), msg.x, msg.y, msg.chess.toString());
                masterOut.writeObject(msg);
                masterOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendSlaveMsg(ChessMsg msg) {
        if (slaveSocket != null) {
            try {
                System.out.printf("[Send Slave] Type:%s, x:%d, y:%d, Chess:%s\r\n",msg.type.toString(), msg.x, msg.y, msg.chess.toString());
                slaveOut.writeObject(msg);
                slaveOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendWatcherMsg(ChessMsg msg) {
        for (Socket socket : watchSockets) {
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(msg);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAllMsg(ChessMsg msg) {
        sendMasterMsg(msg);
        sendSlaveMsg(msg);
        sendWatcherMsg(msg);
    }

    private void processMsg(ChessMsg msg) {
        System.out.printf("[Receive] Type:%s x:%d y:%d Chess:%s\r\n",msg.type.toString(),msg.x,msg.y,msg.chess.toString());
        switch (msg.type) {
            case JOIN -> {
                if(masterOut != null){
                    sendMasterMsg(new ChessMsg(MsgType.SET_CLIENT,0,0,Chess.fromInt(ClientType.MASTER.toInt())));
                }
                if(slaveOut != null){
                    sendSlaveMsg(new ChessMsg(MsgType.SET_CLIENT, 0, 0, Chess.fromInt(ClientType.SLAVE.toInt())));
                }
            }
            case READY -> {
                switch (msg.chess){
                    case BLACK -> {
                        masterReady = msg.x == 1;
                    }
                    case WHITE -> {
                        slaveReady = msg.x == 1;
                    }
                }
                if(masterReady && slaveReady){
                    sendMasterMsg(new ChessMsg(MsgType.SET_CHESS,0,0,Chess.BLACK));
                    sendSlaveMsg(new ChessMsg(MsgType.SET_CHESS,0,0,Chess.WHITE));
                    sendAllMsg(new ChessMsg(MsgType.START, 0, 0, Chess.EMPTY));
                }
            }
            case PLACE -> {
                if (game.placeChess(msg.x, msg.y, msg.chess)) {
                    sendAllMsg(new ChessMsg(MsgType.PLACED, msg.x, msg.y, msg.chess));
                    System.out.println("Placed");
                }
            }
        }
    }
}
