import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class ReplayFrame extends JFrame {
    private ChessBoard board;
    private final List<ChessStep> steps = new LinkedList<>();
    private int index = 0;
    JButton nextBtn;
    JButton prevBtn;
    ReplayFrame(String path) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            return;
        }
        FileReader fileReader = new FileReader(f);
        BufferedReader br = new BufferedReader(fileReader);
        String str = null;
        while((str = br.readLine()) != null) {
            steps.add(new ChessStep(str));
        }
        br.close();
        System.out.println(steps.size());

        setBounds(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 332, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 250, 664, 538);

        board = new ChessBoard(null);
        board.can_place = false;
        board.setBounds(0, 0, 500, 500);
        add(board);

        nextBtn = new JButton("Next");
        nextBtn.setBounds(500,60,100,30);
        nextBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextStep();
            }
        });
        add(nextBtn);
        prevBtn = new JButton("Prev");
        prevBtn.setBounds(500,90,100,30);
        prevBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prevStep();
            }
        });
        add(prevBtn);
        setBtn();
        setLayout(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        setVisible(true);
    }
    void setBtn() {
        if (index < steps.size()) {
            nextBtn.setEnabled(true);
        } else {
            nextBtn.setEnabled(false);
        }
        if(index > 0) {
            prevBtn.setEnabled(true);
        } else {
            prevBtn.setEnabled(false);
        }
    }
    void nextStep() {
        if (index < steps.size()) {
            board.addStep(steps.get(index));
            index++;
            board.repaint();
        }
        setBtn();
    }

    void prevStep() {
        if (index > 0) {
            board.rewind();
            index--;
        }
        setBtn();
    }
}
