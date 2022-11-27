import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameServer {
    private final ChessGame game;
    private ServerSocket serverSocket = null;
    private Socket masterSocket = null;
    private Socket slaveSocket = null;
    private List<Socket> watchSockets = new ArrayList<>();
    private BufferedReader masterIn = null;
    private BufferedReader slaveIn = null;
    private BufferedOutputStream masterOut = null;
    private BufferedOutputStream slaveOut = null;
    private boolean masterReady = false;
    private boolean slaveReady = false;

    public GameServer(int port) {
        game = new ChessGame();
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(port);
                serverBegin();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void serverBegin() {
        new Thread(() -> {
            synchronized (game){

            while (true) {
                try {
                    System.out.println("Wait for Client");
                    Socket socket = serverSocket.accept();
                    System.out.println("New Client");
                    if (masterSocket == null) {
                        masterSocket = socket;
                    } else if (slaveSocket == null) {
                        slaveSocket = socket;
                    } else {
                        System.out.println(masterSocket.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (masterSocket != null) {
                    new Thread(() -> {
                        while (masterSocket != null) {
                            if(masterSocket.isClosed()){
                                masterSocket = null;
                                masterIn = null;
                                masterOut = null;
                                break;
                            }
                            try {
                                if(masterIn == null)
                                    masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
                                if (masterOut == null) {
                                    masterOut = new BufferedOutputStream(masterSocket.getOutputStream());
                                }
                                ChessMsg msg = new ChessMsg(masterIn.readLine());
                                processMsg(msg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                if (slaveSocket != null) {
                    new Thread(() -> {
                        while (!slaveSocket.isClosed()) {
                            try {
                                if(slaveIn == null)
                                    slaveIn = new BufferedReader(new InputStreamReader(slaveSocket.getInputStream()));
                                if (slaveOut == null) {
                                    slaveOut = new BufferedOutputStream(slaveSocket.getOutputStream());
                                }
                                ChessMsg msg = new ChessMsg(slaveIn.readLine());
                                processMsg(msg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        slaveSocket = null;
                        slaveIn = null;
                        slaveOut = null;
                    }).start();
                }
            }

            }
        }).start();
    }

    private void sendMasterMsg(ChessMsg msg) {
        if (masterSocket != null) {
            try {
                System.out.printf("[Send Master] Type:%s, x:%d, y:%d, Chess:%s\r\n", msg.type.toString(), msg.x, msg.y, msg.chess.toString());
                masterOut.write((msg.toString()+"\r\n").getBytes(StandardCharsets.UTF_8));
                masterOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendSlaveMsg(ChessMsg msg) {
        if (slaveSocket != null) {
            try {
                System.out.printf("[Send Slave] Type:%s, x:%d, y:%d, Chess:%s\r\n", msg.type.toString(), msg.x, msg.y, msg.chess.toString());
                slaveOut.write((msg.toString()+"\r\n").getBytes(StandardCharsets.UTF_8));
                slaveOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendWatcherMsg(ChessMsg msg) {
    }

    private void sendAllMsg(ChessMsg msg) {
        sendMasterMsg(msg);
        sendSlaveMsg(msg);
        sendWatcherMsg(msg);
    }

    private void processMsg(ChessMsg msg) throws IOException {
        System.out.printf("[Receive] Type:%s x:%d y:%d Chess:%s\r\n", msg.type.toString(), msg.x, msg.y, msg.chess.toString());
        switch (msg.type) {
            case JOIN -> {
                if (masterOut != null) {
                    sendMasterMsg(new ChessMsg(MsgType.SET_CLIENT, 0, 0, Chess.fromInt(ClientType.MASTER.toInt())));
                }
                if (slaveOut != null) {
                    sendSlaveMsg(new ChessMsg(MsgType.SET_CLIENT, 0, 0, Chess.fromInt(ClientType.SLAVE.toInt())));
                }
            }
            case READY -> {
                switch (msg.chess) {
                    case BLACK -> {
                        masterReady = msg.x == 1;
                    }
                    case WHITE -> {
                        slaveReady = msg.x == 1;
                    }
                }
                if (masterReady && slaveReady) {
                    sendMasterMsg(new ChessMsg(MsgType.SET_CHESS, 0, 0, Chess.BLACK));
                    sendSlaveMsg(new ChessMsg(MsgType.SET_CHESS, 0, 0, Chess.WHITE));
                    sendAllMsg(new ChessMsg(MsgType.START, 0, 0, Chess.EMPTY));
                    game.reset();
                }
            }
            case PLACE -> {
                if (game.placeChess(msg.x, msg.y, msg.chess)) {
                    sendAllMsg(new ChessMsg(MsgType.PLACED, msg.x, msg.y, msg.chess));
                }
                if (game.game_end) {
                    sendAllMsg(new ChessMsg(MsgType.GAME_END, 0, 0, game.winner));
                    Date day=new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                    game.game2File("./"+df.format(day)+".txt");
                    masterReady = false;
                    slaveReady = false;
                }
            }
            case REQUIRE_REWIND -> {
                switch(msg.chess){
                    case BLACK -> {
                        sendSlaveMsg(new ChessMsg(MsgType.REQUIRE_REWIND,0,0,msg.chess));
                    }
                    case WHITE -> {
                        sendMasterMsg(new ChessMsg(MsgType.REQUIRE_REWIND,0,0,msg.chess));
                    }
                }
            }
            case ACCEPT_REWIND -> {
                game.rewind();
                sendAllMsg(new ChessMsg(MsgType.ACCEPT_REWIND,0,0,game.getNextTurn()));
            }
            case EXIT -> {
                switch (msg.chess) {
                    case BLACK -> {
                        try {
                            masterSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    case WHITE -> {
                        try {
                            slaveSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
