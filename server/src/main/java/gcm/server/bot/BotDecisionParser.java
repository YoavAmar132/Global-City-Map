package gcm.server.bot;

import java.util.HashMap;
import java.util.Map;

public class BotDecisionParser {

    public static BotResponse parse(String raw) {

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
}
