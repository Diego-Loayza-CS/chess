package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    public ChessPosition startPosition;
    public ChessPosition endPosition;
    public ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChessMove that = (ChessMove) obj;
        if (promotionPiece == null) {
            if (that.promotionPiece == null) {
                return startPosition.equals(that.startPosition) && endPosition.equals(that.endPosition);
            }
            return false;
        }
        return startPosition.equals(that.startPosition) && endPosition.equals(that.endPosition) && promotionPiece.equals(that.promotionPiece);
    }

    @Override
    public int hashCode() {
        if (promotionPiece == null) {
            return 31 * startPosition.hashCode() + endPosition.hashCode();
        }
        return 31 * startPosition.hashCode() * (endPosition.hashCode() + promotionPiece.hashCode());
    }
}
