import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.UnknownHostException;

public class LoginFrame extends JFrame {
    GameServer gameServer;
    GameFrame gameFrame;
    ImageIcon BGIcon;
    JLabel BG;
    JPanel panel;
    JButton hostBtn;
    JTextField ipField;
    JTextField portField;
    JButton joinBtn;
    Client client;

    LoginFrame() {
        client = new Client();
        BGIcon = new ImageIcon("./Client/images/Login_BG.png");
        BG = new JLabel(BGIcon);
        BG.setBounds(0, 0, 600, 300);
        ipField = new JTextField("127.0.0.1");
        ipField.setBounds(350, 200, 125, 30);
        portField = new JTextField("2333");
        portField.setBounds(500, 200, 50, 30);
        hostBtn = new JButton("创建房间");
        hostBtn.setBounds(350, 125, 200, 30);
        hostBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameServer = new GameServer(2333);
                if (gameServer.serverBegin()) {
                    gameFrame = new GameFrame(client);
                    client.bindGameFrame(gameFrame);
                    if (client.Join("127.0.0.1", 2333)) {
                        gameFrame.setVisible(true);
                        try {
                            gameFrame.appendMessage("Server IP:"+gameServer.getServerIP());
                        } catch (UnknownHostException ex) {
                            throw new RuntimeException(ex);
                        }
                        setVisible(false);
                    }
                }
            }
        });
        joinBtn = new JButton("连接");
        joinBtn.setBounds(350, 250, 200, 30);
        joinBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameFrame = new GameFrame(client);
                client.bindGameFrame(gameFrame);
                if (client.Join(ipField.getText(), Integer.parseInt(portField.getText()))) {
                    gameFrame.setVisible(true);
                    setVisible(false);
                }
            }
        });
        setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 300, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 150, 614, 338);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        getLayeredPane().add(ipField);
        getLayeredPane().add(portField);
        getLayeredPane().add(joinBtn);
        getLayeredPane().add(hostBtn);
        setResizable(false);
        getLayeredPane().add(BG, Integer.MIN_VALUE);
        setLayout(null);
        setTitle("五子棋");
        setVisible(true);
    }


}
