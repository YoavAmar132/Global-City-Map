package gcm.server.bot;

import java.net.http.*;
import java.net.URI;

public class OllamaClient {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final HttpClient client = HttpClient.newHttpClient();

    public String ask(String prompt) throws Exception {
        String body = """
        {
          "model": "mistral",
          "prompt": "%s",
          "stream": false
        }
        """.formatted(prompt.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}
