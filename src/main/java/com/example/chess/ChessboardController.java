package com.example.chess;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
//    private Pane dragLayer;

    private boolean isWhiteTurn = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupChessboardGrid();
//        setupDragLayer();
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

    private boolean isCheck(ChessPiece piece, int newRow, int newCol) {

        // Get the opponent's king
        ChessPiece opponentKing = getOpponentKing(piece.getType().charAt(0));
        System.out.println("opponentKing: " + opponentKing.getType() + " at " + opponentKing.getRow() + ", " + opponentKing.getCol());
        // 1. Direct check
        if (isValidMove(piece, opponentKing.getRow(), opponentKing.getCol())) {
            highlightKingSquare(opponentKing);
            return true;
        }


        // 2. Discovered check
        // Temporarily move the piece
        ChessPiece temp = chessboard[newRow][newCol];
        chessboard[newRow][newCol] = piece;
        chessboard[piece.getRow()][piece.getCol()] = null;
        piece.setRow(newRow);
        piece.setCol(newCol);

        // Check if any of the current player's pieces can attack the opponent's king
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece currentPiece = chessboard[row][col];
                if (currentPiece != null && currentPiece.getType().charAt(0) == piece.getType().charAt(0)
                        && isValidMove(currentPiece, opponentKing.getRow(), opponentKing.getCol())) {
                    // Undo the move
                    chessboard[piece.getRow()][piece.getCol()] = piece;
                    chessboard[newRow][newCol] = temp;
                    piece.setRow(row);
                    piece.setCol(col);
                    highlightKingSquare(opponentKing);
                    return true;
                }
            }
        }

        // Undo the move
        chessboard[piece.getRow()][piece.getCol()] = piece;
        chessboard[newRow][newCol] = temp;
        piece.setRow(newRow);
        piece.setCol(newCol);

        // 3. En passant
        // This will require additional logic to handle correctly
        removeCheckHighlight(opponentKing);
        return false;
    }


    private void highlightKingSquare(ChessPiece king) {
        System.out.println("King position: Row = " + king.getRow() + ", Col = " + king.getCol());
        Pane kingSquare = getSquare(king.getRow(), king.getCol());
        previousCheckKing = kingSquare;
        System.out.println("King's square found.");
        kingSquare.setStyle("-fx-background-color: red;");

    }

    private void removeCheckHighlight(ChessPiece king) {
        if(previousCheckKing != null)
           previousCheckKing.setStyle("-fx-background-color: transparent;");
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


            if (isCheck(chessPiece, newRow, newCol)) {
                System.out.println("Check!");
            }
            // Check if the move puts the opponent in check


            // Snap the piece to the final position
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

//            if (isCheck(chessPiece, targetRow, targetCol)) {
//                System.out.println("Check!");
//            }


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


//            capturePiece(chessPiece, targetRow, targetCol);

        });
    }


    private void movePieceWithAnimation(ChessPiece piece, int newRow, int newCol) {
        double targetX = newCol * (chessboardGrid.getWidth() / 8);
        double targetY = newRow * (chessboardGrid.getHeight() / 8);

        // TranslateTransition for smooth movement
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), piece.getImageView());
        translateTransition.setToX(targetX);
        translateTransition.setToY(targetY);

        // Timeline for bouncing effect
        Timeline bounceTimeline = new Timeline();
        KeyValue keyValueUp = new KeyValue(piece.getImageView().translateYProperty(), targetY - 10); // Up by 10 pixels
        KeyValue keyValueDown = new KeyValue(piece.getImageView().translateYProperty(), targetY);

        KeyFrame keyFrameUp = new KeyFrame(Duration.millis(400), keyValueUp);
        KeyFrame keyFrameDown = new KeyFrame(Duration.millis(500), keyValueDown);

        bounceTimeline.getKeyFrames().addAll(keyFrameUp, keyFrameDown);

        // Play the animations in sequence
        translateTransition.setOnFinished(event -> bounceTimeline.play());
        translateTransition.play();

        // Update chessboard state and reset position after animation
        translateTransition.setOnFinished(event -> {
            chessboard[piece.getRow()][piece.getCol()] = null;
            chessboard[newRow][newCol] = piece;
            piece.setRow(newRow);
            piece.setCol(newCol);

            piece.getImageView().setTranslateX(targetX);
            piece.getImageView().setTranslateY(targetY);

            Pane targetSquare = getSquare(newRow, newCol);
            if (targetSquare != null) {
                targetSquare.getChildren().add(piece.getImageView());
            }
        });
    }


    private boolean isValidMove(ChessPiece piece, int newRow, int newCol) {
        // white moves first
        if (isWhiteTurn && piece.getType().charAt(0) == 'b') {
            return false;
        } else if (!isWhiteTurn && piece.getType().charAt(0) == 'w') {
            return false;
        }

        // Check if the new position is within the bounds of the chessboard
        if (newRow < 0 || newRow >= 8 || newCol < 0 || newCol >= 8) {
            return false;
        }

        // Check if the destination square is occupied by a piece of the same color
        ChessPiece targetPiece = chessboard[newRow][newCol];
        if (targetPiece != null && targetPiece.getType().charAt(0) == piece.getType().charAt(0)) {
            return false; // Prevent moving to a square occupied by a piece of the same color
        }


        // Pawn movement logic
        if (piece.getType().charAt(1) == 'p') {
            int direction = piece.getType().charAt(0) == 'w' ? -1 : 1; // White moves up (-1), Black moves down (+1)
            int startRow = piece.getType().charAt(0) == 'w' ? 6 : 1;    // White pawns start at row 6, Black at row 1

            // Move forward 1 square
            if (newCol == piece.getCol() && newRow == piece.getRow() + direction) {
                return targetPiece == null; // The square must be empty
            }

            // Move forward 2 squares from the initial position
            if (newCol == piece.getCol() && piece.getRow() == startRow && newRow == piece.getRow() + 2 * direction) {
                return targetPiece == null && chessboard[piece.getRow() + direction][newCol] == null; // Both squares must be empty
            }

            // Diagonal capture
            if (Math.abs(newCol - piece.getCol()) == 1 && newRow == piece.getRow() + direction) {
                return targetPiece != null && targetPiece.getType().charAt(0) != piece.getType().charAt(0); // Must capture opposite color
            }


            return false; // All other moves are invalid for a pawn
        }

        // Rook movement logic
        if (piece.getType().charAt(1) == 'r') {
            // Rooks can move horizontally or vertically
            if (newRow != piece.getRow() && newCol != piece.getCol()) {
                return false; // Rooks cannot move diagonally
            }

            // Check for obstructions along the path
            int rowStep = newRow == piece.getRow() ? 0 : (newRow > piece.getRow() ? 1 : -1);
            int colStep = newCol == piece.getCol() ? 0 : (newCol > piece.getCol() ? 1 : -1);

            int currentRow = piece.getRow() + rowStep;
            int currentCol = piece.getCol() + colStep;

            while (currentRow != newRow || currentCol != newCol) {
                if (chessboard[currentRow][currentCol] != null) {
                    return false; // Path is obstructed
                }
                currentRow += rowStep;
                currentCol += colStep;
            }

            return targetPiece == null || targetPiece.getType().charAt(0) != piece.getType().charAt(0); // Must be empty or capture opposite color

        }

        // Knight movement logic
        if (piece.getType().charAt(1) == 'n') {
            // Knights move in an L-shape: 2 squares in one direction, then 1 square perpendicular
            int rowDiff = Math.abs(newRow - piece.getRow());
            int colDiff = Math.abs(newCol - piece.getCol());

            return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2); // Valid L-shape
        }

        // Bishop movement logic
        if (piece.getType().charAt(1) == 'b') {
            // Bishops move diagonally
            if (Math.abs(newRow - piece.getRow()) != Math.abs(newCol - piece.getCol())) {
                return false; // Bishops must move diagonally
            }

            // Check for obstructions along the path
            int rowStep = newRow > piece.getRow() ? 1 : -1;
            int colStep = newCol > piece.getCol() ? 1 : -1;

            int currentRow = piece.getRow() + rowStep;
            int currentCol = piece.getCol() + colStep;

            while (currentRow != newRow || currentCol != newCol) {
                if (chessboard[currentRow][currentCol] != null) {
                    return false; // Path is obstructed
                }
                currentRow += rowStep;
                currentCol += colStep;
            }

            return targetPiece == null || targetPiece.getType().charAt(0) != piece.getType().charAt(0); // Must be empty or capture opposite color
        }

        // Queen movement logic
        if (piece.getType().charAt(1) == 'q') {
            // Queens can move horizontally, vertically, or diagonally
            if (newRow != piece.getRow() && newCol != piece.getCol() && Math.abs(newRow - piece.getRow()) != Math.abs(newCol - piece.getCol())) {
                return false; // Queens must move in a straight line
            }

            // Check for obstructions along the path
            int rowStep = newRow == piece.getRow() ? 0 : (newRow > piece.getRow() ? 1 : -1);
            int colStep = newCol == piece.getCol() ? 0 : (newCol > piece.getCol() ? 1 : -1);

            int currentRow = piece.getRow() + rowStep;
            int currentCol = piece.getCol() + colStep;

            while (currentRow != newRow || currentCol != newCol) {
                if (chessboard[currentRow][currentCol] != null) {
                    return false; // Path is obstructed
                }
                currentRow += rowStep;
                currentCol += colStep;
            }

            return targetPiece == null || targetPiece.getType().charAt(0) != piece.getType().charAt(0); // Must be empty or capture opposite color
        }

        // King movement logic
        if (piece.getType().charAt(1) == 'k') {
            // Kings can move 1 square in any direction
            int rowDiff = Math.abs(newRow - piece.getRow());
            int colDiff = Math.abs(newCol - piece.getCol());

            return rowDiff <= 1 && colDiff <= 1; // Valid 1-square move
        }


        return true;
    }

    private void movePiece(ChessPiece piece, int newRow, int newCol) {
        // Remove the piece from the original position in the array
        chessboard[piece.getRow()][piece.getCol()] = null;

        // If capturing a piece, remove it from the board
        ChessPiece targetPiece = chessboard[newRow][newCol];
        if (targetPiece != null) {
            Pane targetSquare = getSquare(newRow, newCol);
            if (targetSquare != null) {
                targetSquare.getChildren().remove(targetPiece.getImageView());
            }
        }

        // Update the piece's position
        chessboard[newRow][newCol] = piece;
        piece.setRow(newRow);
        piece.setCol(newCol);

        // Reset the translation to align with the grid
        piece.getImageView().setTranslateX(0);
        piece.getImageView().setTranslateY(0);

        // Update the visual position on the board
        Pane targetSquare = getSquare(newRow, newCol);
        if (targetSquare != null) {
            targetSquare.getChildren().add(piece.getImageView());
        }
    }


    private void resetPiecePosition(ChessPiece piece) {
        // Reset translation to zero
        piece.getImageView().setTranslateX(0);
        piece.getImageView().setTranslateY(0);

        // Find the original Pane
        Pane originalSquare = getSquare(draggedPieceOriginalRow, draggedPieceOriginalCol);

        if (originalSquare != null) {
            // Remove the ImageView from its current parent if necessary
            if (piece.getImageView().getParent() != null) {
                Pane currentParent = (Pane) piece.getImageView().getParent();
                currentParent.getChildren().remove(piece.getImageView());
            }

            // Add the ImageView back to the original Pane
            if (!originalSquare.getChildren().contains(piece.getImageView())) {
                originalSquare.getChildren().add(piece.getImageView());
            }
        }
    }


    private Pane getSquare(int row, int col) {
        for (Node node : chessboardGrid.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return (Pane) node;
            }
            // know value row and col,
            // go through every single node/pane in the gridpane
            // if the row and col of the node is the same as the row and col we are looking for
            // return the node/pane
        }
        return null;
    }
}
