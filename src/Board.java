import Enums.Direction;
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

    public static int count = 0;
    public static Map<Enums.Color, Map<Type, List<Piece>>> pieceMap;

    // public static int enPassantType; // -1 means left, 0 means the move is not en passant, 1 means right

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

        Piece whiteKing = pieceMap.get(WHITE).get(KING).get(0);
        System.out.println(whiteKing);
        System.out.println(getSquare(whiteKing.row, whiteKing.col + 2).piece);
    }

    // -- Start (Setup Code)
    private void setup() {
        pieceMap = new HashMap<>();

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
        int kingValue = Integer.MAX_VALUE;

        Object[][] layout = {
                {ROOK, 0, 5},
                {KNIGHT, 1, 3},
                {BISHOP, 2, 3},
                {QUEEN, 3, 9},
                {KING, 4, kingValue},
                {BISHOP, 5, 3},
                {KNIGHT, 6, 3},
                {ROOK, 7, 5},
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

        pieceMap.computeIfAbsent(piece.pieceColor, k -> new HashMap<>())
                .computeIfAbsent(piece.pieceType, k -> new ArrayList<>())
                .add(piece);
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

    private static void exploreDiagonalMoves(Piece piece, int[] direction) {
        int newRow = piece.row + direction[0];
        int newCol = piece.col + direction[1];
        if (isInBounds(newRow, newCol)) {
            Square square = getSquare(newRow, newCol);
            if (!square.isEmpty() && !square.piece.pieceColor.equals(piece.pieceColor)) piece.addValidMove(newRow, newCol);
            square.addDependent(piece);
        }
    }

    private static boolean isInBounds(int row, int col) {
        return (row >= 0 && row < ROWS) && (col >= 0 && col < COLS);
    }

    private static boolean isValidSquare(Square square, Enums.Color color) {
        return square.isEmpty() ||
                (!square.isEmpty() &&
                        !square.piece.pieceColor.equals(color));
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

    private static boolean isCastling(Square to) {
        Piece piece = pieceToMove;
        return piece.pieceType.equals(KING) &&
                ((to.row == 0 && to.col == 6) ||
                        (to.row == 0 && to.col == 2) ||
                        (to.row == 7 && to.col == 6) ||
                        to.row == 7 && to.col == 2);
    }

    private static void checkForEnPassant(Piece piece, boolean left) {
        int col = piece.pieceColor.equals(WHITE) ?
                (left ? piece.col - 1 : piece.col + 1) :
                (left ? piece.col + 1 : piece.col - 1);
        if (isInBounds(piece.row, col)) {
            Square square = getSquare(piece.row, col);
            check(piece, square, left);
        }
    }

    private static void check(Piece piece, Square square, boolean left) {
        if (square != null && !square.isEmpty() && !square.isTeamPiece(piece) &&
                square.piece.pieceType.equals(PAWN) && square.piece.numOfMoves == 1) {

            if (left)
                piece.leftEnPassant = YES;
            else
                piece.rightEnPassant = YES;

            if (piece.pieceColor.equals(WHITE)) {
                piece.addValidMove(square.row - 1, square.col);
                getSquare(square.row - 1, square.col).addDependent(piece);
            } else {
                piece.addValidMove(square.row + 1, square.col);
                getSquare(square.row + 1, square.col).addDependent(piece);
            }
        }
    }

    // -- End

    // -- Start (Functionality)
    public static void calculateMoves() {
        for (Piece piece : whitePieces) {
            if (piece.pieceType.equals(KING)) continue;
            calculate(piece);
        }

        for (Piece piece : blackPieces) {
            if (piece.pieceType.equals(KING)) continue;
            calculate(piece);
        }

        Piece king = pieceMap.get(WHITE).get(KING).get(0);
        calculate(king);
        if (!king.doneCastled) checkForCastle(king);

        king = pieceMap.get(BLACK).get(KING).get(0);
        calculate(king);
        if (!king.doneCastled) checkForCastle(king);
        System.out.println("-------------------------------------------------------------------------");
    }

    public static void calculate(Piece piece) {
        piece.clearValidMoves();
        int[][] directions = piece.directions;

        switch (piece.pieceType) {
            case PAWN -> {
                int[] direction = directions[0];
                int newRow = piece.row + direction[0];
                int newCol = piece.col + direction[1];
                if (isInBounds(newRow, newCol)) {
                    Square square = getSquare(newRow, newCol);
                    if (square.isEmpty()) piece.addValidMove(newRow, newCol);
                    square.addDependent(piece);

                    direction = directions[1];
                    newRow = piece.row + direction[0];
                    newCol = piece.col + direction[1];
                    if (piece.numOfMoves == 0 && isInBounds(newRow, newCol)) {
                        square = getSquare(newRow, newCol);
                        if (square.isEmpty()) piece.addValidMove(newRow, newCol);
                        square.addDependent(piece);
                    }
                }

                exploreDiagonalMoves(piece, directions[2]);
                exploreDiagonalMoves(piece, directions[3]);

                if (piece.leftEnPassant.equals(NO)) checkForEnPassant(piece, true);
                if (piece.rightEnPassant.equals(NO)) checkForEnPassant(piece, false);
            }

            case KING, KNIGHT -> {
                for (int[] dir : directions) {
                    int newRow = piece.row + dir[0];
                    int newCol = piece.col + dir[1];

                    if (isInBounds(newRow, newCol)) {
                        Square square = getSquare(newRow, newCol);
                        if (isValidSquare(square, piece.pieceColor)) piece.addValidMove(newRow, newCol);
                        square.addDependent(piece);
                    }
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
                        if (isValidSquare(square, piece.pieceColor)) piece.addValidMove(row, col);
                        square.addDependent(piece);
                        multiplier++;
                    }

                    int row = piece.row + dir[0] * multiplier;
                    int col = piece.col + dir[1] * multiplier;
                    if (isInBounds(row, col)) {
                        Square square = getSquare(row, col);
                        if (isValidSquare(square, piece.pieceColor)) piece.addValidMove(row, col);
                        square.addDependent(piece);
                    }
                }
            }
        }

        count += 1;
    }

    /**
     *
     * @param to - This is the square where the selected piece
     *             is being moved (the selected piece is set in
     *             the Square class when a Square object with a
     *             non-empty piece is selected)
     */
    public static void movePiece(Square to) {
        Square from = startingSquare;
        boolean isEmpty = to.isEmpty();
        boolean isCastling = isCastling(to);

        /*
        The reason we need to check if the type of piece
        that is being moved is a pawn is because the pawn
        is the only piece that can't move back to a square
        that it left
         */
        if (pieceToMove.pieceType.equals(PAWN)) {
            for (int[] moves : pieceToMove.validMoves) {
                Square square = getSquare(moves[0], moves[1]);
                square.removeDependent(pieceToMove);
            }
        }

        startingSquare.addPiece(null);
        pieceToMove.update(to);
        Piece piece = to.piece;
        to.addPiece(pieceToMove);
        if (!isEmpty) removePiece(to, piece);
        checkCasesAfterMove(to, isEmpty, isCastling);

        /*
        If the move the results in a check for the other player
        check if the move the current player (who is under check)
        moved a piece that stops the check, if not reverse the move
        and prompt the player that they must either
        1. Block the check
        2. Move the king
        3. Eat the opponent piece that threatens check
        4. It may be that the king is checkmated, in which
        case the game will come to an end since there is nothing
        left to do

        Special cases to be taken into account are: castling, en passant
         */
        // reverseMove(from, pieceToMove, piece);
    }

    /*
    private static void reverseMove(Square from, Piece moved, Piece removed) {
        // this is the square where the removed piece was
        // might even be null in the case where no capture
        // take place
        Square to = getSquare(moved.row, moved.col);
    }
     */

    /**
     * The purpose of this method is to handle special moves like
     * promotion, castling and en passant
     *
     * @param to - The square the selected piece is being moved.
     * @param isEmpty - Tells us if the square we are moving to
     *                  contains a piece or not.
     * @param isCastling - Tells us if the current move is a
     *                     castling move.
     */
    private static void checkCasesAfterMove(Square to, boolean isEmpty, boolean isCastling) {
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
            return;
        }

        if (isCastling) {
            pieceToMove.doneCastled = true;
            castle(getCastlingType(to) ? KING_SIDE : QUEEN_SIDE, to.piece);
            return;
        }

        if (canPromote(pieceToMove)) {
            Main.frame.setEnabled(false);
            new PromotionWindow(playerTurn, to);
        }

        if (isEmpty)
            SoundEffects.playSound(MOVE);
        else
            SoundEffects.playSound(CAPTURE);
    }

    public static void update() {
        playerTurn = playerTurn.equals(WHITE) ? BLACK : WHITE;
        refresh();
    }

    /*
    /**
     * When a piece is moved it is unnecessary to update the valid moves
     * and the squares it depends on because only piece in a specific direction
     * moved so we can update the data in that direction. This method acts as
     * an intermediate by finding the direction where we need to update the piece
     * data.
     *
     * @param moved - The piece that just moved in the current turn
     * @param stationary - The piece that is dependent on the square
     *                     of the moved piece
     * @return Direction enumerated type

    public static Direction getDirection(Piece moved, Piece stationary) {
        int fRow = moved.row, fCol = moved.col, sRow = stationary.row, sCol = stationary.col;

        if (fRow == sRow) {
            return (sCol - fCol < 0 ? Direction.SOUTH : Direction.NORTH);
        } else if (fCol == sCol) {
            return (sRow - fRow < 0 ? Direction.EAST : Direction.WEST);
        }

        return null;
    }
     */

    // TO DO: Review
    private static void removePiece(Square to, Piece piece) {
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

    private static void castle(CastleType type, Piece piece) {
        int side = type.equals(KING_SIDE) ? 1 : 0;
        int offset = type.equals(KING_SIDE) ? -1 : 1;

        Square to = getSquare(piece.row, piece.col + offset);
        Piece rookPiece = pieceMap.get(piece.pieceColor).get(ROOK).get(side);

        startingSquare = getSquare(rookPiece.row, rookPiece.col);
        pieceToMove = rookPiece;
        movePiece(to);
        SoundEffects.playSound(CASTLE);
    }

    private static boolean check(int from, int to, int row) {
        List<Square> squares = new ArrayList<>();

        for (int i = from + 1; i < to; i++) {
            Square square = getSquare(row, i);
            if (square.isEmpty()) squares.add(square);
        }

        return !squares.isEmpty();
    }

    // TO DO (?): Clean Up
    public static void checkForCastle(Piece piece) {
        if (piece.numOfMoves != 0) return;
        Enums.Color color = piece.pieceColor;

        int row = color.equals(WHITE) ? 7 : 0;
        Piece leftRook = pieceMap.get(color).get(ROOK).get(0);
        Piece rightRook = pieceMap.get(color).get(ROOK).get(1);

        boolean canCastleQueenSide = check(leftRook.col, piece.col, row);
        boolean canCastleKingSide = check(piece.col, rightRook.col, row);

        if (canCastleQueenSide && leftRook.numOfMoves == 0)
            piece.addValidMove(row, piece.col - 2);
        if (canCastleKingSide && rightRook.numOfMoves == 0)
            piece.addValidMove(row, piece.col + 2);
    }

    // -- End

    // -- Start (Aesthetics/GUI Related)

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