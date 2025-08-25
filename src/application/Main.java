package application;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import boardgame.Piece;
import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.Color;
import chess.api.StockfishAPI;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class Main extends Application {

    // "Backend" com a lógica do jogo
    private ChessMatch chessMatch = new ChessMatch();
    private Label statusLabel = new Label();
    private FlowPane whiteCapturedPane = new FlowPane(5, 5);
    private FlowPane blackCapturedPane = new FlowPane(5, 5);
    private ChessPosition source;
    private ChessPosition target;
    private Font chessFont;
    
    // Canvas para desenhar o tabuleiro
    private Canvas canvas;
    private final int squareSize = 80;
    private final int boardSize = 8;

    private enum GameMode { HUMAN_VS_HUMAN, HUMAN_VS_AI }
    private GameMode gameMode;
    private int aiDifficulty = 10;

    @Override
    public void start(Stage primaryStage) {
        
        if (!selectGameMode()) {
            Platform.exit();
            return;
        }

        try {
            try (InputStream fontStream = Main.class.getResourceAsStream("/fonts/NotoSansSymbols2-Regular.ttf")) {
                if (fontStream == null) {
                    throw new Exception("Arquivo da fonte não encontrado! Verifique a pasta 'resources/fonts'.");
                }
                chessFont = Font.loadFont(fontStream, 50); 
            } catch (Exception e) {
                System.out.println("Erro ao carregar a fonte! Usando fonte padrão 'Arial'.");
                e.printStackTrace();
                chessFont = new Font("Arial", 50);
            }

            BorderPane root = new BorderPane();
            root.setPadding(new Insets(10));
            
            // Cria o Canvas com o tamanho do tabuleiro
            canvas = new Canvas(squareSize * boardSize, squareSize * boardSize);
            
            // Adiciona o listener de clique diretamente no Canvas
            canvas.setOnMouseClicked(event -> {
                int col = (int) (event.getX() / squareSize);
                int row = (int) (event.getY() / squareSize);
                handleSquareClick(ChessPosition.fromMatrixPosition(col, row));
            });

            BackgroundFill backgroundFill = new BackgroundFill(Paint.valueOf("#333333"), CornerRadii.EMPTY, Insets.EMPTY);
            Background background = new Background(backgroundFill);

            VBox leftPanel = new VBox(10);
            leftPanel.setPadding(new Insets(10));
            leftPanel.setAlignment(Pos.TOP_CENTER);
            leftPanel.setBackground(background);
            Label whiteCapturedTitle = new Label("Captured White Pieces:");
            whiteCapturedTitle.setFont(new Font("Arial", 16));
            whiteCapturedTitle.setTextFill(Paint.valueOf("#FFFFFF"));
            blackCapturedPane.setPrefWrapLength(100);
            leftPanel.getChildren().addAll(whiteCapturedTitle, blackCapturedPane);

            VBox rightPanel = new VBox(10);
            rightPanel.setPadding(new Insets(10));
            rightPanel.setAlignment(Pos.TOP_CENTER);
            rightPanel.setBackground(background);
            Label blackCapturedTitle = new Label("Captured Black Pieces:");
            blackCapturedTitle.setFont(new Font("Arial", 16));
            blackCapturedTitle.setTextFill(Paint.valueOf("#FFFFFF"));
            whiteCapturedPane.setPrefWrapLength(100);
            rightPanel.getChildren().addAll(blackCapturedTitle, whiteCapturedPane);
            
            statusLabel.setFont(new Font("Arial", 24));
            statusLabel.setPrefHeight(40);
            statusLabel.setAlignment(Pos.CENTER);

            root.setCenter(canvas); // O Canvas agora fica no centro
            root.setBottom(statusLabel);
            root.setLeft(rightPanel);
            root.setRight(leftPanel);
            BorderPane.setAlignment(statusLabel, Pos.CENTER);

            refreshBoard();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Chess System with JavaFX, by Henrique Motta");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSquareClick(ChessPosition position) {
        if (source == null) {
            source = position;
            try {
                boolean[][] possibleMoves = chessMatch.possibleMoves(source);
                refreshBoard(); // Redesenha o tabuleiro
                highlightPossibleMoves(possibleMoves); // Desenha os destaques sobre o tabuleiro redesenhado
            }
            catch (ChessException e) {
                source = null;
                refreshBoard(); // Limpa qualquer destaque se o clique for inválido
            }
        } else {
            target = position;
            try {
                chessMatch.performChessMove(source, target);
                
                if (gameMode == GameMode.HUMAN_VS_AI && !chessMatch.getCheckMate() && !chessMatch.getStalemate()) {
                    makeAiMove();
                }

            }
            catch (ChessException e) {
                showErrorAlert(e.getMessage());
            }

            source = null;
            target = null;
            refreshBoard();
        }
    }

    private void makeAiMove() {
        canvas.setDisable(true);
        statusLabel.setText("AI is thinking...");

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                String fen = chessMatch.getFen();
                return StockfishAPI.getBestMove(fen, aiDifficulty);
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                String bestMove = task.getValue();
                if (bestMove != null) {
                    ChessPosition aiSource = new ChessPosition(bestMove.charAt(0), Character.getNumericValue(bestMove.charAt(1)));
                    ChessPosition aiTarget = new ChessPosition(bestMove.charAt(2), Character.getNumericValue(bestMove.charAt(3)));
                    chessMatch.performChessMove(aiSource, aiTarget);
                }
                refreshBoard();
                canvas.setDisable(false);
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showErrorAlert("Could not get AI move. Check internet connection.");
                refreshBoard();
                canvas.setDisable(false);
            });
        });

        new Thread(task).start();
    }

    // MÉTODO para usar o Canvas
    private void refreshBoard() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Desenha os quadrados
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if ((row + col) % 2 == 0) {
                    gc.setFill(Paint.valueOf("#F0D9B5"));
                } else {
                    gc.setFill(Paint.valueOf("#B58863"));
                }
                gc.fillRect(col * squareSize, row * squareSize, squareSize, squareSize);
            }
        }

        // 2. Desenha as peças
        ChessPiece[][] pieces = chessMatch.getPieces();
        gc.setFont(chessFont);
        gc.setTextAlign(TextAlignment.CENTER);

        for (int row = 0; row < pieces.length; row++) {
            for (int col = 0; col < pieces[row].length; col++) {
                if (pieces[row][col] != null) {
                    if (pieces[row][col].getColor() == Color.WHITE) {
                        gc.setFill(Paint.valueOf("#FFFFFF"));
                    } else {
                        gc.setFill(Paint.valueOf("#000000"));
                    }
                    // Desenha a peça centralizada no quadrado
                    gc.fillText(getPieceChar(pieces[row][col]), col * squareSize + squareSize / 2, row * squareSize + squareSize / 2 + 15);
                }
            }
        }
        
        // 3. Atualiza os outros componentes da UI
        updateStatusLabel();
        updateCapturedPiecesDisplay();
    }

    // MÉTODO para usar o Canvas
    private void highlightPossibleMoves(boolean[][] possibleMoves) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Paint.valueOf("#6495ED80")); // Azul com 50% de transparência

        for (int row = 0; row < possibleMoves.length; row++) {
            for (int col = 0; col < possibleMoves[row].length; col++) {
                if (possibleMoves[row][col]) {
                    gc.fillRect(col * squareSize, row * squareSize, squareSize, squareSize);
                }
            }
        }
    }

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
    
    private void updateCapturedPiecesDisplay() {
        whiteCapturedPane.getChildren().clear();
        blackCapturedPane.getChildren().clear();
        
        List<Piece> captured = chessMatch.getCapturedPieces();

        List<ChessPiece> whiteCaptured = captured.stream()
                .filter(p -> ((ChessPiece)p).getColor() == Color.WHITE)
                .map(p -> (ChessPiece)p)
                .collect(Collectors.toList());
        
        for(ChessPiece piece : whiteCaptured) {
            Label pieceLabel = new Label(getPieceChar(piece));
            pieceLabel.setFont(Font.font(chessFont.getFamily(), 30));
            pieceLabel.setTextFill(Paint.valueOf("#FFFFFF"));
            blackCapturedPane.getChildren().add(pieceLabel);
        }
        
        List<ChessPiece> blackCaptured = captured.stream()
                .filter(p -> ((ChessPiece)p).getColor() == Color.BLACK)
                .map(p -> (ChessPiece)p)
                .collect(Collectors.toList());
        
        for(ChessPiece piece : blackCaptured) {
            Label pieceLabel = new Label(getPieceChar(piece));
            pieceLabel.setFont(Font.font(chessFont.getFamily(), 30));
            pieceLabel.setTextFill(Paint.valueOf("#FFFFFF"));
            whiteCapturedPane.getChildren().add(pieceLabel);
        }
    }
    
    private String getPieceChar(ChessPiece piece) {
        if (piece == null) return "";
        
        if (piece.getColor() == Color.WHITE) {
            if (piece instanceof Rook) return "\u2656";
            if (piece instanceof Knight) return "\u2658";
            if (piece instanceof Bishop) return "\u2657";
            if (piece instanceof Queen) return "\u2655";
            if (piece instanceof King) return "\u2654";
            if (piece instanceof Pawn) return "\u2659";
        }
        else {
            if (piece instanceof Rook) return "\u265C";
            if (piece instanceof Knight) return "\u265E";
            if (piece instanceof Bishop) return "\u265D";
            if (piece instanceof Queen) return "\u265B";
            if (piece instanceof King) return "\u265A";
            if (piece instanceof Pawn) return "\u265F";
        }
        return "";
    }

    private boolean selectGameMode() {
        List<String> choices = new ArrayList<>();
        choices.add("Human vs Human");
        choices.add("Human vs AI");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Human vs Human", choices);
        dialog.setTitle("Chess Game");
        dialog.setHeaderText("Choose your game mode");
        dialog.setContentText("Mode:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            gameMode = result.get().equals("Human vs Human") ? GameMode.HUMAN_VS_HUMAN : GameMode.HUMAN_VS_AI;
            if (gameMode == GameMode.HUMAN_VS_AI) {
                return selectAiDifficulty();
            }
            return true;
        }
        return false;
    }

    private boolean selectAiDifficulty() {
        List<String> difficulties = new ArrayList<>();
        difficulties.add("Easy");
        difficulties.add("Medium");
        difficulties.add("Hard");
        difficulties.add("Professional");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Hard", difficulties);
        dialog.setTitle("AI Difficulty");
        dialog.setHeaderText("Choose the AI difficulty level");
        dialog.setContentText("Level:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            String choice = result.get();
            if (choice.equals("Easy")) aiDifficulty = 2;
            if (choice.equals("Medium")) aiDifficulty = 5;
            if (choice.equals("Hard")) aiDifficulty = 10;
            if (choice.equals("Professional")) aiDifficulty = 15;
            return true;
        }
        return false;
    }
    
    private void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Invalid Move");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}