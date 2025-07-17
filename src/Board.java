import Enums.Type;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.util.List;
import Enums.CastleType;
import java.awt.event.*;
import static Enums.Type.*;
import static Enums.Color.*;
import static Enums.EnPassant.*;
import static Enums.SoundType.*;
import static Constants.CONST.*;
import static Enums.CastleType.*;

public class Board extends JPanel {
    public static Color[] theme;
    public static int themeIndex;
    public static Square[][] board;
    public static Piece pieceToMove;
    public static List<Color[]> themes;
    public static Square startingSquare;
    public static Enums.Color playerTurn;
    public static List<Piece> whitePieces;
    public static List<Piece> blackPieces;
    public static Map<Map<Type, Enums.Color>, String> imagePaths;

    public static int enPassantType; // -1 means left, 0 means the move is not en passant, 1 means right

    public Board() {
        PreProcess.loadThemes();
        PreProcess.loadImages();

        playerTurn = WHITE;
        board = new Square[ROWS][COLS];
        whitePieces = new ArrayList<>();
        blackPieces = new ArrayList<>();
        pieceToMove = null;
        startingSquare = null;
        themeIndex = 0;
        theme = themes.get(themeIndex);

        this.setPreferredSize(new Dimension(CONST_WIDTH, CONST_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setLayout(new GridLayout(ROWS, COLS));
        this.setFocusable(true);

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('t'), "t");
        this.getActionMap().put("t", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeTheme();
            }
        });

        setup();
    }

    // -- Start (Setup Code)
    private void setup() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Square square = squareSetup(row, col);
                board[row][col] = square;
                this.add(square);
            }
        }

        setupPawns(WHITE, 6);
        setupBackRank(WHITE, 7);

        setupPawns(BLACK, 1);
        setupBackRank(BLACK, 0);

        calculateMoves();
    }

    private void setupPawns(Enums.Color color, int row) {
        for (int col = 0; col < COLS; col++) {
            Piece piece = new Piece(PAWN, color, row, col, color.equals(WHITE) ? 1 : -1, imagePaths.get(Map.of(PAWN, color)));
            addPiece(piece);
        }
    }

    private void setupBackRank(Enums.Color color, int row) {
        int value = 3;
        int kingValue = Integer.MAX_VALUE;

        Object[][] layout = {
                {ROOK,   0, 5},
                {KNIGHT, 1, 3},
                {BISHOP, 2, 3},
                {QUEEN,  3, 9},
                {KING,   4, kingValue},
                {BISHOP, 5, 3},
                {KNIGHT, 6, 3},
                {ROOK,   7, 5},
        };

        for (Object[] entry : layout) {
            Type type = (Type) entry[0];
            int col = (int) entry[1];
            int pieceValue = (int) entry[2];
            addPiece(new Piece(type, color, row, col, color.equals(WHITE) ? pieceValue : (-1 * pieceValue), imagePaths.get(Map.of(type, color))));
        }
    }

    private void addPiece(Piece piece) {
        Square square = board[piece.row][piece.col];
        square.addPiece(piece);

        if (piece.pieceColor.equals(BLACK))
            blackPieces.add(piece);
        else
            whitePieces.add(piece);
    }

    private static Square squareSetup(int row, int col) {
        Square square;

        if (row % 2 == 0) {
            if (col % 2 == 0)
                square = new Square(theme[1], row, col);
            else
                square = new Square(theme[0], row, col);
        } else {
            if (col % 2 == 0)
                square = new Square(theme[0], row, col);
            else
                square = new Square(theme[1], row, col);
        }

        return square;
    }

    // -- End

    // -- Start (Helper methods)
    private static Square getSquare(int row, int col) {
        return board[row][col];
    }

    private static Piece getPiece(Type type, Enums.Color color) {
        List<Piece> pieces = color.equals(WHITE) ? whitePieces : blackPieces;
        for (Piece piece : whitePieces)
            if (piece.pieceType.equals(type) && piece.pieceColor.equals(color)) return piece;
        return null;
    }

    private static void exploreDiagonalMoves(Piece piece, Enums.Color color, int[] direction) {
        int newRow = piece.row + direction[0];
        int newCol = piece.col + direction[1];
        if (isValidMove(newRow, newCol, color) && !getSquare(newRow, newCol).isEmpty())
            piece.addValidMove(newRow, newCol);
    }

    private static boolean isInBounds(int row, int col) {
        return (row >= 0 && row < ROWS) && (col >= 0 && col < COLS);
    }

    private static boolean isValidSquare(int row, int col, Enums.Color color) {
        Square square = getSquare(row, col);
        return square.isEmpty() ||
                (!square.isEmpty() &&
                        !square.piece.pieceColor.equals(color));
    }

    private static boolean isValidMove(int row, int col, Enums.Color color) {
        return isInBounds(row, col) && isValidSquare(row, col, color);
    }

    private static int isEnPassant(Square to) {
        if (pieceToMove.leftEnPassant.equals(YES) && pieceToMove.pieceType.equals(PAWN)) return -1;
        else if (pieceToMove.rightEnPassant.equals(YES) && pieceToMove.pieceType.equals(PAWN)) return 1;
        return 0;
    }

    private static boolean canPromote(Piece piece) {
        if (!piece.pieceType.equals(PAWN)) return false;
        return ((piece.pieceColor.equals(WHITE) && piece.row == 0) || (piece.pieceColor.equals(BLACK) && piece.row == 7));
    }

    private static boolean getCastlingType(Square to) {
        return (to.row == 0 && to.col == 6) || (to.row == 7 && to.col == 6);
    }

    public static boolean isCastling(Square to) {
        Piece piece = pieceToMove;
        return piece.pieceType.equals(KING) &&
                ((to.row == 0 && to.col == 6) ||
                        (to.row == 0 && to.col == 2) ||
                        (to.row == 7 && to.col == 6) ||
                        to.row == 7 && to.col == 2);
    }

    private static boolean checkQueenSideCastlingConditions(Square kingSquare,
                                                            Square rookSquare,
                                                            Square inBetweenFirst,
                                                            Square inBetweenSecond,
                                                            Square inBetweenThird) {
        return kingSquare.piece.numOfMoves == 0 &&
                rookSquare.piece.numOfMoves == 0 &&
                inBetweenFirst.isEmpty() &&
                inBetweenSecond.isEmpty() &&
                inBetweenThird.isEmpty();
    }

    private static boolean checkKingSideCastlingConditions(Square kingSquare,
                                                           Square rookSquare,
                                                           Square inBetweenFirst,
                                                           Square inBetweenSecond) {
        return kingSquare.piece.numOfMoves == 0 &&
                rookSquare.piece.numOfMoves == 0 &&
                inBetweenFirst.isEmpty() &&
                inBetweenSecond.isEmpty();
    }

    private static void checkForEnPassant(Piece piece, Enums.Color pieceColor, boolean left) {
        int col = pieceColor.equals(WHITE) ?
                (left ? piece.col - 1 : piece.col + 1) :
                (left ? piece.col + 1 : piece.col - 1);
        if (isInBounds(piece.row, col)) {
            Square square = getSquare(piece.row, col);
            check(piece, pieceColor, square, left);
        }
    }

    private static void check(Piece piece, Enums.Color pieceColor, Square square, boolean left) {
        if (square != null && !square.isEmpty() && !square.isTeamPiece(piece) &&
                square.piece.pieceType.equals(PAWN) && square.piece.numOfMoves == 1) {

            if (left)
                piece.leftEnPassant = YES;
            else
                piece.rightEnPassant = YES;

            if (pieceColor.equals(WHITE))
                piece.addValidMove(square.row - 1, square.col);
            else
                piece.addValidMove(square.row + 1, square.col);
        }
    }

    // -- End

    // -- Start (Functionality)
    public static void calculateMoves() {
        for (Piece piece : whitePieces)
            calculate(piece, WHITE);

        for (Piece piece : blackPieces)
            calculate(piece, BLACK);
    }

    public static void calculate(Piece piece, Enums.Color color) {
        piece.clearValidMoves();
        int[][] directions = piece.directions;

        switch (piece.pieceType) {
            case PAWN -> {
                int[] direction = directions[0];
                int newRow = piece.row + direction[0];
                int newCol = piece.col + direction[1];
                if (isValidMove(newRow, newCol, color) && getSquare(newRow, newCol).isEmpty()) {
                    piece.addValidMove(newRow, newCol);

                    direction = directions[1];
                    newRow = piece.row + direction[0];
                    newCol = piece.col + direction[1];
                    if (piece.numOfMoves == 0 && isInBounds(newRow, newCol) && getSquare(newRow, newCol).isEmpty())
                        piece.addValidMove(newRow, newCol);
                }

                exploreDiagonalMoves(piece, color, directions[2]);
                exploreDiagonalMoves(piece, color, directions[3]);

                if (piece.leftEnPassant.equals(NO)) checkForEnPassant(piece, color, true);
                if (piece.rightEnPassant.equals(NO)) checkForEnPassant(piece, color, false);
            }

            case KING, KNIGHT -> {
                if (piece.pieceType.equals(KING) && !piece.doneCastled) checkForCastle(color);

                for (int[] dir : directions) {
                    int newRow = piece.row + dir[0];
                    int newCol = piece.col + dir[1];

                    if (isValidMove(newRow, newCol, color))
                        piece.addValidMove(newRow, newCol);
                }
            }

            case BISHOP, ROOK, QUEEN -> {
                for (int[] dir : directions) {
                    int multiplier = 1;

                    while (isInBounds(piece.row + dir[0] * multiplier, piece.col + dir[1] * multiplier)) {
                        int row = piece.row + dir[0] * multiplier;
                        int col = piece.col + dir[1] * multiplier;
                        Square square = getSquare(row, col);
                        if (!square.isEmpty()) break;
                        piece.addValidMove(row, col);
                        multiplier++;
                    }

                    int row = piece.row + dir[0] * multiplier;
                    int col = piece.col + dir[1] * multiplier;
                    if (isValidMove(row, col, color))
                        piece.addValidMove(row, col);
                }
            }
        }
    }

    public static void movePiece(Square to) {
        Square from = startingSquare;
        boolean isEmpty = to.isEmpty();

        startingSquare.addPiece(null);
        pieceToMove.update(to);
        Piece piece = to.piece;
        to.addPiece(pieceToMove);
        removePiece(piece);
        checkCasesAfterMove(to, isEmpty);
    }

    private static void checkCasesAfterMove(Square to, boolean isEmpty) {
        boolean isCastling = isCastling(to);

        // TO DO: Clean Up
        int code;
        if (Math.abs(code = isEnPassant(to)) == 1) {
            Square square = null;
            switch (pieceToMove.pieceColor) {
                case WHITE -> square = getSquare(to.row + 1, to.col);
                case BLACK -> square = getSquare(to.row - 1, to.col);
            }

            if (code == 1)
                Board.pieceToMove.rightEnPassant = NOT_ALLOWED;
            else
                Board.pieceToMove.leftEnPassant = NOT_ALLOWED;

            if (square != null) square.addPiece(null);
            SoundEffects.playSound(CAPTURE);
        }

        if (canPromote(pieceToMove)) {
            Main.frame.setEnabled(false);
            new PromotionWindow(playerTurn, to);
        }

        if (isCastling) {
            pieceToMove.doneCastled = true;
            castle(getCastlingType(to) ? KING_SIDE : QUEEN_SIDE);
        }

        if (isCheck(to)) {
            SoundEffects.playSound(MOVE_CHECK);
        } else {
            if (isEmpty)
                SoundEffects.playSound(MOVE);
            else
                SoundEffects.playSound(CAPTURE);
        }
    }

    public static void update() {
        playerTurn = playerTurn.equals(WHITE) ? BLACK : WHITE;
        refresh();
    }

    private static void removePiece(Piece piece) {
        if (piece == null) return;

        if (Board.playerTurn.equals(WHITE))
            Board.blackPieces.remove(piece);
        else
            Board.whitePieces.remove(piece);
    }

    private static boolean isCheck(Square from) {
        for (int[] move : from.piece.validMoves) {
            Square to = getSquare(move[0], move[1]);
            if (!to.isEmpty() &&
                    to.piece.pieceType.equals(KING) &&
                    !to.isTeamPiece(from.piece)) return true;
        }

        return false;
    }

    private static void castle(CastleType type) {
        Square rookSquare = null;
        Square to = null;
        switch (type) {
            case KING_SIDE -> {
                if (playerTurn.equals(WHITE)) {
                    rookSquare = getSquare(7, 7);
                    to = getSquare(7, 5);
                } else {
                    rookSquare = getSquare(0, 7);
                    to = getSquare(0, 5);
                }
            }

            case QUEEN_SIDE -> {
                if (playerTurn.equals(WHITE)) {
                    rookSquare = getSquare(7, 0);
                    to = getSquare(7, 3);
                } else {
                    rookSquare = getSquare(0, 0);
                    to = getSquare(0, 3);
                }
            }
        }

        startingSquare = rookSquare;
        pieceToMove = rookSquare.piece;
        movePiece(to);
        SoundEffects.playSound(CASTLE);
    }

    // TO DO (?): Clean Up
    private static void checkForCastle(Enums.Color pieceColor) {
        Square firstRookSquare;
        Square secondRookSquare;
        Square kingSquare;

        if (pieceColor.equals(BLACK)) {
            firstRookSquare = getSquare(0, 0);
            secondRookSquare = getSquare(0, 7);
            kingSquare = getSquare(0, 4);

            if (firstRookSquare.isEmpty() ||
                    secondRookSquare.isEmpty() ||
                    kingSquare.isEmpty()) return;

            // Get the squares in between
            Square inBetweenFirst = getSquare(0, 1);
            Square inBetweenSecond = getSquare(0, 2);
            Square inBetweenThird = getSquare(0, 3);
            Square inBetweenFourth = getSquare(0, 5);
            Square inBetweenFifth = getSquare(0, 6);

            boolean canCastleQueenSide = checkQueenSideCastlingConditions(kingSquare,
                    firstRookSquare, inBetweenFirst, inBetweenSecond, inBetweenThird);
            boolean canCastleKingSide = checkKingSideCastlingConditions(kingSquare,
                    secondRookSquare, inBetweenFourth, inBetweenFifth);

            if (canCastleKingSide)
                kingSquare.piece.addValidMove(0, 6);
            else if (canCastleQueenSide)
                kingSquare.piece.addValidMove(0, 2);
        } else {
            firstRookSquare = getSquare(7, 0);
            secondRookSquare = getSquare(7, 7);
            kingSquare = getSquare(7, 4);

            if (firstRookSquare.isEmpty() ||
                    secondRookSquare.isEmpty() ||
                    kingSquare.isEmpty()) return;

            // Get the squares in between
            Square inBetweenFirst = getSquare(7, 1);
            Square inBetweenSecond = getSquare(7, 2);
            Square inBetweenThird = getSquare(7, 3);
            Square inBetweenFourth = getSquare(7, 5);
            Square inBetweenFifth = getSquare(7, 6);

            boolean canCastleQueenSide = checkQueenSideCastlingConditions(kingSquare,
                    firstRookSquare, inBetweenFirst, inBetweenSecond, inBetweenThird);
            boolean canCastleKingSide = checkKingSideCastlingConditions(kingSquare,
                    secondRookSquare, inBetweenFourth, inBetweenFifth);

            if (canCastleKingSide)
                kingSquare.piece.addValidMove(7, 6);
            else if (canCastleQueenSide)
                kingSquare.piece.addValidMove(7, 2);
        }
    }

    // -- End

    // -- Start (Aesthetics/GUI Related)

    /*
    This method reset the colors of the chessboard to their
    original states after a move is made or when the player
    clicks another piece/empty square.
     */
    public static void refresh() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Square square = Board.board[row][col];
                Piece piece = square.piece;
                Color color = square.color;

                if (piece != null) {
                    square.addPieceImage(piece.imagePath);
                } else {
                    square.addPieceImage(null);
                }

                square.setBackground(color);
            }
        }
    }

    private void changeTheme() {
        themeIndex++;
        if (themeIndex == 7) themeIndex = 0;
        theme = themes.get(themeIndex);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Square square = board[row][col];

                if (row % 2 == 0) {
                    if (col % 2 == 0) {
                        square.setBackground(theme[1]);
                        square.color = theme[1];
                    } else {
                        square.setBackground(theme[0]);
                        square.color = theme[0];
                    }
                } else {
                    if (col % 2 == 0) {
                        square.setBackground(theme[0]);
                        square.color = theme[0];
                    } else {
                        square.setBackground(theme[1]);
                        square.color = theme[1];
                    }
                }
            }
        }
    }

    // -- End
}