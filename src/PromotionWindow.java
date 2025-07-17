import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import static Enums.SoundType.CLICK;
import static Enums.SoundType.PROMOTE;
import static Enums.Type.*;

public class PromotionWindow extends JFrame implements ActionListener {
    public JButton queenButton, knightButton, bishopButton, rookButton, chooseButton;
    public Enums.Type pieceType;
    public Enums.Color pieceColor;
    public Square to;

    public PromotionWindow(Enums.Color pieceColor, Square to) {
        pieceType = null;
        this.pieceColor = pieceColor;
        this.to = to;

        Map<Enums.Type, Enums.Color> map = new HashMap<>();
        map.put(QUEEN, pieceColor);
        queenButton = new JButton(new ImageIcon(Board.imagePaths.get(map)));
        map.clear();
        map.put(KNIGHT, pieceColor);
        knightButton = new JButton(new ImageIcon(Board.imagePaths.get(map)));
        map.clear();
        map.put(BISHOP, pieceColor);
        bishopButton = new JButton(new ImageIcon(Board.imagePaths.get(map)));
        map.clear();
        map.put(ROOK, pieceColor);
        rookButton = new JButton(new ImageIcon(Board.imagePaths.get(map)));
        chooseButton = new JButton("Choose");

        queenButton.addActionListener(this);
        rookButton.addActionListener(this);
        knightButton.addActionListener(this);
        bishopButton.addActionListener(this);
        chooseButton.addActionListener(this);

        JPanel optionsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        optionsPanel.add(queenButton, 0);
        optionsPanel.add(rookButton, 1);
        optionsPanel.add(bishopButton, 2);
        optionsPanel.add(knightButton, 3);
        optionsPanel.setPreferredSize(new Dimension(350, 100));

        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(chooseButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(optionsPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        this.add(mainPanel);

        this.setTitle("Promotion chooser");
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
    }

    private Piece getPieceFromType() {
        Piece piece = null;
        Map<Enums.Type, Enums.Color> map = new HashMap<>();
        
        switch (pieceType) {
            case QUEEN -> {
                map.put(QUEEN, pieceColor);
                piece = new Piece(QUEEN, pieceColor, to.row, to.col, 9, Board.imagePaths.get(map));
            }
            case ROOK -> {
                map.put(ROOK, pieceColor);
                piece = new Piece(ROOK, pieceColor, to.row, to.col, 5, Board.imagePaths.get(map));
            }
            case BISHOP -> {
                map.put(BISHOP, pieceColor);
                piece = new Piece(BISHOP, pieceColor, to.row, to.col, 3, Board.imagePaths.get(map));
            }
            case KNIGHT -> {
                map.put(KNIGHT, pieceColor);
                piece = new Piece(KNIGHT, pieceColor, to.row, to.col, 3, Board.imagePaths.get(map));
            }
        }

        return piece;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Color myColor = new Color(174, 186, 252);
        Object source = e.getSource();

        if (source.equals(chooseButton)) {
            if (pieceType != null) {
                Board.whitePieces.remove(to.piece);

                Piece piece = getPieceFromType();
                to.addPiece(piece);
                Board.whitePieces.add(piece);
                Main.frame.setEnabled(true);
                this.dispose();
                SoundEffects.playSound(PROMOTE);
                return;
            } else
                JOptionPane.showMessageDialog(this, "Choose an option!", "Error", JOptionPane.ERROR_MESSAGE);
        } if (source.equals(queenButton)) {
            queenButton.setBackground(myColor);
            rookButton.setBackground(null);
            bishopButton.setBackground(null);
            knightButton.setBackground(null);
            pieceType = QUEEN;
        } else if (source.equals(rookButton)) {
            queenButton.setBackground(null);
            rookButton.setBackground(myColor);
            bishopButton.setBackground(null);
            knightButton.setBackground(null);
            pieceType = ROOK;
        } else if (source.equals(bishopButton)) {
            queenButton.setBackground(null);
            rookButton.setBackground(null);
            bishopButton.setBackground(myColor);
            knightButton.setBackground(null);
            pieceType = BISHOP;
        } else if (source.equals(knightButton)) {
            queenButton.setBackground(null);
            rookButton.setBackground(null);
            bishopButton.setBackground(null);
            knightButton.setBackground(myColor);
            pieceType = KNIGHT;
        }

        SoundEffects.playSound(CLICK);
    }
}
