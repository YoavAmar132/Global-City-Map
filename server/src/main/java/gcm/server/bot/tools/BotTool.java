package gcm.server.bot.tools;

import java.util.Map;

public interface BotTool {
    String getName();
    String getDescription();
    String execute(Map<String, String> args) throws Exception;
}
