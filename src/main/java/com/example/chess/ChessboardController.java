package com.example.chess;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class ChessboardController implements Initializable {

    @FXML
    private GridPane chessboardGrid;
    private Pane previousCheckKing;
    private ChessPiece[][] chessboard = new ChessPiece[8][8];
    private ChessPiece draggedPiece;
    private int draggedPieceOriginalRow;
    private int draggedPieceOriginalCol;
    private boolean isWhiteTurn = true;
    int pRow;
    int pCol;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupChessboardGrid();
    }


    private void setupChessboardGrid() {
        String[][] initialState = {
                {"br", "bn", "bb", "bq", "bk", "bb", "bn", "br"},
                {"bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {"wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"},
                {"wr", "wn", "wb", "wq", "wk", "wb", "wn", "wr"}
        };

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Pane square = new Pane();
                square.setMinSize(0, 0);
                square.setPrefSize(100, 100);

                Color fill = ((row + col) % 2 == 0) ? Color.BLANCHEDALMOND : Color.BURLYWOOD;
                square.setStyle("-fx-background-color: #" + fill.toString().substring(2));


                chessboardGrid.add(square, col, row);

                String piece = initialState[row][col];
                if (piece != null) {
                    Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pieces/" + piece + ".png")));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(50);  // Set appropriate size
                    imageView.setFitHeight(50);

                    ChessPiece chessPiece = new ChessPiece(piece, row, col, imageView);
                    chessboard[row][col] = chessPiece;

                    setupPieceDragAndDrop(chessPiece);

                    square.getChildren().add(imageView);

                    imageView.fitWidthProperty().bind(square.widthProperty());
                    imageView.fitHeightProperty().bind(square.heightProperty());
                }
            }
        }
    }

    private ChessPiece getKing(char player) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = chessboard[row][col];
                if (piece != null && piece.getType().equals(player + "k")) {
                    return piece;
                }
            }
        }
        return null;
    }

    private List<int[]> getPathToKing(ChessPiece checkingPiece, ChessPiece king) {
        List<int[]> path = new ArrayList<>();

        int rowDirection = Integer.compare(king.getRow(), checkingPiece.getRow());
        int colDirection = Integer.compare(king.getCol(), checkingPiece.getCol());

        int currentRow = checkingPiece.getRow() + rowDirection;
        int currentCol = checkingPiece.getCol() + colDirection;

        while (currentRow != king.getRow() || currentCol != king.getCol()) {
            path.add(new int[]{currentRow, currentCol});
            currentRow += rowDirection;
            currentCol += colDirection;
        }

        return path;
    }

    private boolean isSquareUnderAttack(int targetRow, int targetCol, char opponentColor) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = chessboard[row][col];
                // Check if the piece belongs to the opponent
                if (piece != null && piece.getType().charAt(0) == opponentColor) {
                    if (canPieceAttack(piece, targetRow, targetCol)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private boolean canPieceAttack(ChessPiece piece, int targetRow, int targetCol) {
        int rowDiff = Math.abs(piece.getRow() - targetRow);
        int colDiff = Math.abs(piece.getCol() - targetCol);

        switch (piece.getType().charAt(1)) { // Determine piece type (e.g., 'wq' for white queen)
            case 'p': // Pawn
                if (piece.getType().charAt(0) == 'w') {
                    // White pawns attack diagonally upward
                    return rowDiff == 1 && colDiff == 1 && (targetRow < piece.getRow());
                } else {
                    // Black pawns attack diagonally downward
                    return rowDiff == 1 && colDiff == 1 && (targetRow > piece.getRow());
                }
            case 'r': // Rook
                return (rowDiff == 0 || colDiff == 0) && isPathClear(piece, targetRow, targetCol);
            case 'b': // Bishop
                return rowDiff == colDiff && isPathClear(piece, targetRow, targetCol);
            case 'q': // Queen
                return (rowDiff == colDiff || rowDiff == 0 || colDiff == 0) && isPathClear(piece, targetRow, targetCol);
            case 'k': // King
                return rowDiff <= 1 && colDiff <= 1;
            case 'n': // Knight
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
            default:
                return false;
        }
    }


    private boolean isPathClear(ChessPiece piece, int targetRow, int targetCol) {
        int rowStep = Integer.compare(targetRow, piece.getRow());
        int colStep = Integer.compare(targetCol, piece.getCol());

        int currentRow = piece.getRow() + rowStep;
        int currentCol = piece.getCol() + colStep;

        while (currentRow != targetRow || currentCol != targetCol) {
            if (chessboard[currentRow][currentCol] != null) {
                return false; // Path is blocked
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        return true;
    }


    private boolean canBypassCheck(ChessPiece king, ChessPiece checkingPiece) {
        // Check if the king can move out of check
        for (int row = king.getRow() - 1; row <= king.getRow() + 1; row++) {
            for (int col = king.getCol() - 1; col <= king.getCol() + 1; col++) {
                if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                    if (isValidMove(king, row, col) && !isSquareUnderAttack(row, col, checkingPiece.getType().charAt(0))) {
                        return true;
                    }
                }
            }
        }

        // Check if any other piece can capture the checking piece
        char opponentColor = checkingPiece.getType().charAt(0) == 'w' ? 'b' : 'w';
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece currentPiece = chessboard[row][col];
                if (currentPiece != null && currentPiece.getType().charAt(0) == opponentColor) {
                    if (isValidMove(currentPiece, checkingPiece.getRow(), checkingPiece.getCol())) {
                        return true;
                    }
                }
            }
        }

        // Check if any piece can block the check
        List<int[]> pathToKing = getPathToKing(checkingPiece, king);
        for (int[] square : pathToKing) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    ChessPiece currentPiece = chessboard[row][col];
                    if (currentPiece != null && currentPiece.getType().charAt(0) == opponentColor) {
                        if (isValidMove(currentPiece, square[0], square[1])) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isCheck(ChessPiece piece, int newRow, int newCol) {
        // Get the opponent's king
        ChessPiece opponentKing = getOpponentKing(piece.getType().charAt(0));
        System.out.println("opponentKing: " + opponentKing.getType() + " at " + opponentKing.getRow() + ", " + opponentKing.getCol());

        // 1. Direct check
        if (isValidMove(piece, opponentKing.getRow(), opponentKing.getCol())) {
            highlightKingSquare(opponentKing);
            return true;
        }

        // Check if the king is still under attack due to other pieces
        if (isSquareUnderAttack(opponentKing.getRow(), opponentKing.getCol(), piece.getType().charAt(0))) {
            highlightKingSquare(opponentKing);
            return true;
        }

        // 2. Discovered check
        // Temporarily move the piece
        ChessPiece temp = chessboard[newRow][newCol];
        chessboard[newRow][newCol] = piece;
        chessboard[piece.getRow()][piece.getCol()] = null;
        int originalRow = piece.getRow();
        int originalCol = piece.getCol();
        piece.setRow(newRow);
        piece.setCol(newCol);

        // Check if any of the current player's pieces can attack the opponent's king
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece currentPiece = chessboard[row][col];
                if (currentPiece != null && currentPiece.getType().charAt(0) == piece.getType().charAt(0)
                        && isValidMove(currentPiece, opponentKing.getRow(), opponentKing.getCol())) {
                    // Undo the move
                    chessboard[originalRow][originalCol] = piece;
                    chessboard[newRow][newCol] = temp;
                    piece.setRow(originalRow);
                    piece.setCol(originalCol);
                    highlightKingSquare(opponentKing);
                    return true;
                }
            }
        }

        // Undo the move
        chessboard[originalRow][originalCol] = piece;
        chessboard[newRow][newCol] = temp;
        piece.setRow(originalRow);
        piece.setCol(originalCol);

        // Only remove the highlight if it's the opponent's turn and their king is no longer under attack
        char opponentColor = piece.getType().charAt(0) == 'w' ? 'b' : 'w';
        if (!isSquareUnderAttack(opponentKing.getRow(), opponentKing.getCol(), opponentColor)) {
            removeCheckHighlight(opponentKing);
        }

        return false;
    }

    private void highlightKingSquare(ChessPiece king) {
        System.out.println("King position: Row = " + king.getRow() + ", Col = " + king.getCol());
        Pane kingSquare = getSquare(king.getRow(), king.getCol());
        previousCheckKing = kingSquare;
        pRow = king.getRow();
        pCol = king.getCol();
        System.out.println("King's square found.");
        kingSquare.setStyle("-fx-background-color: red;");

    }

    private void removeCheckHighlight(ChessPiece king) {
        if(previousCheckKing != null)
            if ((pRow + pCol) % 2 == 0) {
                previousCheckKing.setStyle("-fx-background-color: BLANCHEDALMOND;"); // White square
            } else {
                previousCheckKing.setStyle("-fx-background-color: BURLYWOOD;"); // Black square
            }
    }


    private ChessPiece getOpponentKing(char player) {
        char opponent = player == 'w' ? 'b' : 'w';
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = chessboard[row][col];
                if (piece != null && piece.getType().equals(opponent + "k")) {
                    return piece;
                }
            }
        }
        return null;
    }

    private void showAvailableMoves(ChessPiece piece) {
        List<int[]> availableMoves = new ArrayList<>();
        int row = piece.getRow();
        int col = piece.getCol();

        // Check all possible moves for the piece
        for (int newRow = 0; newRow < 8; newRow++) {
            for (int newCol = 0; newCol < 8; newCol++) {
                if (isValidMove(piece, newRow, newCol)) {
                    availableMoves.add(new int[]{newRow, newCol});
                }
            }
        }

        // Highlight the available moves
        for (int[] move : availableMoves) {
            int newRow = move[0];
            int newCol = move[1];

            Pane square = getSquare(newRow, newCol);
            if (square != null) {
                Rectangle highlight = new Rectangle();
                highlight.setFill(Color.rgb(0, 255, 0, 0.5));
                highlight.widthProperty().bind(square.widthProperty());
                highlight.heightProperty().bind(square.heightProperty());
                square.getChildren().add(highlight);
            }

        }
    }


    private void setupPieceDragAndDrop(ChessPiece chessPiece) {
        ImageView imageView = chessPiece.getImageView();

        final double[] mouseAnchorX = new double[1];
        final double[] mouseAnchorY = new double[1];

        // Floating effect on press
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), imageView);
        scaleUp.setToX(1.2);
        scaleUp.setToY(1.2);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), imageView);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        // When mouse is pressed, apply floating effect and show available moves
        imageView.setOnMousePressed(event -> {
            scaleUp.play();
            draggedPiece = chessPiece;
            draggedPieceOriginalRow = chessPiece.getRow();
            draggedPieceOriginalCol = chessPiece.getCol();
            mouseAnchorX[0] = event.getSceneX() - imageView.getTranslateX();
            mouseAnchorY[0] = event.getSceneY() - imageView.getTranslateY();

            // Remove the piece from its current square
            Pane currentSquare = getSquare(draggedPieceOriginalRow, draggedPieceOriginalCol);
            currentSquare.toFront();


            // Show available moves
            showAvailableMoves(chessPiece);


        });

        // When mouse is dragged, follow the cursor
        imageView.setOnMouseDragged(event -> {
            double newTranslateX = event.getSceneX() - mouseAnchorX[0];
            double newTranslateY = event.getSceneY() - mouseAnchorY[0];
            imageView.setTranslateX(newTranslateX);
            imageView.setTranslateY(newTranslateY);
        });

        // When mouse is released, snap to the nearest valid position
        imageView.setOnMouseReleased(event -> {
            scaleDown.play(); // Reset the size

            // Calculate the nearest column and row
            double sceneX = event.getSceneX();
            double sceneY = event.getSceneY();
            double cellWidth = chessboardGrid.getWidth() / 8;
            double cellHeight = chessboardGrid.getHeight() / 8;

            int newCol = (int) ((sceneX - chessboardGrid.getLayoutX()) / cellWidth);
            int newRow = (int) ((sceneY - chessboardGrid.getLayoutY()) / cellHeight);

            // Ensure the piece lands within the board boundaries and follows game rules
            if (newRow < 0 || newRow >= 8 || newCol < 0 || newCol >= 8 || !isValidMove(chessPiece, newRow, newCol)) {
                newRow = draggedPieceOriginalRow;
                newCol = draggedPieceOriginalCol;

            }
            // Check if the current player is in check before making the move


            // Snap the piece to the final position
            chessboard[draggedPieceOriginalRow][draggedPieceOriginalCol] = null;
            chessboard[newRow][newCol] = chessPiece;
            chessPiece.setRow(newRow);
            chessPiece.setCol(newCol);

            if (chessPiece.getType().charAt(1) == 'p') {
                handlePawnPromotion(chessPiece);
            }
            if (isCheck(chessPiece, newRow, newCol)) {
                System.out.println("Check!");
            }
            final int targetRow = newRow;
            final int targetCol = newCol;

            chessboard[draggedPieceOriginalRow][draggedPieceOriginalCol] = null;
            chessboard[targetRow][targetCol] = chessPiece;
            chessPiece.setRow(targetRow);
            chessPiece.setCol(targetCol);

            imageView.setTranslateX(0);
            imageView.setTranslateY(0);

            // remove available moves
            for (Node node : chessboardGrid.getChildren()) {
                if (node instanceof Pane) {
                    Pane square = (Pane) node;
                    square.getChildren().removeIf(child -> child instanceof Rectangle);
                }
            }


            // Add the piece to the new square
            Pane currentSquare = getSquare(draggedPieceOriginalRow, draggedPieceOriginalCol);
            Pane newSquare = getSquare(targetRow, targetCol);

            if (currentSquare != null) {
                currentSquare.getChildren().remove(imageView);
            }

            if (newSquare != currentSquare) {
                isWhiteTurn = !isWhiteTurn;
            }

            // Get image view of the target piece
            ImageView targetPieceImageView = null;
            if (newSquare != null && newSquare.getChildren().size() > 0) {
                targetPieceImageView = (ImageView) newSquare.getChildren().get(0);
            }

            // Remove the target piece from the board
            if (targetPieceImageView != null) {
                    newSquare.getChildren().remove(targetPieceImageView);
            }

            if (newSquare != null) {
                newSquare.getChildren().add(imageView);
            }
        });
    }

    private boolean isKingInCheck(char playerColor) {
        ChessPiece king = getKing(playerColor);
        if (king == null) return false;

        char opponentColor = (playerColor == 'w') ? 'b' : 'w';
        return isSquareUnderAttack(king.getRow(), king.getCol(), opponentColor);
    }

    private boolean isValidMove(ChessPiece piece, int newRow, int newCol) {
        if (!isWithinBounds(newRow, newCol)) {
            return false;
        }

        if (!isPlayerTurn(piece)) {
            return false;
        }

        ChessPiece targetPiece = chessboard[newRow][newCol];
        if (isFriendlyFire(piece, targetPiece)) {
            return false;
        }

        // Temporarily make the move
        ChessPiece temp = chessboard[newRow][newCol];
        int originalRow = piece.getRow();
        int originalCol = piece.getCol();

        chessboard[originalRow][originalCol] = null;
        chessboard[newRow][newCol] = piece;
        piece.setRow(newRow);
        piece.setCol(newCol);

        // Check if the king is in check after the move
        boolean isKingSafe = !isKingInCheck(piece.getType().charAt(0));

        // Undo the move
        chessboard[newRow][newCol] = temp;
        chessboard[originalRow][originalCol] = piece;
        piece.setRow(originalRow);
        piece.setCol(originalCol);

        if (!isKingSafe) {
            return false;
        }

        // Validate based on piece type
        switch (piece.getType().charAt(1)) {
            case 'p': // Pawn
                return isValidPawnMove(piece, newRow, newCol, targetPiece);
            case 'r': // Rook
                return isValidRookMove(piece, newRow, newCol);
            case 'n': // Knight
                return isValidKnightMove(piece, newRow, newCol);
            case 'b': // Bishop
                return isValidBishopMove(piece, newRow, newCol);
            case 'q': // Queen
                return isValidQueenMove(piece, newRow, newCol);
            case 'k': // King
                return isValidKingMove(piece, newRow, newCol);
            default:
                return false;
        }
    }

    private boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private boolean isPlayerTurn(ChessPiece piece) {
        return (isWhiteTurn && piece.getType().charAt(0) == 'w') || (!isWhiteTurn && piece.getType().charAt(0) == 'b');
    }

    private boolean isFriendlyFire(ChessPiece piece, ChessPiece targetPiece) {
        return targetPiece != null && targetPiece.getType().charAt(0) == piece.getType().charAt(0);
    }

    private boolean isValidPawnMove(ChessPiece piece, int newRow, int newCol, ChessPiece targetPiece) {
        int direction = piece.getType().charAt(0) == 'w' ? -1 : 1;
        int startRow = piece.getType().charAt(0) == 'w' ? 6 : 1;

        if (newCol == piece.getCol() && newRow == piece.getRow() + direction) {
            return targetPiece == null;
        }

        if (newCol == piece.getCol() && piece.getRow() == startRow && newRow == piece.getRow() + 2 * direction) {
            return targetPiece == null && chessboard[piece.getRow() + direction][newCol] == null;
        }

        if (Math.abs(newCol - piece.getCol()) == 1 && newRow == piece.getRow() + direction) {
            return targetPiece != null && targetPiece.getType().charAt(0) != piece.getType().charAt(0);
        }

        return false;
    }

    private boolean isValidRookMove(ChessPiece piece, int newRow, int newCol) {
        if (newRow != piece.getRow() && newCol != piece.getCol()) {
            return false;
        }
        return isPathClear(piece.getRow(), piece.getCol(), newRow, newCol);
    }

    private boolean isValidKnightMove(ChessPiece piece, int newRow, int newCol) {
        int rowDiff = Math.abs(newRow - piece.getRow());
        int colDiff = Math.abs(newCol - piece.getCol());
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private boolean isValidBishopMove(ChessPiece piece, int newRow, int newCol) {
        if (Math.abs(newRow - piece.getRow()) != Math.abs(newCol - piece.getCol())) {
            return false;
        }
        return isPathClear(piece.getRow(), piece.getCol(), newRow, newCol);
    }

    private boolean isValidQueenMove(ChessPiece piece, int newRow, int newCol) {
        if (newRow != piece.getRow() && newCol != piece.getCol() &&
                Math.abs(newRow - piece.getRow()) != Math.abs(newCol - piece.getCol())) {
            return false;
        }
        return isPathClear(piece.getRow(), piece.getCol(), newRow, newCol);
    }

    private boolean isValidKingMove(ChessPiece piece, int newRow, int newCol) {
        int rowDiff = Math.abs(newRow - piece.getRow());
        int colDiff = Math.abs(newCol - piece.getCol());
        return rowDiff <= 1 && colDiff <= 1;
    }

    private boolean isPathClear(int startRow, int startCol, int endRow, int endCol) {
        int rowStep = Integer.compare(endRow, startRow);
        int colStep = Integer.compare(endCol, startCol);

        int currentRow = startRow + rowStep;
        int currentCol = startCol + colStep;

        while (currentRow != endRow || currentCol != endCol) {
            if (chessboard[currentRow][currentCol] != null) {
                return false; // Path is obstructed
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }

    private Pane getSquare(int row, int col) {
        for (Node node : chessboardGrid.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return (Pane) node;
            }
        }
        return null;
    }

    private void handlePawnPromotion(ChessPiece pawn) {
        if ((pawn.getType().charAt(0) == 'w' && pawn.getRow() == 0) ||
                (pawn.getType().charAt(0) == 'b' && pawn.getRow() == 7)) {
            promptPromotion(pawn);
        }
    }

    private void promptPromotion(ChessPiece pawn) {
        // Example using JavaFX ChoiceDialog
        List<String> options = List.of("Queen", "Rook", "Bishop", "Knight");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Queen", options);
        dialog.setTitle("Pawn Promotion");
        dialog.setHeaderText("Promote your pawn!");
        dialog.setContentText("Choose a piece:");

        dialog.showAndWait().ifPresent(choice -> {
            switch (choice) {
                case "Queen" -> promotePawn(pawn, "q");
                case "Rook" -> promotePawn(pawn, "r");
                case "Bishop" -> promotePawn(pawn, "b");
                case "Knight" -> promotePawn(pawn, "n");
            }
        });
    }

    private void promotePawn(ChessPiece pawn, String newPieceType) {
        char playerColor = pawn.getType().charAt(0); // 'w' or 'b'
        String newType = playerColor + newPieceType; // e.g., "wq" for white queen

        // Create a new ChessPiece object for the promoted piece
        ChessPiece promotedPiece = new ChessPiece(newType, pawn.getRow(), pawn.getCol(), createPieceImage(newType));

        // Update the chessboard model
        chessboard[pawn.getRow()][pawn.getCol()] = promotedPiece;

        // Update the UI
        Pane square = getSquare(pawn.getRow(), pawn.getCol());
        if (square != null) {
            // Clear the square of the old pawn's image
            square.getChildren().clear();

            // Add the new promoted piece's image
            square.getChildren().add(promotedPiece.getImageView());
        }

        Platform.runLater(() -> {
            square.getChildren().clear();
            square.getChildren().add(promotedPiece.getImageView());
        });

        setupPieceDragAndDrop(promotedPiece);

    }


    private ImageView createPieceImage(String type) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pieces/" + type + ".png")));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);  // Set appropriate size
        imageView.setFitHeight(100);
        return imageView;
    }
}
