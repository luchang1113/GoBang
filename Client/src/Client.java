import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(new ChessMsg(MsgType.JOIN,-1,-1,Chess.EMPTY));
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            try {
                ChessMsg msg = (ChessMsg) objectInputStream.readObject();
                processMsg(msg);
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
                //System.out.println("update");
                try {
                    update();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void place(int x, int y){
        try {
            socket = new Socket("127.0.0.1",2333);
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(new ChessMsg(MsgType.PLACE,x,y,chess));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void update(){
        try {
            socket = new Socket("127.0.0.1",2333);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(new ChessMsg(MsgType.UPDATE,-1,-1,Chess.EMPTY));
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            try {
                ChessMsg msg = (ChessMsg) objectInputStream.readObject();
                processMsg(msg);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void processMsg(ChessMsg msg)
    {
        if(msg.type != MsgType.UPDATE)
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
