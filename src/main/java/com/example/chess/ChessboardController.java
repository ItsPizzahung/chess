package com.example.chess;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.image.Image ;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ChessboardController implements Initializable {

    @FXML
    private GridPane chessboardGrid; // Inject the GridPane from FXML

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupChessboardGrid();
        chessboardGrid.widthProperty().addListener((obs, oldWidth, newWidth) -> resizeSquares());
        chessboardGrid.heightProperty().addListener((obs, oldHeight, newHeight) -> resizeSquares());

        // Add padding to the top of the grid
//        chessboardGrid.setPadding(new Insets(30, 0, 0, 0)); // 30px padding at the top

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
                Color fill = ((row + col) % 2 == 0) ? Color.BLANCHEDALMOND: Color.BURLYWOOD;
                square.setStyle("-fx-background-color: #" + fill.toString().substring(2)); // Convert to hex

                chessboardGrid.add(square, col, row); // Add square to the grid

                // If there is a piece on this square, add it
                String piece = initialState[row][col];
                if (piece != null) {
                    // Load the image
                    Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pieces/" + piece + ".png")));
                    // Create an ImageView object
                    ImageView imageView = new ImageView(image);

                    // Bind the size of the ImageView to the size of the square
                    imageView.fitWidthProperty().bind(square.widthProperty());
                    imageView.fitHeightProperty().bind(square.heightProperty());
                    imageView.setPreserveRatio(true); // Preserve aspect ratio

                    // Add the ImageView object to the pane
                    square.getChildren().add(imageView);

                    // Center the ImageView in the square
                    GridPane.setMargin(imageView, new Insets(10)); // Add 10px margin on all sides
                }
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
