package com.example.chess;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ChessboardController implements Initializable {

    @FXML
    private GridPane chessboardGrid;

    private ChessPiece[][] chessboard = new ChessPiece[8][8];

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
                square.setMinSize(50, 50);
                square.setPrefSize(100, 100);

                Color fill = ((row + col) % 2 == 0) ? Color.BLANCHEDALMOND : Color.BURLYWOOD;
                square.setStyle("-fx-background-color: #" + fill.toString().substring(2));

                chessboardGrid.add(square, col, row);

                String piece = initialState[row][col];
                if (piece != null) {
                    Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pieces/" + piece + ".png")));
                    ChessPiece chessPiece = new ChessPiece(image, chessboardGrid, piece, row, col, chessboard);
                    chessboard[row][col] = chessPiece;
                    square.getChildren().add(chessPiece.getImageView());

                    chessPiece.getImageView().fitWidthProperty().bind(square.widthProperty());
                    chessPiece.getImageView().fitHeightProperty().bind(square.heightProperty());
                }
            }
        }
    }
}
