package gcm.server.bot;

import java.util.Map;

public class BotResponse {

    private String toolName;
    private Map<String, String> arguments;
    private String answer;
    private BotDecision decision;

    public BotResponse(BotDecision decision, String answer) {
        this.decision = decision;
        this.answer = answer;
    }

    public BotDecision getDecision() {
        return decision;
    }

    public String getAnswer() {
        return answer;
    }
}
