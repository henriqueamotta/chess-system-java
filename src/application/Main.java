package application;

import java.util.List;
import java.util.stream.Collectors;

import boardgame.Piece;
import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.Color;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    // "Backend" com a lógica do jogo
    private ChessMatch chessMatch = new ChessMatch();
    private GridPane gridPane = new GridPane(); // Tornando o gridPane um campo da classe
    private Label statusLabel = new Label(); // Label para mostrar o status do jogo

    // Painéis para exibir as peças capturadas
    private FlowPane whiteCapturedPane = new FlowPane(5, 5);
    private FlowPane blackCapturedPane = new FlowPane(5, 5);

    // Variáveis para guardar a origem e o destino da jogada
    private ChessPosition source;
    private ChessPosition target;

    @Override
    public void start(Stage primaryStage) {
        try {
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(10));

            int squareSize = 80;
            int boardSize = 8;

            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    Rectangle square = new Rectangle(squareSize, squareSize);

                    if ((row + col) % 2 == 0) {
                        square.setFill(Paint.valueOf("#F0D9B5"));
                    } else {
                        square.setFill(Paint.valueOf("#B58863"));
                    }
                    
                    StackPane stackPane = new StackPane();
                    stackPane.getChildren().add(square);
                    gridPane.add(stackPane, col, row);
                    
                    final int finalRow = row;
                    final int finalCol = col;
                    stackPane.setOnMouseClicked(event -> {
                        handleSquareClick(ChessPosition.fromMatrixPosition(finalCol, finalRow));
                    });
                }
            }

            // Define a cor de fundo grafite
            BackgroundFill backgroundFill = new BackgroundFill(Paint.valueOf("#333333"), CornerRadii.EMPTY, Insets.EMPTY);
            Background background = new Background(backgroundFill);

            // --- Configuração dos painéis laterais de peças capturadas ---
            VBox leftPanel = new VBox(10);
            leftPanel.setPadding(new Insets(10));
            leftPanel.setAlignment(Pos.TOP_CENTER);
            leftPanel.setBackground(background); // NOVO: Aplica o fundo
            Label whiteCapturedTitle = new Label("Captured White Pieces:");
            whiteCapturedTitle.setFont(new Font("Arial", 16));
            whiteCapturedTitle.setTextFill(Paint.valueOf("#FFFFFF")); // NOVO: Cor do texto
            blackCapturedPane.setPrefWrapLength(100);
            leftPanel.getChildren().addAll(whiteCapturedTitle, blackCapturedPane);

            VBox rightPanel = new VBox(10);
            rightPanel.setPadding(new Insets(10));
            rightPanel.setAlignment(Pos.TOP_CENTER);
            rightPanel.setBackground(background); // NOVO: Aplica o fundo
            Label blackCapturedTitle = new Label("Captured Black Pieces:");
            blackCapturedTitle.setFont(new Font("Arial", 16));
            blackCapturedTitle.setTextFill(Paint.valueOf("#FFFFFF")); // NOVO: Cor do texto
            whiteCapturedPane.setPrefWrapLength(100);
            rightPanel.getChildren().addAll(blackCapturedTitle, whiteCapturedPane);
            
            // Configura o Label de status
            statusLabel.setFont(new Font("Arial", 24));
            statusLabel.setPrefHeight(40);
            statusLabel.setAlignment(Pos.CENTER);

            // Adiciona os componentes ao BorderPane
            root.setCenter(gridPane);
            root.setBottom(statusLabel);
            root.setLeft(rightPanel);
            root.setRight(leftPanel);
            BorderPane.setAlignment(statusLabel, Pos.CENTER);

            refreshBoard();

            Scene scene = new Scene(root);
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
            try {
                boolean[][] possibleMoves = chessMatch.possibleMoves(source);
                highlightPossibleMoves(possibleMoves);
            }
            catch (ChessException e) {
                source = null;
            }
        } else {
            target = position;
            try {
                chessMatch.performChessMove(source, target);
            }
            catch (ChessException e) {
                System.out.println("Erro: " + e.getMessage());
            }

            refreshBoard();
            source = null;
            target = null;
        }
    }

    // Renomeado de drawPieces para refreshBoard para refletir melhor sua função
    private void refreshBoard() {
        // Limpa todas as peças antigas e destaques do tabuleiro
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane stackPane = (StackPane) getNodeByRowColumnIndex(row, col, gridPane);
                Rectangle square = (Rectangle) stackPane.getChildren().get(0);

                if ((row + col) % 2 == 0) {
                    square.setFill(Paint.valueOf("#F0D9B5"));
                } else {
                    square.setFill(Paint.valueOf("#B58863"));
                }
                
                if (stackPane.getChildren().size() > 1) {
                    stackPane.getChildren().remove(1);
                }
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
                    StackPane stackPane = (StackPane) getNodeByRowColumnIndex(row, col, gridPane);
                    stackPane.getChildren().add(pieceLabel);
                }
            }
        }

        // Atualiza o texto do Label de status ao final de cada atualização
        updateStatusLabel();
        // Atualiza a exibição de peças capturadas
        updateCapturedPiecesDisplay();
    }

    // Método para atualizar o texto do Label de status
    private void updateStatusLabel() {
        if (chessMatch.getCheckMate()) {
            statusLabel.setText("CHECKMATE! Winner: " + chessMatch.getCurrentPlayer());
        }
        else if (chessMatch.getStalemate()) {
            statusLabel.setText("STALEMATE!");
        }
        else {
            String statusText = "Turn: " + chessMatch.getTurn() + " - Waiting Player: " + chessMatch.getCurrentPlayer();
            if (chessMatch.getCheck()) {
                statusText += " - CHECK!";
            }
            statusLabel.setText(statusText);
        }
    }
    
    // Método para exibir as peças capturadas
    private void updateCapturedPiecesDisplay() {
        whiteCapturedPane.getChildren().clear();
        blackCapturedPane.getChildren().clear();
        
        List<Piece> captured = chessMatch.getCapturedPieces();

        // Peças BRANCAS (capturadas pelo jogador PRETO, ficam no painel 'blackCapturedPane')
        List<ChessPiece> whiteCaptured = captured.stream()
                .filter(p -> ((ChessPiece)p).getColor() == Color.WHITE)
                .map(p -> (ChessPiece)p)
                .collect(Collectors.toList());
        
        for(ChessPiece piece : whiteCaptured) {
            Label pieceLabel = new Label(piece.toString());
            pieceLabel.setFont(new Font("Arial", 30));
            pieceLabel.setTextFill(Paint.valueOf("#FFFFFF")); // Cor branca no fundo escuro
            blackCapturedPane.getChildren().add(pieceLabel);
        }
        
        // Peças PRETAS (capturadas pelo jogador BRANCO, ficam no painel 'whiteCapturedPane')
        List<ChessPiece> blackCaptured = captured.stream()
                .filter(p -> ((ChessPiece)p).getColor() == Color.BLACK)
                .map(p -> (ChessPiece)p)
                .collect(Collectors.toList());
        
        for(ChessPiece piece : blackCaptured) {
            Label pieceLabel = new Label(piece.toString());
            pieceLabel.setFont(new Font("Arial", 30));
            pieceLabel.setTextFill(Paint.valueOf("#FFFFFF")); // NOVO: Cor branca no fundo escuro
            whiteCapturedPane.getChildren().add(pieceLabel);
        }
    }

    // Método para destacar os movimentos possíveis
    private void highlightPossibleMoves(boolean[][] possibleMoves) {
        for (int row = 0; row < possibleMoves.length; row++) {
            for (int col = 0; col < possibleMoves[row].length; col++) {
                if (possibleMoves[row][col]) {
                    StackPane stackPane = (StackPane) getNodeByRowColumnIndex(row, col, gridPane);
                    Rectangle square = (Rectangle) stackPane.getChildren().get(0);
                    square.setFill(Paint.valueOf("#6495ED"));
                }
            }
        }
    }
    
    // Método auxiliar para encontrar o nó (StackPane) correto na grade
    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        Node result = null;
        ObservableList<Node> childrens = gridPane.getChildren();
        for (Node node : childrens) {
            if (GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == row &&
                GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}