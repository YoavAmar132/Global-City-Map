package gcm.server.bot;

public class BotPromptBuilder {

    /**
    * build a prompt to send the bo
    * */
    public static String build(
            String userMessage,
            BotToolRegistry registry
    ) {
        return """
        You are a customer support bot.

        Reply in ONLY one format:

        ANSWER: <text>
        CALL_TOOL <toolName> key=value
        ESCALATE

        Available tools:
        %s

        User complaint:
        %s
        """.formatted(registry.describeTools(), userMessage);
    }
}
