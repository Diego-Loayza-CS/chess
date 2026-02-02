package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private final ChessPiece[][] board = new ChessPiece[8][8];


    public ChessBoard() {
    }


    public static boolean notInBounds(int row, int col) {
        return row < 1 || row > 8 || col < 1 || col > 8;
    }

    public static int[][] join_arrays(int[][] array_a, int[][] array_b) {
        int[][] result = new int[array_a.length + array_b.length][2];
        int i = 0;
        for (int[] tuple : array_a) {
            result[i++] = tuple;
        }
        for (int[] tuple : array_b) {
            result[i++] = tuple;
        }
        return result;
    }

    private static int toIndex(int location) {
        return location - 1;
    }


    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int row = position.getRow();
        int col = position.getColumn();
        if (notInBounds(row, col)) {
            throw new IllegalArgumentException("Position out of bounds: (" + row + ", " + col + ")");
        }
        board[toIndex(row)][toIndex(col)] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        if (notInBounds(row, col)) {
            return null;
        }
        return board[toIndex(row)][toIndex(col)];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        }
        setBackRow(ChessGame.TeamColor.WHITE, 1);
        setPawnRow(ChessGame.TeamColor.WHITE, 2);

        setPawnRow(ChessGame.TeamColor.BLACK, 7);
        setBackRow(ChessGame.TeamColor.BLACK, 8);
    }

    private void setPawnRow(ChessGame.TeamColor color, int row) {
        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(row, col), new ChessPiece(color, ChessPiece.PieceType.PAWN));
        }
    }

    private void setBackRow(ChessGame.TeamColor color, int row) {
        addPiece(new ChessPosition(row, 1), new ChessPiece(color, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(row, 2), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(row, 3), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(row, 4), new ChessPiece(color, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(row, 5), new ChessPiece(color, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(row, 6), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(row, 7), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(row, 8), new ChessPiece(color, ChessPiece.PieceType.ROOK));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ChessBoard that = (ChessBoard) obj;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece a = this.getPiece(position);
                ChessPiece b = that.getPiece(position);
                if (a == null && b == null) {
                    continue;
                }
                if (a == null || b == null) {
                    return false;
                }
                if (!a.equals(b)) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public int hashCode() {
        int hash = 1;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                hash *= 17;
                if (board[row][col] != null) {
                    hash += board[row][col].hashCode();
                }
            }
        }
        return hash;
    }
}
