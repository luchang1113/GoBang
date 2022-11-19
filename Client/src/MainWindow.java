import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MainWindow extends JFrame {
    ChessBoard board;
    JButton startBtn;
    Client client;
    MainWindow(){
        client = new Client(this);
        board = new ChessBoard(client);
        board.setBounds(0,0,500,500);
        startBtn = new JButton("Start");
        startBtn.setBounds(500,0,80,30);
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.setReady(true);
                startBtn.setEnabled(false);
            }
        });
        add(board);
        add(startBtn);
        setBounds(300,300,600,540);
        setLayout(null);
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.exit();
                System.exit(0);
            }
        });

        client.Join();
    }
}
