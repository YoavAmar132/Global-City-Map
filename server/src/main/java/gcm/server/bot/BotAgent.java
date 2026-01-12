package gcm.server.bot;

import gcm.server.data.ComplaintRepo;
import gcm.server.bot.tools.BotTool;
import common.model.Complaint;

import java.util.Optional;

public class BotAgent implements Runnable {

    private final ComplaintRepo repo;
    private final OllamaClient ollama;
    private final BotToolRegistry toolRegistry;

    public BotAgent(
            ComplaintRepo repo,
            OllamaClient ollama,
            BotToolRegistry toolRegistry
    ) {
        this.repo = repo;
        this.ollama = ollama;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!repo.claimNextComplaint()) {
                    Thread.sleep(1000);
                    continue;
                }

                Optional<Complaint> opt = repo.getNextInProgressComplaint();
                if (opt.isEmpty()) continue;

                Complaint complaint = opt.get();
                handleComplaint(complaint);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleComplaint(Complaint complaint) throws Exception {

        String prompt = BotPromptBuilder.build(
                complaint.getText(),
                toolRegistry
        );

        String raw = ollama.ask(prompt);
        BotResponse decision = BotDecisionParser.parse(raw);
        //TODO: add a while loop
        switch (decision.action) {

            case ANSWER -> repo.closeWithBotAnswer(
                    complaint.getId(),
                    decision.text
            );

            case ESCALATE -> repo.setWaitingForHuman(
                    complaint.getId()
            );

            case CALL_TOOL -> {
                Optional<BotTool> tool =
                        toolRegistry.get(decision.toolName);

                if (tool.isEmpty()) {
                    repo.setWaitingForHuman(complaint.getId());
                    return;
                }

                String toolResult =
                        tool.get().execute(decision.toolArgs);

                // Feed tool result back to bot
                String followUpPrompt =
                        prompt + "\n\nTool result:\n" + toolResult;

                String finalAnswer =
                        ollama.ask(followUpPrompt);

                BotResponse finalDecision =
                        BotDecisionParser.parse(finalAnswer);

                if (finalDecision.action == BotAction.ANSWER) {
                    repo.closeWithBotAnswer(
                            complaint.getId(),
                            finalDecision.text
                    );
                } else {
                    repo.setWaitingForHuman(complaint.getId());
                }
            }
        }
    }
}
