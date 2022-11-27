public class ChessStep {
    Chess chess;
    int x;
    int y;
    ChessStep(int x, int y, Chess chess){
        this.x = x;
        this.y = y;
        this.chess = chess;
    }
    public String toString(){
        return chess.toString() + ":" + x + ":" + y;
    }
}
