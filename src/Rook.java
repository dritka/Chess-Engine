import Enums.*;
import java.util.Map;
import static Enums.Type.*;
import static Enums.Color.*;

public class Rook extends Piece {

    public Rook(Color pieceColor) {
        super(ROOK, pieceColor);
    }

    public Rook(Color pieceColor, boolean left) {
        super(PAWN,
                pieceColor,
                (left ? 0 : 7),
                (pieceColor.equals(WHITE) ? 5 : -5),
                Board.imagePaths.get(Map.of(ROOK, pieceColor)));
        directions = new int[][] {
                {1, 0}, {0, 1},
                {-1, 0}, {0, -1}
        };
    }
}
