package application;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
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
    private GridPane gridPane = new GridPane(); // Tornando o gridPane um campo da classe

    // Variáveis para guardar a origem e o destino da jogada
    private ChessPosition source;
    private ChessPosition target;

    @Override
    public void start(Stage primaryStage) {
        try {
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
                    
                    StackPane stackPane = new StackPane();
                    stackPane.getChildren().add(square);
                    gridPane.add(stackPane, col, row);

                    // Adiciona o "ouvinte" de clique a cada quadrado
                    final int finalRow = row;
                    final int finalCol = col;
                    stackPane.setOnMouseClicked(event -> {
                        // Converte a coordenada da matriz (col, row) para ChessPosition
                        handleSquareClick(ChessPosition.fromMatrixPosition(finalCol, finalRow));
                    });
                }
            }

            // Desenha as peças na posição inicial
            refreshBoard();

            Scene scene = new Scene(gridPane);
            primaryStage.setTitle("Chess System with JavaFX");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para lidar com os cliques, recebendo ChessPosition
    private void handleSquareClick(ChessPosition position) {
        if (source == null) {
            source = position;
            // Futuramente, adicionar um destaque visual para a peça selecionada aqui
        } else {
            target = position;
            try {
                chessMatch.performChessMove(source, target);
            }
            catch (ChessException e) {
                // Se o movimento for inválido, mostrar um alerta na tela
                System.out.println("Erro: " + e.getMessage());
            }

            // Independentemente de o movimento ser válido ou não, o tabuleiro
            // é atualizado e preparado para a próxima jogada.
            refreshBoard();
            source = null;
            target = null;
        }
    }

    // Renomeado de drawPieces para refreshBoard para refletir melhor sua função
    private void refreshBoard() {
        // Limpa todas as peças antigas do tabuleiro
        for (int i = 0; i < 64; i++) {
            StackPane stackPane = (StackPane) gridPane.getChildren().get(i);
            if (stackPane.getChildren().size() > 1) {
                stackPane.getChildren().remove(1);
            }
        }

        // Desenha todas as peças na sua posição atual
        ChessPiece[][] pieces = chessMatch.getPieces();
        for (int row = 0; row < pieces.length; row++) {
            for (int col = 0; col < pieces[row].length; col++) {
                if (pieces[row][col] != null) {
                    Label pieceLabel = new Label(pieces[row][col].toString());
                    pieceLabel.setFont(new Font("Arial", 50));
                    
                    if (pieces[row][col].getColor() == Color.WHITE) {
                        pieceLabel.setTextFill(Paint.valueOf("#FFFFFF"));
                    } else {
                        pieceLabel.setTextFill(Paint.valueOf("#000000"));
                    }

                    pieceLabel.setMouseTransparent(true);

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