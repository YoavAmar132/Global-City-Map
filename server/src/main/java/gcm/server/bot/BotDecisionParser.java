package gcm.server.bot;

import java.util.HashMap;
import java.util.Map;

/**
 * handle raw response from the bot and extract information that the other part of the system can understand and interact with easily
 */

public class BotDecisionParser {

    // IMPORTANT: parse() expects RAW Ollama JSON, not plain text

    /*
     * get the bot raw textual response (which is in a json-like format in the case of ollama).
     * generate BotResponse based on the type of answer from the bot.
     *
     * examples:
     * if the bot returned:
     * {"model":"qwen2.5:1.5b","created_at":"2026-01-1T12:00:00.6599456Z","response":"CALL_TOOL getCityExist cityName=london","done":true,....}<p>
     * it will return BotResponse with:
     * action = BotAction.CALL_TOOL
     * toolName = getCityExist
     * toolArgs = {cityName=london}
     * text = null
     *
     * if the bot returned:
     * {"model":"qwen2.5:1.5b","created_at":"2026-01-1T12:00:00.6599456Z","response":"ANSWER: The city of london exist in the system","done":true,....}<p>
     * it will return BotResponse with:<
     * action = BotAction.ANSWER
     * toolName = null
     * toolArgs = null
     * text = "The city of london does exist in the system
     * */
    public static BotResponse parse(String raw) {
        raw = extractResponseText(raw);
        raw = raw.trim();
        System.out.println("-botDecisionParser: bot response: " + raw);

        // ---- FIX: handle mixed ANSWER / CALL_TOOL responses ----
        String[] lines = raw.split("\\R");
        String firstLine = null;

        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                firstLine = line;
                break;
            }
        }

        if (firstLine == null) {
            System.out.println("-botDecisionParser: BOT ESCALATE");
            return BotResponse.escalate();
        }

        raw = firstLine;
        // -------------------------------------------------------

        if (raw.startsWith("ANSWER:")) {
            String text = raw.substring("ANSWER:".length()).trim();
            System.out.println("-botDecisionParser: BOT ANSWER: " + text);
            return BotResponse.answer(text);
        }

        if (raw.startsWith("ESCALATE")) {
            System.out.println("-botDecisionParser: BOT ESCALATE");
            return BotResponse.escalate();
        }

        if (raw.startsWith("CALL_TOOL")) {

            String[] parts = raw.split("\\s+");

            String tool = parts[1];
            System.out.println("-botDecisionParser: BOT CALLED TOOL: " + tool);

            Map<String, String> args = new HashMap<>();

            for (int i = 2; i < parts.length; i++) {
                String[] kv = parts[i].split("=");
                if (kv.length == 2) {
                    args.put(kv[0], kv[1]);
                }
            }

            return BotResponse.callTool(tool, args);
        }

        // Fallback safety
        System.out.println("-botDecisionParser: BOT ESCALATE");
        return BotResponse.escalate();
    }

    /*
     * get the bot raw textual response (which is in a json-like format in the case of ollama)
     * split the response to its different part and extract the relevant parts (which is the response itself)
     * */
    public static String extractResponseText(String rawJson) {

        int responseIndex = rawJson.indexOf("\"response\":");
        if (responseIndex == -1) {
            throw new RuntimeException("Ollama response does not contain 'response' field");
        }

        int startQuote = rawJson.indexOf("\"", responseIndex + 11);
        int endQuote = rawJson.indexOf("\"", startQuote + 1);

        if (startQuote == -1 || endQuote == -1) {
            throw new RuntimeException("Failed to parse Ollama response text");
        }

        String response = rawJson.substring(startQuote + 1, endQuote);

        response = response.replace("\\n", "\n");
        response = response.replace("\\\"", "\"");

        return response.trim();
    }
}