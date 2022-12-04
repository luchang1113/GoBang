import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    private Socket socket;
    private BufferedOutputStream out = null;
    private BufferedReader in = null;
    private ClientType type = ClientType.WATCHER;
    private Chess chess = Chess.EMPTY;
    private GameFrame window;
    private final boolean gameStarted = false;

    public void bindGameFrame(GameFrame gameFrame){
        window = gameFrame;
    }

    private void sendMsg(ChessMsg msg) {
        try {
            if (out == null)
                out = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            System.out.printf("[Client] [Send] Type:%s, x:%d, y:%d, Chess:%s\r\n", msg.type.toString(), msg.x, msg.y, msg.chess.toString());
            out.write((msg + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException ignored) {
        }
    }

    public boolean Join(String host, int port) {
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMsg(new ChessMsg(MsgType.JOIN, -1, -1, Chess.EMPTY));
        startUpdate();
        return true;
    }

    private void startUpdate() {
        Thread updateThread = new Thread(() -> {
            while (socket != null && in != null) {
                try {
                    processMsg(new ChessMsg(in.readLine()));
                } catch (IOException ignored) {
                }
            }
        });
        updateThread.start();
    }

    public void setReady(boolean ready) {
        sendMsg(new ChessMsg(MsgType.READY, ready ? 1 : 0, 0, Chess.fromInt(type.toInt())));
    }

    public void place(int x, int y) {
        sendMsg(new ChessMsg(MsgType.PLACE, x, y, chess));
    }

    public void RequireRewind() {
        sendMsg(new ChessMsg(MsgType.REQUIRE_REWIND, 0, 0, Chess.fromInt(type.toInt())));
    }
    public void sendChat(String msg) {
        sendMsg(new ChessMsg(MsgType.CHAT,0,0,Chess.fromInt(type.toInt()),msg));
    }

    public void disconnect() {
        if (socket != null && !socket.isClosed() && in != null) {
            sendMsg(new ChessMsg(MsgType.EXIT, 0, 0, Chess.fromInt(type.toInt())));
            try {
                socket.close();
                socket=null;
                in = null;
            } catch (IOException ignored) {
            }
        }
    }

    private void processMsg(ChessMsg msg) {
        System.out.printf("[Client] [Receive] Type:%s x:%d y:%d Chess:%s\r\n", msg.type.toString(), msg.x, msg.y, msg.chess.toString());
        switch (msg.type) {
            case SET_CLIENT -> {
                type = ClientType.fromInt(msg.chess.toInt());
                assert type != null;
                window.setTitle(type.toString());
            }
            case SET_CHESS -> {
                if (chess == Chess.EMPTY) {
                    chess = msg.chess;
                    window.setTitle(chess.toString());
                }
            }
            case START -> {
                window.board.can_place = chess == Chess.BLACK;
                window.board.steps.clear();
                window.board.repaint();
            }
            case PLACED -> {
                window.board.addStep(msg.x, msg.y, msg.chess);
                if (msg.chess != chess) {
                    window.board.can_place = true;
                    window.rewindBtn.setEnabled(false);
                } else {
                    window.board.can_place = false;
                    window.rewindBtn.setEnabled(true);
                }
            }
            case REQUIRE_REWIND -> {
                if (window.AskRewind() == 0) {
                    sendMsg(new ChessMsg(MsgType.ACCEPT_REWIND, 0, 0, Chess.fromInt(type.toInt())));
                    window.board.can_place = false;
                    window.rewindBtn.setEnabled(true);
                } else {
                    sendMsg(new ChessMsg(MsgType.REJECT_REWIND, 0, 0, Chess.fromInt(type.toInt())));
                    window.board.can_place = true;
                    window.rewindBtn.setEnabled(false);
                }
            }
            case GAME_END -> {
                window.setTitle("GAME_END");
                window.board.can_place = false;
                window.rewindBtn.setEnabled(false);
                System.out.printf("%s wins\r\n", msg.chess.toString());
                window.startBtn.setEnabled(true);
            }
            case ACCEPT_REWIND -> {
                System.out.println("Rewind accepted");
                if (msg.chess == chess) {
                    window.board.can_place = true;
                    window.rewindBtn.setEnabled(false);
                } else {
                    window.board.can_place = false;
                    window.rewindBtn.setEnabled(true);
                }
                window.board.rewind();
            }
            case REJECT_REWIND -> {
                System.out.println("Rewind rejected");
            }
            case CHAT -> {
                window.appendMessage(ClientType.fromInt(msg.chess.toInt()).toString()+":"+msg.msg);
            }
        }
    }
}
