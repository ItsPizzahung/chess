package com.example.chess;

import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class ChessPiece {
    private String type;
    private int row;
    private int col;
    private ImageView imageView;

    public ChessPiece(String type, int row, int col, ImageView imageView) {
        this.type = type;
        this.row = row;
        this.col = col;
        this.imageView = imageView;
    }

    public String getType() {
        return type;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public ImageView getImageView() {
        return imageView;
    }
}