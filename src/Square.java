import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static Constants.CONST.TEMP;
import static Enums.SoundType.ILLEGAL;

public class Square extends JButton implements ActionListener {
    public Piece piece;
    public Color color;
    public int row;
    public int col;

    public Square(Color color, int row, int col) {
        piece = null;
        this.color = color;
        this.row = row;
        this.col = col;
        this.setBackground(color);
        this.addActionListener(this);
    }

    public void addPiece(Piece piece) {
        if (piece == null) {
            this.piece = null;
            addPieceImage(null);
            return;
        }

        this.piece = piece;
        addPieceImage(piece.imagePath);
    }

    public void addPieceImage(String imagePath) {
        this.setIcon(new ImageIcon(imagePath));
    }

    public boolean isTeamPiece(Piece piece) {
        return this.piece.pieceColor.equals(piece.pieceColor);
    }

    public boolean isEmpty() {
        return piece == null;
    }

    /*
    This method handles the case when the player
    has already clicked the piece that they want
    to move and have know chosen to move that piece
    to one of its valid squares.
     */
    private boolean checkMoveOrCaptureConditions() {
        return Board.pieceToMove != null &&
               Board.pieceToMove.canReach(this.row, this.col) &&
               (this.isEmpty() || (!this.isEmpty() &&
               !Board.pieceToMove.pieceColor.equals(this.piece.pieceColor))) &&
               (this.getBackground().equals(Board.themes.get(Board.themeIndex)[2]) ||
               this.getBackground().equals(TEMP));
    }

    private boolean checkValidMovesConditions() {
        return this.piece != null &&
               Board.playerTurn.equals(this.piece.pieceColor);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (checkValidMovesConditions()) {
            Board.startingSquare = this;
            Board.pieceToMove = this.piece;

            Board.refresh();

            for (int[] valid : piece.validMoves) {
                Square square = Board.getSquare(valid[0], valid[1]);
                if (!square.isEmpty() && !square.isTeamPiece(piece))
                    square.setBackground(TEMP);
                else
                    square.setBackground(Board.themes.get(Board.themeIndex)[2]);
            }
        } else if (checkMoveOrCaptureConditions()) {
            Board.movePiece(this);
            Board.calculateMoves();
            Board.update();
        } else {
            Board.startingSquare = null;
            Board.pieceToMove = null;
            Board.refresh();
            SoundEffects.playSound(ILLEGAL);
        }
    }
}
