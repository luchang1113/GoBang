import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameServer {
    private final ChessGame game;
    private final int port;
    private ServerSocket serverSocket = null;
    private Socket hostSocket = null;
    private Socket clientSocket = null;
    private BufferedReader hostIn = null;
    private BufferedReader clientIn = null;
    private BufferedOutputStream hostOut = null;
    private BufferedOutputStream clientOut = null;
    private boolean hostReady = false;
    private boolean clientReady = false;

    public GameServer(int port) {
        game = new ChessGame();
        this.port = port;
    }

    public String getServerIP() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }
    public int getServerPort(){
        return serverSocket.getLocalPort();
    }

    public boolean serverBegin() {
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        new Thread(() -> {
            synchronized (game){
                try {
                    System.out.printf("Server start @%s:%d\r\n", InetAddress.getLocalHost().getHostAddress(), serverSocket.getLocalPort());
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                while (true) {
                try {
                    System.out.println("Wait for Client");
                    Socket socket = serverSocket.accept();
                    System.out.println("New Client");
                    if (hostSocket == null) {
                        hostSocket = socket;
                    } else if (clientSocket == null) {
                        clientSocket = socket;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (hostSocket != null) {
                    new Thread(() -> {
                        while (hostSocket != null) {
                            if(hostSocket.isClosed()){
                                hostSocket = null;
                                hostIn = null;
                                hostOut = null;
                                break;
                            }
                            try {
                                if(hostIn == null)
                                    hostIn = new BufferedReader(new InputStreamReader(hostSocket.getInputStream()));
                                if (hostOut == null) {
                                    hostOut = new BufferedOutputStream(hostSocket.getOutputStream());
                                }
                                ChessMsg msg = new ChessMsg(hostIn.readLine());
                                processMsg(msg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                if (clientSocket != null) {
                    new Thread(() -> {
                        while (!clientSocket.isClosed()) {
                            try {
                                if(clientIn == null)
                                    clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                                if (clientOut == null) {
                                    clientOut = new BufferedOutputStream(clientSocket.getOutputStream());
                                }
                                ChessMsg msg = new ChessMsg(clientIn.readLine());
                                processMsg(msg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        clientSocket = null;
                        clientIn = null;
                        clientOut = null;
                    }).start();
                }
            }

            }
        }).start();
        return true;
    }

    private void sendHostMsg(ChessMsg msg) {
        if (hostSocket != null) {
            try {
                System.out.printf("[Server] [Send Master] Type:%s, x:%d, y:%d, Chess:%s\r\n", msg.type.toString(), msg.x, msg.y, msg.chess.toString());
                hostOut.write((msg +"\r\n").getBytes(StandardCharsets.UTF_8));
                hostOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendClientMsg(ChessMsg msg) {
        if (clientSocket != null) {
            try {
                System.out.printf("[Server] [Send Slave] Type:%s, x:%d, y:%d, Chess:%s\r\n", msg.type.toString(), msg.x, msg.y, msg.chess.toString());
                clientOut.write((msg +"\r\n").getBytes(StandardCharsets.UTF_8));
                clientOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendWatcherMsg(ChessMsg msg) {
    }

    private void sendAllMsg(ChessMsg msg) {
        sendHostMsg(msg);
        sendClientMsg(msg);
        sendWatcherMsg(msg);
    }

    private void processMsg(ChessMsg msg) throws IOException {
        System.out.printf("[Server] [Receive] Type:%s x:%d y:%d Chess:%s\r\n", msg.type.toString(), msg.x, msg.y, msg.chess.toString());
        switch (msg.type) {
            case JOIN -> {
                if (hostOut != null) {
                    sendHostMsg(new ChessMsg(MsgType.SET_CLIENT, 0, 0, Chess.fromInt(ClientType.HOST.toInt())));
                }
                if (clientOut != null) {
                    sendClientMsg(new ChessMsg(MsgType.SET_CLIENT, 0, 0, Chess.fromInt(ClientType.CLIENT.toInt())));
                }
            }
            case READY -> {
                switch (msg.chess) {
                    case BLACK -> {
                        hostReady = msg.x == 1;
                    }
                    case WHITE -> {
                        clientReady = msg.x == 1;
                    }
                }
                if (hostReady && clientReady) {
                    sendHostMsg(new ChessMsg(MsgType.SET_CHESS, 0, 0, Chess.BLACK));
                    sendClientMsg(new ChessMsg(MsgType.SET_CHESS, 0, 0, Chess.WHITE));
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
                    hostReady = false;
                    clientReady = false;
                }
            }
            case REQUIRE_REWIND -> {
                switch(msg.chess){
                    case BLACK -> {
                        sendClientMsg(new ChessMsg(MsgType.REQUIRE_REWIND,0,0,msg.chess));
                    }
                    case WHITE -> {
                        sendHostMsg(new ChessMsg(MsgType.REQUIRE_REWIND,0,0,msg.chess));
                    }
                }
            }
            case ACCEPT_REWIND -> {
                game.rewind();
                sendAllMsg(new ChessMsg(MsgType.ACCEPT_REWIND,0,0,game.getNextTurn()));
            }
            case CHAT -> {
                sendAllMsg(new ChessMsg(MsgType.CHAT,0,0,msg.chess,msg.msg));
            }
            case EXIT -> {
                switch (msg.chess) {
                    case BLACK -> {
                        try {
                            hostSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    case WHITE -> {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
