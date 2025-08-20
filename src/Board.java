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

        setupPawns(WHITE);
        setupBackRank(WHITE);
        setupPawns(BLACK);
        setupBackRank(BLACK);

        calculateMoves();
    }

    private void setupPawns(Enums.Color color) {
        for (int col = 0; col < COLS; col++) {
            Pawn pawn = new Pawn(color, col);
            addPiece(pawn);
        }
    }

    // Absolute shit, TO DO: Refactor
    private void setupBackRank(Enums.Color color) {
         Piece piece = new Rook(color, true);
         addPiece(piece);
         piece = new Rook(color, false);
         addPiece(piece);
         piece = new Knight(color, true);
         addPiece(piece);
         piece = new Knight(color, false);
         addPiece(piece);
         piece = new Bishop(color, true);
         addPiece(piece);
         piece = new Bishop(color, false);
         addPiece(piece);
         piece = new Queen(color);
         addPiece(piece);
         piece = new King(color);
         addPiece(piece);
    }

    private void addPiece(Piece piece) {
        Square square = getSquare(piece.row, piece.col);
        square.addPiece(piece);

        if (piece.sameColor(BLACK))
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
            if (piece.sameType(type) && piece.sameColor(color)) return piece;
        return null;
    }

    private static void exploreDiagonalMoves(Piece piece, int[] direction) {
        int newRow = piece.row + direction[0];
        int newCol = piece.col + direction[1];
        if (isInBounds(newRow, newCol)) {
            Square square = getSquare(newRow, newCol);
            if (!square.isEmpty() && !square.sameColor(piece.pieceColor)) piece.addValidMove(newRow, newCol);
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
        if (((Pawn) pieceToMove).getLeftEnPassant().equals(YES) && pieceToMove.pieceType.equals(PAWN)) return -1;
        if (((Pawn) pieceToMove).getRightEnPassant().equals(YES) && pieceToMove.pieceType.equals(PAWN)) return 1;
        return 0;
    }

    private static boolean canPromote(Piece piece) {
        if (!piece.sameType(PAWN)) return false;
        return ((piece.sameColor(WHITE) && piece.row == 0) || (piece.sameColor(BLACK) && piece.row == 7));
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
            square.sameType(PAWN) && square.piece.numOfMoves == 1) {

            if (left)
                ((Pawn) piece).setLeftEnPassant(YES);
            else
                ((Pawn) piece).setRightEnPassant(YES);

            int offset = piece.sameColor(WHITE) ? -1 : 1;
            piece.addValidMove(square.row + offset, square.col);
            getSquare(square.row + offset, square.col).addDependent(piece);
        }
    }

    // -- End

    // -- Start (Functionality)
    public static void calculateMoves() {
        List<Piece> allPieces = new ArrayList<>(whitePieces);
        allPieces.addAll(blackPieces);
        allPieces.stream().filter(piece -> !piece.sameType(KING)).forEach(Board::calculate);
    }

    public static void calculate(Piece piece) {
        for (int[] move : piece.validMoves) {
            Square square = getSquare(move[0], move[1]);
            square.removeDependent(piece);
        }

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

                if (piece.getLeftEnPassant().equals(NO)) checkForEnPassant(piece, true);
                if (piece.getRightEnPassant().equals(NO)) checkForEnPassant(piece, false);
            }

            case KING, KNIGHT -> {
                if (piece.sameType(KING) && piece.getCastledStatus()) checkForCastle(piece);

                for (int[] dir : directions) {
                    int newRow = piece.row + dir[0];
                    int newCol = piece.col + dir[1];

                    if (isInBounds(newRow, newCol)) {
                        Square square = getSquare(newRow, newCol);
                        if (isValidSquare(square, piece.pieceColor)) {
                            boolean valid = square.getDependentPieces()
                                    .stream()
                                    .noneMatch(p -> p.sameColor(
                                            piece.sameColor(WHITE) ? BLACK : WHITE
                                    ));

                            if (piece.sameType(KING) && !valid) continue;
                            piece.addValidMove(newRow, newCol);
                        }
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

    public static void movePiece(Square to) {
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

        Piece piece = to.piece;
        startingSquare.addPiece(null);
        // pieceToMove.setLastPosition();
        pieceToMove.update(to, false);
        if (!isEmpty) removePiece(to, piece);
        checkCasesAfterMove(to, isEmpty, isCastling, false);
    }

    public static void reverseMove(Piece removed) {
        Square to = getSquare(pieceToMove.row, pieceToMove.col);
        if (removed != null) {
            to.addPiece(removed);
            if (removed.sameColor(WHITE))
                whitePieces.add(removed);
            else
                blackPieces.add(removed);
        } else {
            to.addPiece(null);
        }

        pieceToMove.update(startingSquare, true);
        playerTurn = playerTurn.equals(WHITE) ? BLACK : WHITE;
    }

    private static void checkCasesAfterMove(Square to, boolean isEmpty, boolean isCastling, boolean reverse) {
        int code;
        if (Math.abs(code = isEnPassant(to)) == 1) {
            Square square = null;
            switch (pieceToMove.pieceColor) {
                case WHITE -> square = getSquare(to.row + 1, to.col);
                case BLACK -> square = getSquare(to.row - 1, to.col);
            }

            if (code == 1)
                ((Pawn) Board.pieceToMove).setRightEnPassant(NOT_ALLOWED);
            else
                ((Pawn) Board.pieceToMove).setLeftEnPassant(NOT_ALLOWED);

            if (square != null) square.addPiece(null);
            SoundEffects.playSound(CAPTURE);
            return;
        }

        if (isCastling) {
            ((King) pieceToMove).setCastledStatus(false);
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

    // TO DO: Review
    private static void removePiece(Square to, Piece piece) {
        if (Board.playerTurn.equals(WHITE))
            Board.blackPieces.remove(piece);
        else
            Board.whitePieces.remove(piece);
    }

    private static boolean isCheck(Enums.Color pieceColor) {
        List<Piece> pieces = pieceColor.equals(WHITE) ? blackPieces : whitePieces;
        return pieces
               .stream()
               .flatMap(piece -> piece.validMoves.stream())
               .map(coords -> getSquare(coords[0], coords[1]))
               .filter(square -> square.piece != null)
               .anyMatch(square -> square.sameType(KING));
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
            if (!square.isEmpty()) return false;
            squares.add(square);
        }

        /*
        Detects if there are any opposing pieces that
        attack any of the squares in between the king
        and the rook. If so then castling is not allowed
        */
        return squares.stream()
                .flatMap(s -> s.getDependentPieces().stream())
                .noneMatch(piece -> piece.pieceColor.equals(
                        (row == 7) ? BLACK : WHITE
                ));
    }

    // TO DO (?): Clean Up
    public static void checkForCastle(Piece piece) {
        if (piece.numOfMoves != 0 || isCheck(piece.pieceColor)) return;
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