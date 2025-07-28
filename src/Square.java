import Enums.Type;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.util.List;
import java.awt.event.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static Enums.Color.BLACK;
import static Enums.Color.WHITE;
import static Enums.SoundType.*;
import static Constants.CONST.*;
import static Enums.Type.KING;

public class Square extends JButton implements ActionListener {
    /*
     TO DO: Implement getters and setters for all fields except dependentPieces (only getter)
     and also set the respective fields to private
     */
    public Piece piece;
    public Color color;
    public int row;
    public int col;
    private final Set<Piece> dependentPieces;
    private final Map<Color, Map<Type, List<int[]>>> cache;

    public Square(Color color, int row, int col) {
        piece = null;
        this.color = color;
        this.row = row;
        this.col = col;
        dependentPieces = new HashSet<>();
        cache = new HashMap<>();
        this.setBackground(color);
        this.addActionListener(this);
    }

    // Getters and Setters
    public Piece getPiece() {
        return this.piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getRow() {
        return this.row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return this.col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public Set<Piece> getDependentPieces() {
        return this.dependentPieces;
    }

    public void addDependent(Piece piece) {
        this.dependentPieces.add(piece);
    }

    public void removeDependent(Piece piece) {
        this.dependentPieces.remove(piece);
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
        Path path;
        if (imagePath != null) path = Paths.get(imagePath);
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
               Board.pieceToMove.canReach(this.row, this.col);
    }

    private boolean checkValidMovesConditions() {
        return this.piece != null && Board.playerTurn.equals(this.piece.pieceColor);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (checkValidMovesConditions()) {
            Board.startingSquare = this;
            Board.pieceToMove = this.piece;

            Board.refresh();

            for (int[] valid : piece.validMoves) {
                Square square = Board.board[valid[0]][valid[1]];
                if (!square.isEmpty() && !square.isTeamPiece(piece))
                    square.setBackground(TEMP);
                else
                    square.addPieceImage("./images/dot.png");
            }
        } else if (checkMoveOrCaptureConditions()) {
            Board.movePiece(this);
            Board.count = 0; // for logging purposes
            Board.calculate(Board.pieceToMove);

            List<Piece> pieces = new ArrayList<>(Board.startingSquare.getDependentPieces());
            for (Piece dependent : pieces) {
                if (dependent.equals(Board.pieceToMove)) continue;
                Board.calculate(dependent);
            }

            pieces = new ArrayList<>(this.getDependentPieces());
            for (Piece dependent : pieces) {
                if (dependent.equals(Board.pieceToMove)) continue;
                Board.calculate(dependent);
            }

            Piece king = Board.pieceMap.get(WHITE).get(KING).get(0);
            Board.calculate(king);
            if (!king.doneCastled) Board.checkForCastle(king);

            king = Board.pieceMap.get(BLACK).get(KING).get(0);
            Board.calculate(king);
            if (!king.doneCastled) Board.checkForCastle(king);

            System.out.printf("The calculateMoves() function was called a total of %d times this turn!%n", Board.count); // again for logging purposes
            Board.update();
        } else {
            Board.startingSquare = null;
            Board.pieceToMove = null;
            Board.refresh();
            SoundEffects.playSound(ILLEGAL);
        }
    }
}
