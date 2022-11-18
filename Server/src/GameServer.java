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
                        if (masterSocket == null) {
                            masterSocket = serverSocket.accept();
                            System.out.println("Master entered");
                            new Thread(() -> {
                                while (true) {
                                    try {
                                        ObjectInputStream objectInputStream = new ObjectInputStream(masterSocket.getInputStream());
                                        try {
                                            processMsg((ChessMsg) objectInputStream.readObject());
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
                                        ObjectInputStream objectInputStream = new ObjectInputStream(slaveSocket.getInputStream());
                                        try {
                                            processMsg((ChessMsg) objectInputStream.readObject());
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
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(masterSocket.getOutputStream());
                objectOutputStream.writeObject(msg);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendSlaveMsg(ChessMsg msg) {
        if (slaveSocket != null) {
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(slaveSocket.getOutputStream());
                objectOutputStream.writeObject(msg);
                objectOutputStream.flush();
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
        if (msg.type != MsgType.HEARTBEAT)
            System.out.printf("type:%s x:%d y:%d chess:%s\r\n", msg.type.toString(), msg.x, msg.y, msg.chess.toString());
        switch (msg.type) {
            case JOIN -> {
                if (!hasBlack) {
                    hasBlack = true;
                    sendMasterMsg(new ChessMsg(MsgType.SETCHESS, -1, -1, Chess.BLACK));
                    System.out.println("Set Black");
                } else {
                    sendSlaveMsg(new ChessMsg(MsgType.SETCHESS, -1, -1, Chess.WHITE));
                    System.out.println("Set White");
                }
            }
            case HEARTBEAT -> {
                sendAllMsg(new ChessMsg(MsgType.HEARTBEAT, -1, -1, Chess.EMPTY));
            }
            case PLACE -> {
                if (game.placeChess(msg.x, msg.y, msg.chess)) {
                    sendAllMsg(new ChessMsg(MsgType.PLACED, msg.x, msg.y, msg.chess));
                    System.out.println("Placed");
                }
            }
            case REQUIRE_REWIND -> {
                sendAllMsg(new ChessMsg(MsgType.REQUIRE_REWIND, 0, 0, msg.chess));
            }
            case ACCEPT_REWIND -> {
                game.rewind();
                sendAllMsg(new ChessMsg(MsgType.ACCEPT_REWIND, -1, -1, game.getNextTurn()));
            }
            case REJECT_REWIND -> {
                sendAllMsg(new ChessMsg(MsgType.REJECT_REWIND, -1, -1, game.getNextTurn()));
            }
        }
    }
}
