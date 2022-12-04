import java.util.Scanner;

public class GameMain {
    public static void main(String[] args){
        ChessGame game = new ChessGame();
        Scanner scanner = new Scanner(System.in);
        while(!game.game_end){
            int n = scanner.nextInt();
            if (n == 0) {
                int x = scanner.nextInt();
                int y = scanner.nextInt();
                game.placeChess(x, y);
                game.display();
            }
        }
    }
}
