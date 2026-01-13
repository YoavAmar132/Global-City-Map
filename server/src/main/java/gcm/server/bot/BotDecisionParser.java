package gcm.server.bot;

import java.util.HashMap;
import java.util.Map;

public class BotDecisionParser {

    // IMPORTANT: parse() expects RAW Ollama JSON, not plain text
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

    private static String extractResponseText(String rawJson) {

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

/*
package gcm.server.bot;

import java.util.HashMap;
import java.util.Map;

public class BotDecisionParser {

    // IMPORTANT: parse() expects RAW Ollama JSON, not plain text
    public static BotResponse parse(String raw) {
        raw = extractResponseText(raw);
        raw = raw.trim();
        System.out.println("-botDecisionParser: bot response: " + raw);

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


    private static String extractResponseText(String rawJson) {

        // Find the "response":" ... " field
        int responseIndex = rawJson.indexOf("\"response\":");
        if (responseIndex == -1) {
            throw new RuntimeException("Ollama response does not contain 'response' field");
        }

        // Find first quote after "response":
        int startQuote = rawJson.indexOf("\"", responseIndex + 11);
        int endQuote = rawJson.indexOf("\"", startQuote + 1);

        if (startQuote == -1 || endQuote == -1) {
            throw new RuntimeException("Failed to parse Ollama response text");
        }

        String response = rawJson.substring(startQuote + 1, endQuote);

        // Ollama escapes newlines as \n → convert to real newlines
        response = response.replace("\\n", "\n");
        response = response.replace("\\\"", "\"");

        return response.trim();
    }
}
*/