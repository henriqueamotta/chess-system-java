package application;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.Color;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    // "Backend" com a lógica do jogo
    private ChessMatch chessMatch = new ChessMatch();

    @Override
    public void start(Stage primaryStage) {
        try {
            GridPane gridPane = new GridPane();
            int squareSize = 80;
            int boardSize = 8;

            // Loop para criar as 64 casas do tabuleiro
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    Rectangle square = new Rectangle(squareSize, squareSize);

                    // Define a cor do quadrado
                    if ((row + col) % 2 == 0) {
                        square.setFill(Paint.valueOf("#F0D9B5")); // Cor clara
                    } else {
                        square.setFill(Paint.valueOf("#B58863")); // Cor escura
                    }
                    
                    // Cria um StackPane para colocar a peça sobre o quadrado
                    StackPane stackPane = new StackPane();
                    stackPane.getChildren().add(square);
                    gridPane.add(stackPane, col, row);
                }
            }

            // Desenha as peças na posição inicial
            drawPieces(gridPane);

            Scene scene = new Scene(gridPane);
            primaryStage.setTitle("Chess System with JavaFX"); // Nome na barra de título
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para desenhar as peças
    private void drawPieces(GridPane gridPane) {
        ChessPiece[][] pieces = chessMatch.getPieces();
        for (int row = 0; row < pieces.length; row++) {
            for (int col = 0; col < pieces[row].length; col++) {
                if (pieces[row][col] != null) {
                    Label pieceLabel = new Label(pieces[row][col].toString());
                    pieceLabel.setFont(new Font("Arial", 50)); // Aumenta o tamanho da fonte para a peça
                    
                    // Define a cor da peça
                    if (pieces[row][col].getColor() == Color.WHITE) {
                        pieceLabel.setTextFill(Paint.valueOf("#FFFFFF"));
                    } else {
                        pieceLabel.setTextFill(Paint.valueOf("#000000"));
                    }

                    // Adiciona a peça (Label) ao StackPane na posição correta
                    StackPane stackPane = (StackPane) gridPane.getChildren().get(row * 8 + col);
                    stackPane.getChildren().add(pieceLabel);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
