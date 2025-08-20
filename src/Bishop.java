import Enums.*;

import java.util.Map;

import static Enums.Type.*;
import static Enums.Color.*;

public class Bishop extends Piece {

    public Bishop(Color pieceColor) {
        super(BISHOP, pieceColor);
    }

    public Bishop(Color pieceColor, boolean left) {
        super(BISHOP,
                pieceColor,
                (left ? 2 : 5),
                (pieceColor.equals(WHITE) ? 3 : -3),
                Board.imagePaths.get(Map.of(BISHOP, pieceColor)));
        directions = new int[][] {
                {1, 1}, {-1, 1},
                {-1, -1}, {1, -1}
        };
    }
}
