package gcm.server.bot;

public class BotAgent {

    private final OllamaClient llm;
    private final BotToolService tools;

    public BotAgent(OllamaClient llm, BotToolService tools) {
        this.llm = llm;
        this.tools = tools;
    }

    public BotResponse handleComplaint(String complaintText) {

        try {
            String prompt = """
            You are a customer support bot.
            Answer ONLY if you are confident.
            If you cannot answer, respond with: ESCALATE

            Complaint:
            %s
            """.formatted(complaintText);

            String raw = llm.ask(prompt);

            if (raw.contains("ESCALATE")) {
                return new BotResponse(BotDecision.ESCALATE, null);
            }

            return new BotResponse(BotDecision.ANSWER, raw);

        } catch (Exception e) {
            return new BotResponse(BotDecision.ESCALATE, null);
        }
    }
}
