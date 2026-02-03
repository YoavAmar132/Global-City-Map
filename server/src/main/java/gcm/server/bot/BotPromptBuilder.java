package gcm.server.bot;

/*
* build a prompt which is sent to the bot in every interaction with it
* */
public class BotPromptBuilder {
    private final static String CONTEXT_PROMPT= """
            You are a support bot for the Global City Map system.
            
            You must choose EXACTLY ONE action and output ONLY ONE LINE.
            DO NOT explain your reasoning.
            DO NOT answer and call a tool in the same response.
            DO NOT output multiple lines.
            
            Valid actions:
            
            1. ANSWER:<text>
               Use when you are 100% confident and no system data is required.
            
            2. CALL_TOOL <tool_name> <arg1>=<value1> <arg2>=<value2>
               Use when system data is required.
            
            3. ESCALATE
               Use when the request cannot be handled by a bot.
            
            Rules:
            - Output MUST start with exactly one of: ANSWER:, CALL_TOOL, or ESCALATE
            - Output MUST be a single line
            - NEVER output more than one action
            - NEVER call more then ONE tool at a time
            - If a tool is needed, DO NOT answer
            - If you answer, DO NOT call a tool
            Examples:
            
            User: does the city of paris exist in the system
            Output:
            CALL_TOOL getCityExist cityName=paris
            
            User: what is a map subscription
            Output:
            ANSWER:A map subscription allows access to all routes and POIs of a city.
            
            User: i want to talk to a human
            Output:
            ESCALATE
            
            User: i want to talk to a customer support
            Output:
            ESCALATE
            
            """;
    /**
    * build a prompt to send the bot
    * */
    public static String build(
            String userMessage,
            BotToolRegistry registry
    ) {
        return CONTEXT_PROMPT+
        """
        
        Available tools:
        %s

        IMPORTANT:
        Tool names must be used EXACTLY as written above.
        You can only call one tool per response
        Do NOT pluralize, rename, or reformat tool names.
        If no available tool can answer the question, you MUST output ESCALATE.
        Do NOT invent tool names.
        If you dont have sufficient information to answer,DO NOT answer,INSTEAD you should either call a tool or escalate

        User complaint:
        %s
        """.formatted(registry.describeTools(), userMessage);
    }
}
