import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MainWindow extends JFrame {
    public ChessBoard board;
    MainWindow(Client client){
        board = new ChessBoard(this,client);
        add(board);
        setSize(500,500);
        setVisible(true);
    }
}
