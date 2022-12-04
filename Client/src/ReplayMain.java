import javax.swing.*;
import java.io.IOException;

public class ReplayMain {
    public static void main(String args[]) throws IOException {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal=fileChooser.showOpenDialog(null);

        if(returnVal==JFileChooser.APPROVE_OPTION) {
            ReplayFrame replay = new ReplayFrame(fileChooser.getSelectedFile().getPath());
        }
    }
}
