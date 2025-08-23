import Enums.*;

import java.util.Map;

import static Enums.Type.*;
import static Enums.Color.*;

public class King extends Piece {
    private boolean castledStatus;

    /*
    public King(Color pieceColor) {
        super(KING, pieceColor);
    }
     */

    public King(Color pieceColor) {
        super(KING,
                pieceColor,
                4,
                (pieceColor.equals(WHITE) ? Integer.MAX_VALUE : Integer.MIN_VALUE),
                Board.imagePaths.get(Map.of(KING, pieceColor)));
        castledStatus = false;
        directions = new int[][]{
                {1, 0}, {1, 1}, {0, 1},
                {-1, 1}, {-1, 0}, {-1, -1},
                {0, -1}, {1, -1}
        };
    }

    public boolean getCastledStatus() {
        return castledStatus;
    }

    public void setCastledStatus(boolean castledStatus) {
        this.castledStatus = castledStatus;
    }
}
