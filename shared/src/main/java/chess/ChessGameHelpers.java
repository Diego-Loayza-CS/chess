package chess;

import java.util.Collection;

public class ChessGameHelpers {
    private ChessGameHelpers() {
    }

    ;

    static ChessGame.TeamColor getOppositeTeam(ChessGame.TeamColor team) {
        if (team == ChessGame.TeamColor.WHITE) {
            return ChessGame.TeamColor.BLACK;
        }
        if (team == ChessGame.TeamColor.BLACK) {
            return ChessGame.TeamColor.WHITE;
        }
        return null;
    }

    static boolean isBoardInCheck(ChessBoard board, ChessGame.TeamColor teamColor) {
        ChessPosition kingPosition = findKing(board, teamColor);
        if (kingPosition == null) {
            return false;
        }
        ChessGame.TeamColor enemyColor = ChessGameHelpers.getOppositeTeam(teamColor);

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition from = new ChessPosition(row, col);
                ChessPiece attacker = board.getPiece(from);
                if (attacker == null || attacker.getTeamColor() != enemyColor) {
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

    static ChessPosition findKing(ChessBoard board, ChessGame.TeamColor teamColor) {
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

    static void moveRook(ChessBoard board, int row, int rookStartCol, int rookEndCol) {
        ChessPosition rookStart = new ChessPosition(row, rookStartCol);
        ChessPosition rookEnd = new ChessPosition(row, rookEndCol);

        ChessPiece rook = board.getPiece(rookStart);
        board.addPiece(rookStart, null);
        board.addPiece(rookEnd, rook);
    }

    static boolean isSquareAttacked(ChessBoard b, ChessPosition square, ChessGame.TeamColor byTeam) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition from = new ChessPosition(row, col);
                ChessPiece p = b.getPiece(from);
                if (p == null || p.getTeamColor() != byTeam) {
                    continue;
                }

                for (ChessMove m : p.pieceMoves(b, from)) {
                    if (m.getEndPosition().equals(square)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static boolean isCastlingMove(ChessPiece piece, ChessMove move) {
        if (piece.getPieceType() != ChessPiece.PieceType.KING) {
            return false;
        }
        ChessPosition s = move.getStartPosition();
        ChessPosition e = move.getEndPosition();
        return s.getRow() == e.getRow() && Math.abs(e.getColumn() - s.getColumn()) == 2;
    }

    static boolean containsMove(Collection<ChessMove> moves, ChessMove target) {
        for (ChessMove move : moves) {
            if (sameMove(move, target)) {
                return true;
            }
        }
        return false;
    }

    static boolean sameMove(ChessMove moveA, ChessMove moveB) {
        if (moveA == moveB) {
            return true;
        }
        if (moveA == null || moveB == null) {
            return false;
        }

        if (!moveA.getStartPosition().equals(moveB.getStartPosition())) {
            return false;
        }
        if (!moveA.getEndPosition().equals(moveB.getEndPosition())) {
            return false;
        }

        ChessPiece.PieceType pieceTypeA = moveA.getPromotionPiece();
        ChessPiece.PieceType pieceTypeB = moveB.getPromotionPiece();
        if (pieceTypeA == null && pieceTypeB == null) {
            return true;
        }
        if (pieceTypeA == null || pieceTypeB == null) {
            return false;
        }
        return pieceTypeA == pieceTypeB;

    }
}