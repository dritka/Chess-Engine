import Enums.*;
import java.util.List;
import java.util.ArrayList;

import static Enums.Color.*;
import static Enums.Type.*;

public class Piece {
    public int row;
    public int col;
    public int value;
    public int numOfMoves;
    public Type pieceType;
    public String imagePath;
    public Color pieceColor;
    public int[] lastPosition;
    public int[][] directions;
    public List<int[]> validMoves;

    public Piece(Type pieceType, Color pieceColor) {
        this.pieceType = pieceType;
        this.pieceColor = pieceColor;
    }

    public Piece(Type pieceType, Color pieceColor, int col, int value, String imagePath) {
        this(pieceType, pieceColor);
        this.row = pieceType.equals(PAWN) ?
                (pieceColor.equals(WHITE) ? 6 : 1) :
                (pieceColor.equals(WHITE) ? 7 : 0);
        this.col = col;
        this.value = value;
        this.imagePath = imagePath;
        numOfMoves = 0;
        validMoves = new ArrayList<>();
    }

    public boolean sameColor(Enums.Color pieceColor) {
        return this.pieceColor.equals(pieceColor);
    }

    public boolean sameType(Type pieceType) {
        return this.pieceType.equals(pieceType);
    }

    public boolean canReach(int row, int col) {
        for (int[] valid : validMoves)
            if (valid[0] == row && valid[1] == col) return true;
        return false;
    }

    public void setLastPosition() {
        lastPosition = new int[] {row, col};
    }

    public void addValidMove(int row, int col) {
        validMoves.add(new int[]{row, col});
    }

    public void clearValidMoves() {
        validMoves.clear();
    }

    public void update(Square square, boolean reverse) {
        row = square.row;
        col = square.col;
        numOfMoves += reverse ? -1 : 1;
        square.addPiece(this);
    }
}