import Enums.*;
import java.util.Map;
import static Enums.Color.*;
import static Enums.Type.*;

public class Queen extends Piece {

    /*
    public Queen(Color pieceColor) {
        super(QUEEN, pieceColor);
    }
     */

    public Queen(Color pieceColor) {
        super(QUEEN,
                pieceColor,
                3,
                (pieceColor.equals(WHITE) ? 8 : -8),
                Board.imagePaths.get(Map.of(QUEEN, pieceColor)));
        directions = new int[][] {
                {1, 0}, {1, 1}, {0, 1},
                {-1, 1}, {-1, 0}, {-1, -1},
                {0, -1}, {1, -1}
        };
    }
}
