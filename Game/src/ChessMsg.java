import java.io.Serializable;

public class ChessMsg implements Serializable {
    public MsgType type;
    public int x;
    public int y;
    public Chess chess;
    public String msg;
    public ChessMsg(MsgType type, int x, int y, Chess chess){
        this.type = type;
        this.x = x;
        this.y = y;
        this.chess = chess;
        this.msg = "NoMessage";
    }
    public ChessMsg(MsgType type, int x, int y, Chess chess, String msg){
        this.type = type;
        this.x = x;
        this.y = y;
        this.chess = chess;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return type.toString()+":"+ x + ":" + y + ":" + chess.toString() + ":" + msg;
    }

    public ChessMsg(String string){
        type = MsgType.valueOf(string.split(":")[0]);
        x = Integer.parseInt(string.split(":")[1]);
        y = Integer.parseInt(string.split(":")[2]);
        chess = Chess.valueOf(string.split(":")[3]);
        msg = string.split(":")[4];
    }
}
