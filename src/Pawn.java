import Enums.*;
import java.util.Map;
import static Enums.Type.*;
import static Enums.Color.*;
import static Enums.EnPassant.*;

public class Pawn extends Piece {

    private EnPassant leftEnPassant;
    private EnPassant rightEnPassant;

    public Pawn(Color pieceColor) {
        super(PAWN, pieceColor);
    }

    public Pawn(Color pieceColor, int col) {
        super(PAWN,
                pieceColor,
                col,
                (pieceColor.equals(WHITE) ? 1 : -1),
                Board.imagePaths.get(Map.of(PAWN, pieceColor)));
        directions = pieceColor.equals(WHITE) ?
                (new int[][] {
                        {-1, 0}, {-2, 0},
                        {-1, 1}, {-1, -1}
                }) :
                (new int[][] {
                        {1, 0}, {2, 0},
                        {1, 1}, {1, -1}
                });
        leftEnPassant = rightEnPassant = NO;
    }

    @Override
    public EnPassant getLeftEnPassant() {
        return leftEnPassant;
    }

    public void setLeftEnPassant(EnPassant leftEnPassant) {
        this.leftEnPassant = leftEnPassant;
    }

    @Override
    public EnPassant getRightEnPassant() {
        return rightEnPassant;
    }

    public void setRightEnPassant(EnPassant rightEnPassant) {
        this.rightEnPassant = rightEnPassant;
    }
}