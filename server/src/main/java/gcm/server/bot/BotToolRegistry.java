package gcm.server.bot;

import gcm.server.bot.tools.BotTool;

import java.util.*;

/**
 * class used for registering the tools the bot may use
 * allow for an easy addition of new tools
 */
public class BotToolRegistry {

    private final Map<String, BotTool> tools = new HashMap<>();

    public void register(BotTool tool) {
        tools.put(tool.getName(), tool);
    }

    public Optional<BotTool> get(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    /**
     * create a list of descriptions for available tools
     * main purpose:to allow for BotAgent to know what queries he can use
     * */
    public String describeTools() {
        StringBuilder sb = new StringBuilder();
        for (BotTool tool : tools.values()) {
            sb.append("- ")
                    .append(tool.getName())
                    .append(": ")
                    .append(tool.getDescription())
                    .append("\n");
        }
        return sb.toString();
    }
}
