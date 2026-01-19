package chess;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    public ChessGame.TeamColor pieceColor;
    public ChessPiece.PieceType type;

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
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        if (x == 0 && y == 0) continue;
                        addIfValid(board, myPosition, myPosition.getRow() + x, myPosition.getColumn() + y, moves);
                    }
                }
                break;


            case KNIGHT:
                int[][] jumps = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {-1, 2}, {1, -2}, {-1, -2}};
                for (int[] j : jumps) {
                    addIfValid(board, myPosition, myPosition.getRow() + j[0], myPosition.getColumn() + j[1], moves);
                }
                break;


            case ROOK:
                scanLine(board, myPosition, 1, 0, moves);
                scanLine(board, myPosition, -1, 0, moves);
                scanLine(board, myPosition, 0, 1, moves);
                scanLine(board, myPosition, 0, -1, moves);
                break;


            case BISHOP:
                scanLine(board, myPosition, 1, 1, moves);
                scanLine(board, myPosition, 1, -1, moves);
                scanLine(board, myPosition, -1, 1, moves);
                scanLine(board, myPosition, -1, -1, moves);
                break;


            case QUEEN:
                scanLine(board, myPosition, 1, 0, moves);
                scanLine(board, myPosition, -1, 0, moves);
                scanLine(board, myPosition, 0, 1, moves);
                scanLine(board, myPosition, 0, -1, moves);
                scanLine(board, myPosition, 1, 1, moves);
                scanLine(board, myPosition, 1, -1, moves);
                scanLine(board, myPosition, -1, 1, moves);
                scanLine(board, myPosition, -1, -1, moves);
                break;


            case PAWN:
                pawnMoves(board, myPosition, moves);
                break;
        }

        return moves;
    }

}
