import java.io.Serializable;

public class ChessMsg implements Serializable {
    public MsgType type;
    public int x;
    public int y;
    public Chess chess;
    public ChessMsg(MsgType type){
        this.type = type;
    }
    public ChessMsg(MsgType type, int x, int y, Chess chess){
        this.type = type;
        this.x = x;
        this.y = y;
        this.chess = chess;
    }
}
