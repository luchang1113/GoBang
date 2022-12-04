import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameFrame extends JFrame {
    ChessBoard board;
    JButton startBtn;
    JButton rewindBtn;
    JLabel message;
    JTextArea chatArea;
    JTextField chatMsg;
    Client client;
    GameFrame(Client client){
        this.client = client;
        board = new ChessBoard(client);
        board.setBounds(0,0,500,500);
        message = new JLabel();
        message.setBounds(500,30,150,30);
        message.setHorizontalTextPosition(SwingConstants.CENTER);
        startBtn = new JButton("Start");
        startBtn.setBounds(525,60,100,30);
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.setReady(true);
                startBtn.setEnabled(false);
            }
        });
        rewindBtn = new JButton("Rewind");
        rewindBtn.setBounds(525,90,100,30);
        rewindBtn.setEnabled(false);
        rewindBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.RequireRewind();
                rewindBtn.setEnabled(false);
            }
        });
        chatArea = new JTextArea();
        chatArea.setBounds(500,180,150,200);
        chatArea.setEditable(false);
        chatMsg = new JTextField();
        chatMsg.setBounds(500,380,150,30);
        chatMsg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendChat(chatMsg.getText());
                chatMsg.setText("");

            }
        });
        add(board);
        add(startBtn);
        add(rewindBtn);
        add(message);
        add(chatArea);
        add(chatMsg);
        setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 332, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 250, 664, 538);
        setLayout(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
                System.exit(0);
            }
        });
    }

    public void appendMessage(String msg){
        chatArea.append(msg+"\r\n");
    }
    int AskRewind(){
        return JOptionPane.showConfirmDialog(this,"是否允许悔棋","对方请求悔棋",JOptionPane.YES_NO_OPTION);
    }
}
