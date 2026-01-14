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
                System.out.println("-bot agent: trying to claim next complaint");
                Optional<Complaint> opt = repo.getNextInProgressComplaint();
                if (opt.isEmpty()) continue;
                System.out.println("-bot agent: claimed next complaint successfully");
                Complaint complaint = opt.get();
                handleComplaint(complaint);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleComplaint(Complaint complaint) throws Exception {
        System.out.println("-bot agent: building prompt");
        String prompt = BotPromptBuilder.build(
                complaint.getText(),
                toolRegistry
        );
        System.out.println("-bot agent: asking ollama the following prompt:");
        System.out.println(prompt);

        String raw = ollama.ask(prompt);
        System.out.println("-bot agent: ollama response:");
        System.out.println(raw);

        System.out.println("-bot agent: analyzing decision");
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
                System.out.println("-bot agent: getting tool");

                Optional<BotTool> tool =
                        toolRegistry.get(decision.toolName);

                if (tool.isEmpty()) {
                    System.out.println("-bot agent: setting to wait for human");
                    repo.setWaitingForHuman(complaint.getId());
                    return;
                }

                System.out.println("-bot agent: using tool");
                String toolResult =
                        tool.get().execute(decision.toolArgs);

                // Feed tool result back to bot
                System.out.println("-bot agent: answering the bot back:");
                /*
                String followUpPrompt =
                        prompt + "\n\nTool result:\n" + toolResult;*/

                String followUpPrompt =
                        prompt + "Tool result:" + toolResult;
                System.out.println(followUpPrompt);

                String finalAnswer =
                        ollama.ask(followUpPrompt);
                System.out.println("-bot agent: bot responded back with:");
                System.out.println(finalAnswer);

                System.out.println("-bot agent: parsing his final response");

                BotResponse finalDecision =
                        BotDecisionParser.parse(finalAnswer);

                if (finalDecision.action == BotAction.ANSWER) {
                    System.out.println("-bot agent: setting final response");
                    repo.closeWithBotAnswer(
                            complaint.getId(),
                            finalDecision.text
                    );
                } else {
                    System.out.println("-bot agent: setting to wait for human after unsuccessful follow up response");
                    repo.setWaitingForHuman(complaint.getId());
                }
            }
        }
    }
}
