import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

public class ChessBoard extends JPanel implements MouseListener {
    Client client;
    boolean can_place = false;
    public List<ChessStep> steps = new ArrayList<>();
    ImageIcon blackChess = new ImageIcon("./images/black_chess.png");
    ImageIcon whiteChess = new ImageIcon("./images/white_chess.png");

    ChessBoard(Client client) {
        this.client = client;
        this.addMouseListener(this);
        setVisible(true);
    }

    public void addStep(int x, int y, Chess chess) {
        steps.add(new ChessStep(x, y, chess));
        repaint();
    }

    public void rewind() {
        if (steps.size() < 1)
            return;
        steps.remove(steps.size() - 1);
        repaint();
    }

    public void paint(Graphics g) {
        g.setColor(new Color(127, 127, 127));
        g.fillRect(0, 0, 500, 500);
        g.setColor(Color.WHITE);
        for (int i = 0; i < 15; i++) {
            g.drawLine(i * 30 + 40, 40, i * 30 + 40, 460);
            g.drawLine(40, i * 30 + 40, 460, i * 30 + 40);
        }
        for (int i = 0; i < 3; i++) {
            g.fillOval(125, 125 + i * 120, 10, 10);
            g.fillOval(245, 125 + i * 120, 10, 10);
            g.fillOval(365, 125 + i * 120, 10, 10);
        }
        for (ChessStep step : steps) {
            switch (step.chess) {
                case WHITE -> {
                    g.drawImage(whiteChess.getImage(),step.y * 30 + 25, step.x * 30 +25,30,30, this);
                }
                case BLACK -> {
                    g.drawImage(blackChess.getImage(),step.y * 30 + 25, step.x * 30 +25,30,30, this);
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (can_place) {
            int place_x = (e.getX() - 25) / 30;
            int place_y = (e.getY() - 25) / 30;
            if (place_x >= 0 && place_x <= 14 && place_y >= 0 && place_y <= 14) {
                client.place(place_y, place_x);
                this.repaint();
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
