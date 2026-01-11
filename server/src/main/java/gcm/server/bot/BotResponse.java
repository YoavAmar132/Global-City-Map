package gcm.server.bot;

public class BotResponse {
    private BotDecision decision;
    private String answer;

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
