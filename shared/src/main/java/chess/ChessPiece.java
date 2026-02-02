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

    private final int[][] STRAIGHT_MOVES = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
    private final int[][] DIAGONAL_MOVES = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    private final int[][] SURROUNDING_MOVES = ChessBoard.join_arrays(STRAIGHT_MOVES, DIAGONAL_MOVES);
    private final int[][] KNIGHT_MOVES = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {-1, 2}, {1, -2}, {-1, -2}};


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
                for (int[] i : SURROUNDING_MOVES) {
                    addIfValid(board, myPosition, myPosition.getRow() + i[0], myPosition.getColumn() + i[1], moves);
                }
                break;


            case KNIGHT:
                for (int[] i : KNIGHT_MOVES) {
                    addIfValid(board, myPosition, myPosition.getRow() + i[0], myPosition.getColumn() + i[1], moves);
                }
                break;


            case ROOK:
                for (int[] i : STRAIGHT_MOVES) {
                    scanLine(board, myPosition, i[0], i[1], moves);
                }
                break;


            case BISHOP:
                for (int[] i : DIAGONAL_MOVES) {
                    scanLine(board, myPosition, i[0], i[1], moves);
                }
                break;


            case QUEEN:
                for (int[] i : SURROUNDING_MOVES) {
                    scanLine(board, myPosition, i[0], i[1], moves);
                }
                break;


            case PAWN:
                pawnMoves(board, myPosition, moves);
                break;
        }

        return moves;
    }




    private void addIfValid(ChessBoard board, ChessPosition origin, int d_row, int d_col, Collection<ChessMove> moves) {
        if (ChessBoard.notInBounds(d_row, d_col)) {
            return;
        }

        ChessPosition destination = new ChessPosition(d_row, d_col);
        ChessPiece target = board.getPiece(destination);

        if (target == null || target.getTeamColor() != pieceColor) {
            moves.add(new ChessMove(origin, destination, null));
        }
    }

    private void scanLine(ChessBoard board, ChessPosition origin, int dir_row, int dir_col, Collection<ChessMove> moves) {
        int d_row = origin.getRow();
        int d_col = origin.getColumn();

        while (true) {
            d_row += dir_row;
            d_col += dir_col;

            if (ChessBoard.notInBounds(d_row, d_col)) {
                return;
            }

            ChessPosition destination = new ChessPosition(d_row, d_col);
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
        int dir = (pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1);
        int startRow = (pieceColor == ChessGame.TeamColor.WHITE ? 2 : 7);
        int promotionRow = (pieceColor == ChessGame.TeamColor.WHITE ? 8 : 1);

        int row = origin.getRow();
        int col = origin.getColumn();

        int row1 = row + dir;
        if (!ChessBoard.notInBounds(row1, col)) {
            ChessPosition destination_1 = new ChessPosition(row1, col);
            if (board.getPiece(destination_1) == null) {
                pawnAddIfValid(origin, destination_1, promotionRow, moves);

                int row2 = row + 2 * dir;
                if (!ChessBoard.notInBounds(row2, col)) {
                    ChessPosition destination_2 = new ChessPosition(row2, col);
                    if (row == startRow && board.getPiece(destination_2) == null) {
                        moves.add(new ChessMove(origin, destination_2, null));
                    }
                }

            }
        }

        for (int diag : new int[]{-1, 1}) {
            int diag_col = col + diag;
            int diag_row = row + dir;

            if (!ChessBoard.notInBounds(diag_row, diag_col)) {
                ChessPosition destination_3 = new ChessPosition(diag_row, diag_col);
                ChessPiece target = board.getPiece(destination_3);
                if (target != null && target.getTeamColor() != pieceColor) {
                    pawnAddIfValid(origin, destination_3, promotionRow, moves);
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
