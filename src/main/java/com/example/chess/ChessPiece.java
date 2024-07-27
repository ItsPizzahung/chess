package com.example.chess;

import javafx.scene.Cursor;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.Arrays;
import java.util.List;

public class ChessPiece {
    private final ImageView imageView;
    private final GridPane chessboardGrid;
    private final ChessPiece[][] chessboard;
    private String type;
    private int row;
    private int col;

    public ChessPiece(Image image, GridPane chessboardGrid, String type, int row, int col, ChessPiece[][] chessboard) {
        this.imageView = new ImageView(image);
        this.chessboardGrid = chessboardGrid;
        this.type = type;
        this.row = row;
        this.col = col;
        this.chessboard = chessboard;
        setupMouseEvents();
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void move(int targetRow, int targetCol) {
        if (canMove(targetRow, targetCol, chessboard)) {
            this.row = targetRow;
            this.col = targetCol;
            chessboardGrid.getChildren().remove(imageView);
            chessboardGrid.add(imageView, col, row);
        } else {
            chessboardGrid.getChildren().remove(imageView);
            chessboardGrid.add(imageView, col, row); // Reset to original position if move is invalid
        }
    }

    public boolean canMove(int targetRow, int targetCol, ChessPiece[][] chessboard) {
        // Implement the specific movement rules for the pawn
        if (type.equals("wp")) {
            if (row == 1 && targetRow == row + 2 && targetCol == col && chessboard[targetRow][targetCol] == null) {
                return true;
            } else if (targetRow == row + 1 && targetCol == col && chessboard[targetRow][targetCol] == null) {
                return true;
            }
        } else if (type.equals("bp")) {
            if (row == 6 && targetRow == row - 2 && targetCol == col && chessboard[targetRow][targetCol] == null) {
                return true;
            } else if (targetRow == row - 1 && targetCol == col && chessboard[targetRow][targetCol] == null) {
                return true;
            }
        }
        return false;
    }

    public boolean canTake(int targetRow, int targetCol) {
        if (type.equals("wp")) {
            return targetRow == row + 1 && Math.abs(targetCol - col) == 1;
        } else if (type.equals("bp")) {
            return targetRow == row - 1 && Math.abs(targetCol - col) == 1;
        }
        return false;
    }

//    public void promote() {
//        List<String> choices = Arrays.asList("rook", "knight", "bishop", "queen");
//        ChoiceDialog<String> dialog = new ChoiceDialog<>("queen", choices);
//        dialog.setTitle("Pawn Promotion");
//        dialog.setHeaderText("Choose a piece to promote your pawn to:");
//        dialog.showAndWait().ifPresent(choice -> {
//            type = choice;
//            Image image = new Image(getClass().getResourceAsStream("/pieces/" + type + ".png"));
//            imageView.setImage(image);
//        });
//    }

//    public boolean collision(int targetRow, int targetCol) {
//        if (type.equals("wp")) {
//            return chessboard[row + 1][col] != null;
//        } else if (type.equals("bp")) {
//            return chessboard[row - 1][col] != null;
//        }
//        return false;
//    }

    private void setupMouseEvents() {
        final Delta dragDelta = new Delta();

        imageView.setOnMousePressed(mouseEvent -> {
            dragDelta.x = imageView.getLayoutX() - mouseEvent.getSceneX();
            dragDelta.y = imageView.getLayoutY() - mouseEvent.getSceneY();
            imageView.setCursor(Cursor.MOVE);
        });

        imageView.setOnMouseReleased(mouseEvent -> {
            imageView.setCursor(Cursor.HAND);
            int closestRow = (int) Math.round((imageView.getLayoutY() - chessboardGrid.getLayoutY()) / imageView.getFitHeight());
            int closestCol = (int) Math.round((imageView.getLayoutX() - chessboardGrid.getLayoutX()) / imageView.getFitWidth());
            if (canMove(closestRow, closestCol, chessboard)) {
                move(closestRow, closestCol);
            } else {
                // Snap back to original position if move is invalid
                chessboardGrid.getChildren().remove(imageView);
                chessboardGrid.add(imageView, col, row);
            }
        });

        imageView.setOnMouseDragged(mouseEvent -> {
            imageView.relocate(mouseEvent.getSceneX() + dragDelta.x, mouseEvent.getSceneY() + dragDelta.y);
        });

        imageView.setOnMouseEntered(mouseEvent -> imageView.setCursor(Cursor.HAND));
    }

    private static class Delta {
        double x, y;
    }
}
