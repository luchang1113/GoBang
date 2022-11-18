import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class Client {
    Socket socket;
    Chess chess = Chess.EMPTY;
    MainWindow window;
    boolean gameStarted = false;

    public Client(){
        window = new MainWindow(this);
    }

    public void setReady(){
        try {
            socket = new Socket("127.0.0.1",2333);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(new ChessMsg(MsgType.JOIN,-1,-1,Chess.EMPTY));
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            try {
                processMsg((ChessMsg) objectInputStream.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void startGame(){
        new Thread(() -> {
            setReady();
            while(true)
            {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //System.out.println(df.format(System.currentTimeMillis()) + "update");
                update();
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }).start();
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

//        try {
//            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
//            try {
//                processMsg((ChessMsg) objectInputStream.readObject());
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    public void update(){

//        try {
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//            objectOutputStream.writeObject(new ChessMsg(MsgType.HEARTBEAT,-1,-1,Chess.EMPTY));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            try {
                processMsg((ChessMsg) objectInputStream.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void processMsg(ChessMsg msg)
    {
        if(msg.type != MsgType.HEARTBEAT)
            System.out.printf("type:%s x:%d y:%d chess:%s\r\n",msg.type.toString(),msg.x,msg.y,msg.chess.toString());
        switch (msg.type){
            case SETCHESS -> {
                if(chess == Chess.EMPTY){
                    chess = msg.chess;
                    switch(chess){
                        case BLACK -> System.out.println("get b");
                        case WHITE -> System.out.println("get w");
                    }
                }
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
