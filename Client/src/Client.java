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

    public Client(GameFrame window) {
        this.window = window;
    }

    private void sendMsg(ChessMsg msg) {
        try {
            if (out == null)
                out = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            System.out.printf("[Send] Type:%s, x:%d, y:%d, Chess:%s\r\n", msg.type.toString(), msg.x, msg.y, msg.chess.toString());
            out.write((msg + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Join(String host) {
        try {
            socket = new Socket(host, 2333);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (socket != null) {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendMsg(new ChessMsg(MsgType.JOIN, -1, -1, Chess.EMPTY));
            startUpdate();
        }
    }

    private void startUpdate() {
        updateThread = new Thread(() -> {
            while (socket != null && in != null) {
                try {
                    processMsg(new ChessMsg(in.readLine()));
                } catch (IOException e) {
                    e.printStackTrace();
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

    public void disconnect() {
        if (socket != null && !socket.isClosed() && in != null) {
            sendMsg(new ChessMsg(MsgType.EXIT, 0, 0, Chess.fromInt(type.toInt())));
            try {
                socket.close();
                socket=null;
                in = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMsg(ChessMsg msg) {
        System.out.printf("[Receive] Type:%s x:%d y:%d Chess:%s\r\n", msg.type.toString(), msg.x, msg.y, msg.chess.toString());
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
        }
    }
}
