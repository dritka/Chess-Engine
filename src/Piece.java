import Enums.*;
import java.util.List;
import java.util.ArrayList;

import static Enums.EnPassant.NO;

public class Piece {
    public Type pieceType;
    public String imagePath;
    public Color pieceColor;
    public List<int[]> validMoves;
    public EnPassant leftEnPassant;
    public EnPassant rightEnPassant;

    public int row;
    public int col;
    public int value;
    public int numOfMoves;
    public int[][] directions;
    public boolean doneCastled;

    private static final int[][] DIAGONAL_DIRECTIONS = {{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
    private static final int[][] STRAIGHT_DIRECTIONS = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
    private static final int[][] ALL_DIRECTIONS = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};

    public Piece(Type pieceType, Color pieceColor) {
        this.pieceType = pieceType;
        this.pieceColor = pieceColor;
    }

    public Piece(Type pieceType, Color pieceColor, int row, int col, int value, String imagePath) {
        this(pieceType, pieceColor);
        this.row = row;
        this.col = col;
        this.value = value;
        this.numOfMoves = 0;
        this.imagePath = imagePath;
        validMoves = new ArrayList<>();
        directions = calculateDirections();
        doneCastled = false;

        leftEnPassant = NO;
        rightEnPassant = NO;
    }

    public int[][] calculateDirections() {
        switch (pieceType) {
            // Refactor
            case PAWN -> {
                switch (pieceColor) {
                    case WHITE -> {
                        return new int[][]{{-1, 0}, {-2, 0}, {-1, 1}, {-1, -1}};
                    }
                    case BLACK -> {
                        return new int[][]{{1, 0}, {2, 0}, {1, 1}, {1, -1}};
                    }
                }
            }

            case KNIGHT -> {
                return new int[][]{{2, 1}, {2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}, {-2, 1}, {-2, -1}};
            }

            case BISHOP -> {
                return DIAGONAL_DIRECTIONS;
            }

            case ROOK -> {
                return STRAIGHT_DIRECTIONS;
            }

            case QUEEN, KING -> {
                return ALL_DIRECTIONS;
            }

        }

        return new int[0][];
    }

    public boolean canReach(int row, int col) {
        for (int[] valid : validMoves)
            if (valid[0] == row && valid[1] == col) return true;
        return false;
    }

    public void addValidMove(int row, int col) {
        validMoves.add(new int[]{row, col});
    }

    public void clearValidMoves() {
        validMoves.clear();
    }

    public void update(Square square) {
        this.row = square.row;
        this.col = square.col;
        this.numOfMoves += 1;
    }

    // For tracking moves
    @Override
    public String toString() {
        return this.pieceColor.toString() + " moved " + this.pieceType.toString() + " to row: " + this.row + ", col: " + this.col;
    }
}