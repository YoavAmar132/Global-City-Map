package gcm.server.bot;

import java.util.Map;

public class BotResponse {

    public final BotAction action;
    public final String text;
    public final String toolName;
    public final Map<String, String> toolArgs;

    private BotResponse(BotAction action, String text, String toolName, Map<String, String> toolArgs) {
        this.action = action;
        this.text = text;
        this.toolName = toolName;
        this.toolArgs = toolArgs;
    }

    public static BotResponse answer(String text) {
        return new BotResponse(BotAction.ANSWER, text, null, null);
    }

    public static BotResponse escalate() {
        return new BotResponse(BotAction.ESCALATE, null, null, null);
    }

    public static BotResponse callTool(String tool, Map<String, String> args) {
        return new BotResponse(BotAction.CALL_TOOL, null, tool, args);
    }
}

/*
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
*/