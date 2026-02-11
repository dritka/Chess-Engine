package Game;

import javax.swing.*;

public class Main {

    public static JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    public Main() {
        frame = new JFrame();
        frame.setTitle("Chess (:!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new Board());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}