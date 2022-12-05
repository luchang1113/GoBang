import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.UnknownHostException;

public class LoginFrame extends JFrame {
    GameServer gameServer;
    GameFrame gameFrame;
    ImageIcon BGIcon;
    JLabel BG;
    JButton hostBtn;
    JTextField ipField;
    JTextField portField;
    JButton joinBtn;
    JButton replayBtn;
    Client client;

    LoginFrame() {
        client = new Client();
        BGIcon = new ImageIcon("./Client/images/Login_BG.png");
        BG = new JLabel(BGIcon);
        BG.setBounds(0, 0, 512, 250);
        ipField = new JTextField("127.0.0.1");
        ipField.setBounds(250, 325, 125, 30);
        portField = new JTextField("2333");
        portField.setBounds(400, 325, 50, 30);
        hostBtn = new JButton("创建房间");
        hostBtn.setBounds(50, 325, 100, 30);
        hostBtn.addActionListener(e -> {
            gameServer = new GameServer(0);
            if (gameServer.serverBegin()) {
                gameFrame = new GameFrame(client);
                client.bindGameFrame(gameFrame);
                if (client.Join("127.0.0.1", gameServer.getServerPort())) {
                    gameFrame.setVisible(true);
                    try {
                        gameFrame.appendMessage("Server IP:"+gameServer.getServerIP());
                        gameFrame.appendMessage("Server Port:"+gameServer.getServerPort());
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                    setVisible(false);
                }
            }
        });
        joinBtn = new JButton("连接");
        joinBtn.setBounds(300, 370, 100, 30);
        joinBtn.addActionListener(e -> {
            gameFrame = new GameFrame(client);
            client.bindGameFrame(gameFrame);
            if (client.Join(ipField.getText(), Integer.parseInt(portField.getText()))) {
                gameFrame.setVisible(true);
                setVisible(false);
            } else {
                JOptionPane.showMessageDialog(null, "无法连接至服务器", "连接失败",JOptionPane.ERROR_MESSAGE);
            }
        });
        replayBtn = new JButton("复盘");
        replayBtn.setBounds(50,370,100,30);
        replayBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(".");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal=fileChooser.showOpenDialog(null);

            if(returnVal==JFileChooser.APPROVE_OPTION) {
                try {
                    new ReplayFrame(fileChooser.getSelectedFile().getPath());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 256, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 256, 512, 512);
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
        getLayeredPane().add(replayBtn);
        setResizable(false);
        getLayeredPane().add(BG, Integer.MIN_VALUE);
        setLayout(null);
        setTitle("五子棋");
        setVisible(true);
    }


}
