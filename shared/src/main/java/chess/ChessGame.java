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


    public ChessGame() {
        board.resetBoard();
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
        Collection<ChessMove> legalMoves = new java.util.ArrayList<>();


        for (ChessMove move : allMoves) {
            ChessBoard copy = copyBoard(board);
            applyMove(copy, move, piece);
            if (!helper_isInCheck(copy, pieceColor)) {
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
        if (legalMoves == null || !containsMove(legalMoves, move)) {
            throw new InvalidMoveException("Illegal move");
        }

        applyMove(board, move, piece);
        teamTurn = (teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE); //TODO helper switch teams
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return helper_isInCheck(board, teamColor);
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
        return !hasAnyLegalMove(teamColor);
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
        return !hasAnyLegalMove(teamColor);
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


    private boolean helper_isInCheck(ChessBoard board, TeamColor team_color) {
        ChessPosition kingPosition = findKing(board, team_color);
        if (kingPosition == null) {
            return false;
        }
        TeamColor enemy_color = (team_color == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE); //TODO helper switch teams

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition from = new ChessPosition(row, col);
                ChessPiece attacker = board.getPiece(from);
                if (attacker == null || attacker.getTeamColor() != enemy_color) {
                    continue;
                }

                Collection<ChessMove> attacks = attacker.pieceMoves(board, from);
                for (ChessMove move : attacks) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private ChessPosition findKing(ChessBoard board, TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece == null) {
                    continue;
                }
                if (piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }


    private ChessBoard copyBoard(ChessBoard original) {
        ChessBoard copy = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = original.getPiece(position);
                if (piece == null) {
                    copy.addPiece(position, null);
                } else {
                    copy.addPiece(position, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                }
            }
        }
        return copy;
    }

    private void applyMove(ChessBoard board, ChessMove move, ChessPiece piece) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        board.addPiece(start, null);

        ChessPiece movingPiece = piece;

        if (move.getPromotionPiece() != null) {
            movingPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        board.addPiece(end, movingPiece);
    }



    private boolean hasAnyLegalMove(TeamColor team) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece == null || piece.getTeamColor() != team) continue;
                Collection<ChessMove> moves = validMoves(position);
                if (moves != null && !moves.isEmpty()) return true;
            }
        }
        return false;
    }


    private boolean containsMove(Collection<ChessMove> moves, ChessMove target) {
        for (ChessMove move : moves) {
            if (sameMove(move, target)) return true;
        }
        return false;
    }


    private boolean sameMove(ChessMove move_a, ChessMove move_b) {
        if (move_a == move_b) return true;
        if (move_a == null || move_b == null) return false;

        if (!move_a.getStartPosition().equals(move_b.getStartPosition())) return false;
        if (!move_a.getEndPosition().equals(move_b.getEndPosition())) return false;

        ChessPiece.PieceType pieceType_a = move_a.getPromotionPiece();
        ChessPiece.PieceType pieceType_b = move_b.getPromotionPiece();
        if (pieceType_a == null && pieceType_b == null) return true;
        if (pieceType_a == null || pieceType_b == null) return false;
        return pieceType_a == pieceType_b;
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
        return teamTurn == that.teamTurn && board == that.board;
    }

    @Override
    public int hashCode() {
        return 17 * teamTurn.hashCode() + board.hashCode();
    }

}
