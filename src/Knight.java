import Enums.*;

import java.util.Map;

import static Enums.Type.*;
import static Enums.Color.*;

public class Knight extends Piece {

    public Knight(Color pieceColor) {
        super(KNIGHT, pieceColor);
    }

    public Knight(Color pieceColor, boolean left) {
        super(KNIGHT,
                pieceColor,
                (left ? 1 : 6),
                (pieceColor.equals(WHITE) ? 3 : -3),
                Board.imagePaths.get(Map.of(KNIGHT, pieceColor)));
        directions = new int[][] {
                {2, 1}, {2, -1}, {1, 2},
                {1, -2}, {-1, 2}, {-1, -2},
                {-2, 1}, {-2, -1}
        };
    }
}