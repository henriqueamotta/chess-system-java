package chess.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class StockfishAPI {

    private static final String API_V2_URL = "https://stockfish.online/api/s/v2.php";

    /**
     * Consulta a API v2 do Stockfish para obter a melhor jogada para uma dada posição FEN.
     * @param fen A string FEN da posição atual do tabuleiro.
     * @param depth A profundidade de cálculo (nível de dificuldade).
     * @return A melhor jogada no formato de notação de coordenadas longas (ex: "e2e4").
     */
    public static String getBestMove(String fen, int depth) {
        
        try {
            String encodedFen = URLEncoder.encode(fen, StandardCharsets.UTF_8);
            String requestUri = API_V2_URL + "?fen=" + encodedFen + "&depth=" + depth;

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUri))
                    .build();

            System.out.println("Sending request (v2, GET method) to API: " + requestUri);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            return parseBestMove(response.body());

        } catch (IOException | InterruptedException e) {
            System.out.println("Error communicating with Stockfish API: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parser de JSON extrai corretamente a jogada de uma resposta complexa.
     * Exemplo de resposta: {"success":true,"bestmove":"bestmove e7e6 ponder b2b3"}
     * @param jsonResponse A string de resposta da API.
     * @return A jogada extraída (ex: "e7e6").
     */
    private static String parseBestMove(String jsonResponse) {
        System.out.println("Response from API: " + jsonResponse);
        if (jsonResponse != null && jsonResponse.contains("\"bestmove\":\"")) {
            // Separa a string a partir de "bestmove":"
            String[] parts = jsonResponse.split("\"bestmove\":\"");
            if (parts.length > 1) {
                // Pega a segunda parte: "bestmove e7e6 ponder b2b3",...}"
                String moveData = parts[1];
                // Separa as palavras por espaço
                String[] moveParts = moveData.split(" ");
                // A jogada real é a segunda palavra (índice 1)
                if (moveParts.length > 1 && moveParts[1].length() == 4) {
                    return moveParts[1]; // Retorna "e7e6"
                }
            }
        }
        return null;
    }
}