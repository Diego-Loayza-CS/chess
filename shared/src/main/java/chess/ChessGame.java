package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn = TeamColor.WHITE;
    private ChessBoard board = new ChessBoard();

    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookAMoved = false;
    private boolean whiteRookHMoved = false;
    private boolean blackRookAMoved = false;
    private boolean blackRookHMoved = false;

    private ChessPosition enPassantTarget = null;
    private ChessPosition enPassantPawnPosition = null;

    public ChessGame() {
        board.resetBoard();

        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRookAMoved = false;
        whiteRookHMoved = false;
        blackRookAMoved = false;
        blackRookHMoved = false;

        enPassantTarget = null;
        enPassantPawnPosition = null;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        TeamColor pieceColor = piece.getTeamColor();

        Collection<ChessMove> allMoves = piece.pieceMoves(board, startPosition);

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            allMoves.addAll(getCastlingMoves(startPosition, pieceColor));
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            ChessMove epMove = getEnPassantMove(startPosition, pieceColor);
            if (epMove != null) {
                allMoves.add(epMove);
            }
        }

        Collection<ChessMove> legalMoves = new java.util.ArrayList<>();

        for (ChessMove move : allMoves) {
            ChessBoard copiedBoard = ChessBoard.copyBoard(board);
            applyMove(copiedBoard, move);
            if (!ChessGameHelpers.isBoardInCheck(copiedBoard, pieceColor)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (move == null) {
            throw new InvalidMoveException("Move cannot be null");
        }

        ChessPosition start = move.getStartPosition();
        ChessPiece piece = board.getPiece(start);
        if (piece == null) {
            throw new InvalidMoveException("No piece at start position");
        }
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Not your turn");
        }

        Collection<ChessMove> legalMoves = validMoves(start);
        if (legalMoves == null || !ChessGameHelpers.containsMove(legalMoves, move)) {
            throw new InvalidMoveException("Illegal move");
        }

        updateMovedFlagsBeforeMove(piece, start);

        applyMove(board, move);

        enPassantTarget = null;
        enPassantPawnPosition = null;

        if (ChessGameHelpers.isCastlingMove(piece, move)) {
            updateRookMovedAfterCastling(piece.getTeamColor(), move.getEndPosition());
        }
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            updateEnPassantAfterPawnMove(piece, move);
        }

        teamTurn = ChessGameHelpers.getOppositeTeam(teamTurn);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return ChessGameHelpers.isBoardInCheck(board, teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return hasNoLegalMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return hasNoLegalMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private void applyMove(ChessBoard board, ChessMove move) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);
        if (piece == null) {
            return;
        }

        boolean enPassant = isEnPassantMove(board, piece, start, end);
        boolean castling = ChessGameHelpers.isCastlingMove(piece, move);

        board.addPiece(start, null);

        if (enPassant) {
            int dir = (piece.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
            ChessPosition capturedPawnPos = new ChessPosition(end.getRow() - dir, end.getColumn());
            board.addPiece(capturedPawnPos, null);
        }

        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        board.addPiece(end, piece);

        if (castling) {
            int row = start.getRow();
            if (end.getColumn() == 7) {
                ChessGameHelpers.moveRook(board, row, 8, 6);
            } else if (end.getColumn() == 3) {
                ChessGameHelpers.moveRook(board, row, 1, 4);
            }
        }
    }

    private Collection<ChessMove> getCastlingMoves(ChessPosition kingPos, TeamColor kingColor) {
        Collection<ChessMove> moves = new java.util.ArrayList<>();

        int row = (kingColor == TeamColor.WHITE) ? 1 : 8;

        if (kingPos.getRow() != row || kingPos.getColumn() != 5) {
            return moves;
        }

        if (kingColor == TeamColor.WHITE && whiteKingMoved) {
            return moves;
        }
        if (kingColor == TeamColor.BLACK && blackKingMoved) {
            return moves;
        }

        if (ChessGameHelpers.isBoardInCheck(board, kingColor)) {
            return moves;
        }

        TeamColor enemy = ChessGameHelpers.getOppositeTeam(kingColor);

        if (canCastleKingSide(kingColor, row, enemy)) {
            moves.add(new ChessMove(kingPos, new ChessPosition(row, 7), null));
        }

        if (canCastleQueenSide(kingColor, row, enemy)) {
            moves.add(new ChessMove(kingPos, new ChessPosition(row, 3), null));
        }

        return moves;
    }

    private boolean canCastleKingSide(TeamColor kingColor, int row, TeamColor enemy) {
        if (kingColor == TeamColor.WHITE && whiteRookHMoved) {
            return false;
        }
        if (kingColor == TeamColor.BLACK && blackRookHMoved) {
            return false;
        }

        ChessPiece rook = board.getPiece(new ChessPosition(row, 8));
        if (rook == null || rook.getTeamColor() != kingColor || rook.getPieceType() != ChessPiece.PieceType.ROOK) {
            return false;
        }

        ChessPosition f = new ChessPosition(row, 6);
        ChessPosition g = new ChessPosition(row, 7);
        if (board.getPiece(f) != null || board.getPiece(g) != null) {
            return false;
        }

        return !ChessGameHelpers.isSquareAttacked(board, f, enemy) && !ChessGameHelpers.isSquareAttacked(board, g, enemy);
    }

    private boolean canCastleQueenSide(TeamColor kingColor, int row, TeamColor enemy) {
        if (kingColor == TeamColor.WHITE && whiteRookAMoved) {
            return false;
        }
        if (kingColor == TeamColor.BLACK && blackRookAMoved) {
            return false;
        }

        ChessPiece rook = board.getPiece(new ChessPosition(row, 1));
        if (rook == null || rook.getTeamColor() != kingColor || rook.getPieceType() != ChessPiece.PieceType.ROOK) {
            return false;
        }

        ChessPosition b = new ChessPosition(row, 2);
        ChessPosition c = new ChessPosition(row, 3);
        ChessPosition d = new ChessPosition(row, 4);
        if (board.getPiece(b) != null || board.getPiece(c) != null || board.getPiece(d) != null) {
            return false;
        }

        return !ChessGameHelpers.isSquareAttacked(board, d, enemy) && !ChessGameHelpers.isSquareAttacked(board, c, enemy);
    }

    private void updateMovedFlagsBeforeMove(ChessPiece piece, ChessPosition start) {
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                whiteKingMoved = true;
            } else {
                blackKingMoved = true;
            }
        } else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (piece.getTeamColor() == TeamColor.WHITE && start.getRow() == 1) {
                if (start.getColumn() == 1) {
                    whiteRookAMoved = true;
                }
                if (start.getColumn() == 8) {
                    whiteRookHMoved = true;
                }
            } else if (piece.getTeamColor() == TeamColor.BLACK && start.getRow() == 8) {
                if (start.getColumn() == 1) {
                    blackRookAMoved = true;
                }
                if (start.getColumn() == 8) {
                    blackRookHMoved = true;
                }
            }
        }
    }

    private void updateRookMovedAfterCastling(TeamColor kingColor, ChessPosition kingEnd) {
        boolean kingSide = (kingEnd.getColumn() == 7);
        if (kingColor == TeamColor.WHITE) {
            if (kingSide) {
                whiteRookHMoved = true;
            } else {
                whiteRookAMoved = true;
            }
        } else {
            if (kingSide) {
                blackRookHMoved = true;
            } else {
                blackRookAMoved = true;
            }
        }
    }

    private boolean isEnPassantMove(ChessBoard board, ChessPiece pawn, ChessPosition start, ChessPosition end) {
        if (pawn.getPieceType() != ChessPiece.PieceType.PAWN) {
            return false;
        }
        if (enPassantTarget == null || enPassantPawnPosition == null) {
            return false;
        }
        if (!end.equals(enPassantTarget)) {
            return false;
        }
        if (board.getPiece(end) != null) {
            return false;
        }

        int dir = ChessPiece.getPawnDirection(pawn.getTeamColor());
        return end.getRow() == start.getRow() + dir && Math.abs(end.getColumn() - start.getColumn()) == 1;
    }

    private ChessMove getEnPassantMove(ChessPosition pawnPos, TeamColor pawnColor) {
        if (enPassantTarget == null || enPassantPawnPosition == null) {
            return null;
        }
        ChessPiece pawn = board.getPiece(pawnPos);
        if (pawn == null || pawn.getPieceType() != ChessPiece.PieceType.PAWN) {
            return null;
        }
        if (pawn.getTeamColor() != pawnColor) {
            return null;
        }
        if (board.getPiece(enPassantTarget) != null) {
            return null;
        }

        int dir = ChessPiece.getPawnDirection(pawnColor);
        if (enPassantTarget.getRow() != pawnPos.getRow() + dir) {
            return null;
        }
        if (Math.abs(enPassantTarget.getColumn() - pawnPos.getColumn()) != 1) {
            return null;
        }
        if (enPassantPawnPosition.getRow() != pawnPos.getRow()) {
            return null;
        }
        if (enPassantPawnPosition.getColumn() != enPassantTarget.getColumn()) {
            return null;
        }

        ChessPiece victim = board.getPiece(enPassantPawnPosition);
        if (victim == null || victim.getPieceType() != ChessPiece.PieceType.PAWN) {
            return null;
        }
        if (victim.getTeamColor() == pawnColor) {
            return null;
        }

        return new ChessMove(pawnPos, enPassantTarget, null);
    }

    private void updateEnPassantAfterPawnMove(ChessPiece pawn, ChessMove move) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        if (Math.abs(end.getRow() - start.getRow()) != 2) {
            return;
        }

        int dir = ChessPiece.getPawnDirection(pawn.getTeamColor());

        enPassantTarget = new ChessPosition(start.getRow() + dir, start.getColumn());
        enPassantPawnPosition = end;
    }

    private boolean hasNoLegalMoves(TeamColor team) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece == null || piece.getTeamColor() != team) {
                    continue;
                }
                Collection<ChessMove> moves = validMoves(position);
                if (moves != null && !moves.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChessGame that = (ChessGame) obj;
        return teamTurn == that.teamTurn && board.equals(that.board);
    }

    @Override
    public int hashCode() {
        return 17 * teamTurn.hashCode() + board.hashCode();
    }

}
