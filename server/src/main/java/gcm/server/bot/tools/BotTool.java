package gcm.server.bot.tools;
import java.util.Map;

/*
 * BotTool is an interface for the tools we wish to let the bot use
 * e.g.:
 * if we want to allow the bot to know what is the price for a subscription to a city,we'll have to create a class which implement BotTool
 * that it's entire purpose is to answer said query,assuming it gets the arguments it needs
 *
 * if the bot needs to use a tool he prints
 *
 * CALL_TOOL <tool_name> <arg1>=<value1> <arg2>=<value2>
 *
 * */
public interface BotTool {
    /*
     * return the name of the tool (it doesn't have to have the same name as the class)
     * it has to be a unique name (compare to other bot tool's names)
     * this is the name we give the bot to let it know what's the name of the tool
     * if the bot want to use this tool it will use the name in order to refer to it
     * */
    String getName();

    /*
     * return a description of what the query does
     * the description will be given to the bot alongside the name in order to let the bot know what the query does
     * it's crucial that you tell the bot the exact names of the arguments and that those are the names he must use when he call the tool
     * */
    String getDescription();

    /*
     * get a map that maps from the names of the arguments to the values of the arguments (both a given as strings)
     * return a string with the answer for the query
     *
     * if an exception is thrown the complaint is then forwarded to customer service
     * */
    String execute(Map<String, String> args) throws Exception;
}
