package gcm.server.bot;

import gcm.server.bot.tools.BotTool;

import java.util.*;

/*
 * class used for registering the tools the bot may use
 * allow for an easy addition of new tools
 * if we want to add a new tool we must give make a class that implement BotTool
 * if we want the bot to be able to use the tool we must register the tool inside initializeBot() in ServerBootstrap class
 */
public class BotToolRegistry {

    private final Map<String, BotTool> tools = new HashMap<>();

    /*
    * register the tool by adding it to the set of tools the bot may use
    * */
    public void register(BotTool tool) {
        tools.put(tool.getName(), tool);
    }

    /*
     * get a name and return Optional<toolBot>
     * (if a tool with this name exist it return the tool, but otherwise it's null)
     * */
    public Optional<BotTool> get(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    /*
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
