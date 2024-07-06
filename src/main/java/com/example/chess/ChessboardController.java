package com.example.chess;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class ChessboardController implements Initializable {

    @FXML
    private GridPane chessboardGrid; // Inject the GridPane from FXML


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Pane square = new Pane();
                square.setPrefSize(50, 50); // Set square size

                // Alternating colors (chessboard pattern)
                Color fill = ((row + col) % 2 == 0) ? Color.WHITE : Color.DARKGRAY;
                square.setStyle("-fx-background-color: #" + fill.toString().substring(2)); // Convert to hex

                chessboardGrid.add(square, col, row); // Add square to the grid
                // test
            }
        }
    }
}