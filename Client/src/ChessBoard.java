import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class ChessBoard extends JPanel implements MouseListener {
    JFrame mainWindow;
    Client client;
    boolean game_end = false;
    public List<ChessStep> steps = new ArrayList<>();

    ChessBoard(JFrame parent, Client client) {
        mainWindow = parent;
        this.client = client;
        this.addMouseListener(this);
        setSize(500, 500);
        setVisible(true);
    }

    public void addStep(int x, int y, Chess chess){
        steps.add(new ChessStep(x,y,chess));
        repaint();
    }

    public void rewind(){
        if(steps.size() < 1)
            return;
        steps.remove(steps.size()-1);
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
                    g.setColor(Color.WHITE);
                    g.fillOval(step.y * 30 + 25, step.x * 30 + 25, 30, 30);
                }
                case BLACK -> {
                    g.setColor(Color.BLACK);
                    g.fillOval(step.y * 30 + 25, step.x * 30 + 25, 30, 30);
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
        if (!game_end) {
            int place_x = (e.getX() - 25) / 30;
            int place_y = (e.getY() - 25) / 30;
            client.place(place_x,place_y);
            this.repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
