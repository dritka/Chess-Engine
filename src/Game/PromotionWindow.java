package Game;

import java.awt.*;
import java.util.Map;
import javax.swing.*;
import java.util.HashMap;
import static Enums.Type.*;
import static Enums.SoundType.*;

public class PromotionWindow extends JFrame {
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

        queenButton.addActionListener(e -> {
            adjust();
            pieceType = QUEEN;
            SoundEffects.playSound(CLICK);
        });
        rookButton.addActionListener(e -> {
            adjust();
            pieceType = ROOK;
            SoundEffects.playSound(CLICK);
        });
        knightButton.addActionListener(e -> {
            adjust();
            pieceType = KNIGHT;
            SoundEffects.playSound(CLICK);
        });
        bishopButton.addActionListener(e -> {
            adjust();
            pieceType = BISHOP;
            SoundEffects.playSound(CLICK);
        });
        chooseButton.addActionListener(e -> {
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
        });

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
        Piece piece = switch (pieceType) {
            case QUEEN -> new Piece(QUEEN, pieceColor);
            case ROOK -> new Piece(ROOK, pieceColor);
            case BISHOP -> new Piece(BISHOP, pieceColor);
            case PAWN -> null; // should never occur
            case KNIGHT -> new Piece(KNIGHT, pieceColor);
            case KING -> null; // should never occur
        };

        return piece;
    }

    public void adjust() {
        java.util.List<JButton> buttons = java.util.List.of(queenButton, rookButton, bishopButton, knightButton);
        JButton exclude = switch (pieceType) {
            case QUEEN -> queenButton;
            case ROOK -> rookButton;
            case BISHOP -> bishopButton;
            case PAWN -> null; // should never occur
            case KNIGHT -> knightButton;
            case KING -> null; // should never occur
        };

        /*
        Another alternative is to do
        Objects.requireNoNull(
            exclude.setBackground(new Color(174, 186, 252))
        );
         */
        if (exclude != null)
            exclude.setBackground(new Color(174, 186, 252));
        buttons.stream()
                .filter(button -> !button.equals(exclude))
                .forEach(button -> button.setBackground(null));
    }
}
