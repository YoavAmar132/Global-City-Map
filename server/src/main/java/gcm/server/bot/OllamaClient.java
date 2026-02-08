package gcm.server.bot;

import java.net.http.*;
import java.net.URI;
/**
 * The class that actually communicate with ollama
 * */
public class OllamaClient {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * get a prompt,send it to the bot and return it's raw textual answer
     * */
    public String ask(String prompt) throws Exception {
        String escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        BotConfig config = BotConfig.getInstance();
        String body = """
        {
          "model": "%s",
          "prompt": "%s",
          "stream": false,
          "options": {
            "num_predict": 64
          }
        }
        """.formatted(config.getModel(), escapedPrompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }


    /*
     * warm up the bot
     * in order for the bot to be ready to get prompts from us
     * if we don't execute this at the initialization of the server it will take way longer for the bot to response
     * because it will have to wake up everytime
     * */
    public void warmUp() {
        try {
            ask("ping");
            System.out.println("[BOT] Ollama is warm");
        } catch (Exception e) {
            System.err.println("[BOT] Ollama unavailable, disabling bot");
            BotConfig botConfig = BotConfig.getInstance();
            botConfig.setEnabled(false);
        }
    }
}
