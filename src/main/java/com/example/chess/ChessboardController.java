package com.example.chess;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class ChessboardController implements Initializable {

    @FXML
    private GridPane chessboardGrid; // Inject the GridPane from FXML

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupChessboardGrid();
        chessboardGrid.widthProperty().addListener((obs, oldWidth, newWidth) -> resizeSquares());
        chessboardGrid.heightProperty().addListener((obs, oldHeight, newHeight) -> resizeSquares());
    }

    private void setupChessboardGrid() {
        for (int row = 0; row < 8; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS);
            chessboardGrid.getRowConstraints().add(rowConstraints);

            for (int col = 0; col < 8; col++) {
                if (row == 0) {
                    ColumnConstraints columnConstraints = new ColumnConstraints();
                    columnConstraints.setHgrow(Priority.ALWAYS);
                    chessboardGrid.getColumnConstraints().add(columnConstraints);
                }

                Pane square = new Pane();
                square.setMinSize(50, 50); // Set minimum square size
                square.setPrefSize(100, 100); // Set preferred square size

                // Alternating colors (chessboard pattern)
                Color fill = ((row + col) % 2 == 0) ? Color.WHITE : Color.DARKGRAY;
                square.setStyle("-fx-background-color: #" + fill.toString().substring(2)); // Convert to hex

                chessboardGrid.add(square, col, row); // Add square to the grid
            }
        }
    }

    private void resizeSquares() {
        double width = chessboardGrid.getWidth();
        double height = chessboardGrid.getHeight();
        double newSize = Math.max(width / 8, height / 8);

        // Limit the size to prevent it from becoming too small
        newSize = Math.max(newSize, 50);

        for (var node : chessboardGrid.getChildren()) {
            if (node instanceof Pane) {
                ((Pane) node).setPrefSize(newSize, newSize);
                ((Pane) node).setMinSize(newSize, newSize);
                ((Pane) node).setMaxSize(newSize, newSize);
            }
        }

        // Update the column and row constraints to ensure they resize proportionally
        for (ColumnConstraints colConstraint : chessboardGrid.getColumnConstraints()) {
            colConstraint.setPercentWidth(100.0 / 8);
        }
        for (RowConstraints rowConstraint : chessboardGrid.getRowConstraints()) {
            rowConstraint.setPercentHeight(100.0 / 8);
        }
    }
}
