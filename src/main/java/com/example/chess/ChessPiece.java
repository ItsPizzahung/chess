package com.example.chess;

import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class ChessPiece {
    private final ImageView imageView;
    private final GridPane chessboardGrid;

    public ChessPiece(Image image, GridPane chessboardGrid) {
        this.imageView = new ImageView(image);
        this.chessboardGrid = chessboardGrid;
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
            imageView.relocate(closestCol * imageView.getFitWidth(), closestRow * imageView.getFitHeight());
        });

        imageView.setOnMouseDragged(mouseEvent -> {
            imageView.relocate(mouseEvent.getSceneX() + dragDelta.x, mouseEvent.getSceneY() + dragDelta.y);
        });

        imageView.setOnMouseEntered(mouseEvent -> imageView.setCursor(Cursor.HAND));
    }

    private static class Delta { double x, y; }
}