package gcm.server.bot;

import java.util.Map;


/*
* A class that saves the bot response and allow others to see what was the bot's response
* */
public class BotResponse {

    private final BotAction action;
    private final String text;
    private final String toolName;
    private final Map<String, String> toolArgs;

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

    public BotAction getAction() {
        return action;
    }

    public String getText() {
        return text;
    }

    public String getToolName() {
        return toolName;
    }

    public Map<String, String> getToolArgs() {
        return toolArgs;
    }
}
