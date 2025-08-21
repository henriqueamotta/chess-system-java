package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            GridPane gridPane = new GridPane();
            int squareSize = 80; // Tamanho de cada quadrado do tabuleiro
            int boardSize = 8;

            // Loop para criar as 64 casas do tabuleiro
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    Rectangle square = new Rectangle(squareSize, squareSize);

                    // Define a cor do quadrado (alternando entre claro e escuro)
                    if ((row + col) % 2 == 0) {
                        square.setFill(Color.web("#F0D9B5")); // Cor clara
                    } else {
                        square.setFill(Color.web("#B58863")); // Cor escura
                    }
                    
                    gridPane.add(square, col, row);
                }
            }

            // Cria a cena com o tabuleiro (GridPane)
            Scene scene = new Scene(gridPane);

            primaryStage.setTitle("Chess System with JavaFX"); // Título
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
