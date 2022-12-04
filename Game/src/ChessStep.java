public class ChessStep {
    Chess chess;
    int x;
    int y;
    ChessStep(int x, int y, Chess chess){
        this.x = x;
        this.y = y;
        this.chess = chess;
    }
    ChessStep(String string) {
        chess = Chess.valueOf(string.split(":")[0]);
        x = Integer.parseInt(string.split(":")[1]);
        y = Integer.parseInt(string.split(":")[2]);
    }
    public String toString(){
        return chess.toString() + ":" + x + ":" + y;
    }
}
