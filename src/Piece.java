import Enums.Color;
import Enums.EnPassant;
import Enums.Type;

import java.util.ArrayList;
import java.util.List;

import static Enums.EnPassant.NO;

public class Piece {
    public Type pieceType;
    public Color pieceColor;
    public String imagePath;
    public List<int[]> validMoves;

    public EnPassant leftEnPassant;
    public EnPassant rightEnPassant;

    public int row;
    public int col;
    public int value;
    public int moves;
    public int[][] directions;
    public boolean doneCastled;

    public Piece(Type pieceType, Color pieceColor) {
        this.pieceType = pieceType;
        this.pieceColor = pieceColor;
    }

    public Piece(Type pieceType, Color pieceColor, int row, int col, int value, String imagePath) {
        this(pieceType, pieceColor);
        this.row = row;
        this.col = col;
        this.value = value;
        this.moves = 0;
        this.imagePath = imagePath;
        validMoves = new ArrayList<>();
        directions = calculateDirections();
        doneCastled = false;

        leftEnPassant = NO;
        rightEnPassant = NO;
    }

    public int[][] calculateDirections() {
        switch (pieceType) {
            case PAWN -> {
                switch (pieceColor) {
                    case WHITE -> { return new int[][] { {-1, 0}, {-2, 0}, {-1, 1}, {-1, -1} }; }
                    case BLACK -> { return new int[][] { {1, 0}, {2, 0}, {1, 1}, {1, -1}}; }
                }
            }

            case KNIGHT -> {
                return new int[][]{{2, 1}, {2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}, {-2, 1}, {-2, -1}};
            }

            case BISHOP -> {
                return new int[][]{{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
            }

            case ROOK -> {
                return new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            }

            case QUEEN, KING -> {
                return new int[][]{{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
            }

        }

        return null;
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
        this.moves += 1;
    }
}