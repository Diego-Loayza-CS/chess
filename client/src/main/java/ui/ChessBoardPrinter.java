package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class ChessBoardPrinter {

    public static String drawBoard(boolean whitePerspective) {
        ChessGame game = new ChessGame();
        ChessBoard board = game.getBoard();

        StringBuilder out = new StringBuilder();
        out.append(SET_BG_COLOR_BLACK).append(SET_TEXT_COLOR_WHITE);

        if (whitePerspective) {
            drawFiles(out, true);
            for (int row = 8; row >= 1; row--) {
                drawRow(out, board, row, true);
            }
            drawFiles(out, true);
        } else {
            drawFiles(out, false);
            for (int row = 1; row <= 8; row++) {
                drawRow(out, board, row, false);
            }
            drawFiles(out, false);
        }

        out.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR);
        return out.toString();
    }

    private static void drawFiles(StringBuilder out, boolean whitePerspective) {
        out.append(SET_BG_COLOR_LIGHT_GREY).append(SET_TEXT_COLOR_BLACK);
        out.append("   ");

        if (whitePerspective) {
            for (char file = 'a'; file <= 'h'; file++) {
                out.append(" ").append(file).append(" ");
            }
        } else {
            for (char file = 'h'; file >= 'a'; file--) {
                out.append(" ").append(file).append(" ");
            }
        }

        out.append("   ").append("\n");
    }

    private static void drawRow(StringBuilder out, ChessBoard board, int row, boolean whitePerspective) {
        out.append(SET_BG_COLOR_LIGHT_GREY).append(SET_TEXT_COLOR_BLACK);
        out.append(" ").append(row).append(" ");

        if (whitePerspective) {
            for (int col = 1; col <= 8; col++) {
                drawSquare(out, board, row, col);
            }
        } else {
            for (int col = 8; col >= 1; col--) {
                drawSquare(out, board, row, col);
            }
        }

        out.append(SET_BG_COLOR_LIGHT_GREY).append(SET_TEXT_COLOR_BLACK);
        out.append(" ").append(row).append(" ");
        out.append("\n");
    }

    private static void drawSquare(StringBuilder out, ChessBoard board, int row, int col) {
        boolean lightSquare = (row + col) % 2 != 0;
        out.append(lightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_DARK_GREY);

        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            out.append("   ");
            return;
        }

        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            out.append(SET_TEXT_COLOR_RED);
        } else {
            out.append(SET_TEXT_COLOR_BLUE);
        }

        out.append(" ").append(pieceSymbol(piece)).append(" ");
    }

    private static String pieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "P";
        };
    }
}