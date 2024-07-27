package com.example.chess;

import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

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
            move(closestRow, closestCol);
            imageView.relocate(closestCol * imageView.getFitWidth(), closestRow * imageView.getFitHeight());
        });

        imageView.setOnMouseDragged(mouseEvent -> {
            imageView.relocate(mouseEvent.getSceneX() + dragDelta.x, mouseEvent.getSceneY() + dragDelta.y);
        });

        imageView.setOnMouseEntered(mouseEvent -> imageView.setCursor(Cursor.HAND));
    }

    private void move(int targetRow, int targetCol) {
        // Check if the target position is within the bounds of the chessboard
        if (targetRow < 0 || targetRow >= 8 || targetCol < 0 || targetCol >= 8) {
            return;
        }

        this.row = targetRow;
        this.col = targetCol;
        chessboardGrid.getChildren().remove(imageView);
        chessboardGrid.add(imageView, col, row);
        imageView.relocate(col * imageView.getFitWidth(), row * imageView.getFitHeight());
    }

    private static class Delta {
        double x, y;
    }
}