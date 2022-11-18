import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginWindow {
    LoginWindow(){
        JFrame window = new JFrame();
        window.setTitle("Login");
        ImageIcon BG = new ImageIcon("image/Login_BG.jpeg");
        Image img = BG.getImage();
        img = img.getScaledInstance(300,300,Image.SCALE_AREA_AVERAGING);
        BG.setImage(img);
        JLabel iconLabel = new JLabel(BG);
        window.add(iconLabel);
        window.setBounds(0,0,500,300);
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.gridwidth = 1;
        g.weightx = 1;
        g.weighty = 1;
        layout.setConstraints(iconLabel,g);
        window.setLayout(layout);
        window.setVisible(true);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}
