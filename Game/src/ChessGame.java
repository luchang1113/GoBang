import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChessGame {
    public List<ChessStep> steps;
    private final Chess[][] board = new Chess[15][15];
    private Chess next_chess = Chess.BLACK;
    public Chess winner = Chess.EMPTY;
    public boolean game_end = false;
    public ChessGame(){
        steps = new ArrayList<>();
        for(int x = 0;x < 15;x++){
            for(int y = 0;y < 15;y++){
                board[x][y] = Chess.EMPTY;
            }
        }
    }
    public List<ChessStep> getSteps(){
        return steps;
    }
    public Chess getNextTurn(){
        return next_chess;
    }
    public void rewind(){
        if(steps.size() < 1)
        {
            System.out.println("[ChessGame] Cannot rewind");
            return;
        }
        board[steps.get(steps.size()-1).x][steps.get(steps.size()-1).y] = Chess.EMPTY;
        steps.remove(steps.size()-1);
        switch (next_chess){
            case BLACK -> next_chess = Chess.WHITE;
            case WHITE -> next_chess = Chess.BLACK;
        }
        System.out.printf("[ChessGame] Next Chess is %s\r\n",next_chess.toString());
        //display();
    }
    public void display(){
        for(Chess[] row : board){
            for(Chess chess : row){
                switch(chess){
                    case EMPTY -> System.out.print(". ");
                    case BLACK -> System.out.print("b ");
                    case WHITE -> System.out.print("w ");
                }
            }
            System.out.print("\r\n");
        }
    }
    public boolean placeChess(int x, int y){
        if(!placeChess(x,y,next_chess)){
            return false;
        }
        return true;
    }
    public boolean placeChess(int x, int y, Chess chess){
        if(x < 0 || y < 0 || x > 14 || y > 14 || chess == Chess.EMPTY || board[x][y] != Chess.EMPTY){
            System.out.println("[ChessGame] Invalid place");
            return false;
        }
        steps.add(new ChessStep(x,y,chess));
        board[x][y] = chess;
        for(ChessStep step : steps){
            if(checkWin(step.x, step.y, step.chess)){
                switch (step.chess){
                    case BLACK -> System.out.println("[ChessGame] Black wins!");
                    case WHITE -> System.out.println("[ChessGame] White wins");
                }
                game_end = true;
            }
        }
        //display();
        switch (next_chess){
            case BLACK -> next_chess = Chess.WHITE;
            case WHITE -> next_chess = Chess.BLACK;
        }
        System.out.printf("[ChessGame] Next Chess is %s\r\n",next_chess.toString());
        return true;
    }
    public void reset(){
        steps.clear();
        winner = Chess.EMPTY;
        for(int x = 0;x < 15;x++){
            for(int y = 0;y < 15;y++){
                board[x][y] = Chess.EMPTY;
            }
        }
        game_end = false;
        next_chess = Chess.BLACK;
    }
    private boolean checkWin(int x, int y, Chess chess){
        boolean flag = false;
        int count = 1;
        int i = 1;
        while(x+i < 15 && y < 15 && board[x+i][y] == chess ){
            i++;
            count++;
        }
        if(count >= 5){
            flag = true;
        }
        count = 1;
        i = 1;
        while(y+i<15 && x < 15 && board[x][y+i] == chess){
            i++;
            count++;
        }
        if(count >= 5){
            flag = true;
        }
        count = 1;
        i = 1;
        while(y+i<15 && x-i >= 0 && board[x-i][y+i] == chess){
            i++;
            count++;
        }
        if(count >= 5){
            flag = true;
        }
        count = 1;
        i = 1;
        while(x + i < 15 && y + i < 15 && board[x+i][y+i] == chess){
            i++;
            count++;
        }
        if(count >= 5){
            flag = true;
        }
        if(flag){
            winner = chess;
        }
        return flag;
    }
    public void game2File(String path) throws IOException {
        System.out.println(path);
        File f = new File(path);
        if(!f.exists()){
            f.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(f.getName(),true);
        BufferedWriter bw = new BufferedWriter(fileWriter);
        for(ChessStep step : steps){
            bw.write(step.toString()+"\r\n");
            System.out.printf("[ChessGame] File Write:%s\r\n", step);
        }
        bw.flush();
        bw.close();
        System.out.println(f.getAbsolutePath());
    }
}
