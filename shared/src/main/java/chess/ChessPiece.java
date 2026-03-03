package chess;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    private static final int[][] straightMoves = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
    private static final int[][] diagonalMoves = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    private static final int[][] surroundingMoves = ChessBoard.joinArrays(straightMoves, diagonalMoves);
    private static final int[][] knightMoves = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {-1, 2}, {1, -2}, {-1, -2}};


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new java.util.ArrayList<>();
        switch (type) {

            case KING:
                for (int[] i : surroundingMoves) {
                    addIfValid(board, myPosition, myPosition.getRow() + i[0], myPosition.getColumn() + i[1], moves);
                }
                break;


            case KNIGHT:
                for (int[] i : knightMoves) {
                    addIfValid(board, myPosition, myPosition.getRow() + i[0], myPosition.getColumn() + i[1], moves);
                }
                break;


            case ROOK:
                for (int[] i : straightMoves) {
                    scanLine(board, myPosition, i[0], i[1], moves);
                }
                break;


            case BISHOP:
                for (int[] i : diagonalMoves) {
                    scanLine(board, myPosition, i[0], i[1], moves);
                }
                break;


            case QUEEN:
                for (int[] i : surroundingMoves) {
                    scanLine(board, myPosition, i[0], i[1], moves);
                }
                break;


            case PAWN:
                pawnMoves(board, myPosition, moves);
                break;
        }

        return moves;
    }

    public static int getPawnDirection(ChessGame.TeamColor pawnColor) {
        if (pawnColor == ChessGame.TeamColor.WHITE) {
            return 1;
        }
        if (pawnColor == ChessGame.TeamColor.BLACK) {
            return -1;
        }
        throw new IllegalArgumentException("Invalid color: " + pawnColor);
    }


    private void addIfValid(ChessBoard board, ChessPosition origin, int destRow, int destCol, Collection<ChessMove> moves) {
        if (ChessBoard.notInBounds(destRow, destCol)) {
            return;
        }

        ChessPosition destination = new ChessPosition(destRow, destCol);
        ChessPiece target = board.getPiece(destination);

        if (target == null || target.getTeamColor() != pieceColor) {
            moves.add(new ChessMove(origin, destination, null));
        }
    }

    private void scanLine(ChessBoard board, ChessPosition origin, int dirRow, int dirCol, Collection<ChessMove> moves) {
        int currentRow = origin.getRow();
        int currentCol = origin.getColumn();

        while (true) {
            currentRow += dirRow;
            currentCol += dirCol;

            if (ChessBoard.notInBounds(currentRow, currentCol)) {
                return;
            }

            ChessPosition destination = new ChessPosition(currentRow, currentCol);
            ChessPiece target = board.getPiece(destination);

            if (target == null) {
                moves.add(new ChessMove(origin, destination, null));
            } else {
                if (target.getTeamColor() != pieceColor) {
                    moves.add(new ChessMove(origin, destination, null));
                }
                return;
            }
        }
    }

    private void pawnMoves(ChessBoard board, ChessPosition origin, Collection<ChessMove> moves) {
        int dir = getPawnDirection(pieceColor);
        int startRow = (pieceColor == ChessGame.TeamColor.WHITE ? 2 : 7);
        int promotionRow = (pieceColor == ChessGame.TeamColor.WHITE ? 8 : 1);

        int row = origin.getRow();
        int col = origin.getColumn();

        int row1 = row + dir;
        if (!ChessBoard.notInBounds(row1, col)) {
            ChessPosition destination1 = new ChessPosition(row1, col);
            if (board.getPiece(destination1) == null) {
                pawnAddIfValid(origin, destination1, promotionRow, moves);

                int row2 = row + 2 * dir;
                if (!ChessBoard.notInBounds(row2, col)) {
                    ChessPosition destination2 = new ChessPosition(row2, col);
                    if (row == startRow && board.getPiece(destination2) == null) {
                        moves.add(new ChessMove(origin, destination2, null));
                    }
                }

            }
        }

        for (int diag : new int[]{-1, 1}) {
            int diagCol = col + diag;
            int diagRow = row + dir;

            if (!ChessBoard.notInBounds(diagRow, diagCol)) {
                ChessPosition destination3 = new ChessPosition(diagRow, diagCol);
                ChessPiece target = board.getPiece(destination3);
                if (target != null && target.getTeamColor() != pieceColor) {
                    pawnAddIfValid(origin, destination3, promotionRow, moves);
                }
            }

        }
    }


    private void pawnAddIfValid(ChessPosition origin, ChessPosition destination, int promotionRow, Collection<ChessMove> moves) {
        if (destination.getRow() == promotionRow) {
            moves.add(new ChessMove(origin, destination, PieceType.QUEEN));
            moves.add(new ChessMove(origin, destination, PieceType.ROOK));
            moves.add(new ChessMove(origin, destination, PieceType.BISHOP));
            moves.add(new ChessMove(origin, destination, PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(origin, destination, null));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) obj;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return 17 * pieceColor.hashCode() + type.hashCode();
    }
}
