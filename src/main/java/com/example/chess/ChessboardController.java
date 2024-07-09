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
    private GridPane chessboardGrid; // Inject the GridPane from FXML

    private ChessPiece[][] chessboard = new ChessPiece[8][8];

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupChessboardGrid();
    }

    private void setupChessboardGrid() {
        // Define the initial state of the chessboard
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
                square.setMinSize(50, 50); // Set minimum square size
                square.setPrefSize(100, 100); // Set preferred square size

                // Alternating colors (chessboard pattern)
                Color fill = ((row + col) % 2 == 0) ? Color.BLANCHEDALMOND: Color.BURLYWOOD;
                square.setStyle("-fx-background-color: #" + fill.toString().substring(2)); // Convert to hex

                chessboardGrid.add(square, col, row); // Add square to the grid

                String piece = initialState[row][col];
                if (piece != null) {
                    // Load the image
                    Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pieces/" + piece + ".png")));
                    // Create a ChessPiece object
                    ChessPiece chessPiece = new ChessPiece(image, chessboardGrid);
                    chessboard[row][col] = chessPiece;
                    square.getChildren().add(chessPiece.getImageView());

                    // Bind the size of the ChessPiece to the size of the square
                    chessPiece.getImageView().fitWidthProperty().bind(square.widthProperty());
                    chessPiece.getImageView().fitHeightProperty().bind(square.heightProperty());
                }
            }
        }
    }
}