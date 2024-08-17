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
import javafx.util.Duration;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ChessboardController implements Initializable {

    @FXML
    private GridPane chessboardGrid;

    private ChessPiece[][] chessboard = new ChessPiece[8][8];
    private ChessPiece draggedPiece;
    private int draggedPieceOriginalRow;
    private int draggedPieceOriginalCol;
//    private Pane dragLayer;

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

//    private void setupDragLayer() {
//        dragLayer = new Pane();
//        dragLayer.setPickOnBounds(false);
//        chessboardGrid.getChildren().add(dragLayer);
//    }





















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

        // When mouse is pressed, apply floating effect
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
//            if (currentSquare != null) {
//                currentSquare.getChildren().remove(imageView);
//            }
//            dragLayer.getChildren().add(imageView);

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

            // Snap the piece to the final position
            final int targetRow = newRow;
            final int targetCol = newCol;

            chessboard[draggedPieceOriginalRow][draggedPieceOriginalCol] = null;
            chessboard[targetRow][targetCol] = chessPiece;
            chessPiece.setRow(targetRow);
            chessPiece.setCol(targetCol);

            imageView.setTranslateX(0);
            imageView.setTranslateY(0);

            // Add the piece to the new square
            Pane currentSquare = getSquare(draggedPieceOriginalRow, draggedPieceOriginalCol);
            Pane newSquare = getSquare(targetRow, targetCol);

//            dragLayer.getChildren().remove(imageView);
            if (currentSquare != null) {
                currentSquare.getChildren().remove(imageView);
            }
            if (newSquare != null) {
                newSquare.getChildren().add(imageView);
            }
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
