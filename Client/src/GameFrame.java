import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameFrame extends JFrame {
    ChessBoard board;
    JButton startBtn;
    JButton rewindBtn;
    Client client;
    GameFrame(Client client){
        this.client = client;
        board = new ChessBoard(client);
        board.setBounds(0,0,500,500);
        startBtn = new JButton("Start");
        startBtn.setBounds(500,60,80,30);
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.setReady(true);
                startBtn.setEnabled(false);
            }
        });
        rewindBtn = new JButton("Rewind");
        rewindBtn.setBounds(500,90,80,30);
        rewindBtn.setEnabled(false);
        rewindBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.RequireRewind();
                rewindBtn.setEnabled(false);
            }
        });
        add(board);
        add(startBtn);
        add(rewindBtn);
        setSize(1280,720);
        setLayout(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
                System.exit(0);
            }
        });
    }
    int AskRewind(){
        return JOptionPane.showConfirmDialog(this,"是否允许悔棋","对方请求悔棋",JOptionPane.YES_NO_OPTION);
    }
}
