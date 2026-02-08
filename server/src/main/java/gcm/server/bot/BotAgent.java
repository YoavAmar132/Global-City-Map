package gcm.server.bot;

import gcm.server.bot.tools.BotTool;
import common.model.Complaint;
import gcm.server.service.ComplaintService;

import java.util.Optional;

import static gcm.server.bot.BotDecisionParser.extractResponseText;


/*
 * run concurrently with the rest of the server
 * The class is a wrapper for the real class that interact with the bot (i.e. OllamaClient)
 * implement the interaction of the bot with the message it gets from the user (using BotPromptBuilder to give it prompt so it knows the
 * context of his role as a support service bot and the rules he must obey by in order to give an appropriate response back to the user)
 * implement the interaction access to the database using BotToolsRegistry which let him know what tools he may use and how
 * and also by using BotDecisionParser which get a text from the bot and parse it so we'll know what the bot wanted
 *
 * The bot also use ComplaintService to get a complaint to handle once it's free to do so and also call it if it cannot answer and in
 * which case the complaint is then escalated to customer support
 *
 * we can also adjust the number of times the bot is allowed to call a tool by changing toolUseLimit
* */

public class BotAgent implements Runnable {

    private final ComplaintService complaintService;
    private final OllamaClient ollama;
    private final BotToolRegistry toolRegistry;
    private final int toolUseLimit = 3; //the number of times the bot is allowed to call tools in response to one complaint

    public BotAgent(
            ComplaintService complaintService,
            OllamaClient ollama,
            BotToolRegistry toolRegistry
    ) {
        this.complaintService = complaintService;
        this.ollama = ollama;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public void run() {
        System.out.println("-bot agent: initialized");

        while (true) {
            try {
                /*
                * we check complaintService.hasInProgressComplaint() in order to work on a compliant in case where the server crashed
                * while the bot was working on a complaint
                * */
                if (!complaintService.claimNextComplaint() && !complaintService.hasInProgressComplaint()) {
                    Thread.sleep(1000);
                    continue;
                }
                System.out.println("-bot agent: trying to claim next complaint");
                Optional<Complaint> opt = complaintService.getNextInProgressComplaint();
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

        String trimmed_response = extractResponseText(raw).trim();

        BotResponse decision = BotDecisionParser.parse(raw);
        int usedTools = 0;
        while (usedTools < toolUseLimit){
            usedTools++;
            switch (decision.getAction()) {

                case ANSWER ->{
                    complaintService.closeWithBotAnswer(
                                     complaint.getId(),
                                     decision.getText()
                             );

                    return;
                 }

                case ESCALATE -> {
                    complaintService.setWaitingForHuman(
                        complaint.getId()
                    );
                    return;
                }

                case CALL_TOOL -> {
                    System.out.println("-bot agent: getting tool");

                    Optional<BotTool> tool =
                            toolRegistry.get(decision.getToolName());

                    if (tool.isEmpty()) {

                        System.out.println("-bot agent: setting to wait for human (tool is empty)");
                        complaintService.setWaitingForHuman(complaint.getId());
                        return;
                    }

                    System.out.println("-bot agent: using tool");
                    String toolResult = "";

                    try {
                        toolResult =
                                tool.get().execute(decision.getToolArgs());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("-bot agent: setting to wait for human (calling tool return exception)");
                        complaintService.setWaitingForHuman(complaint.getId());
                        return;
                    }

                    // Feed tool result back to bot
                    System.out.println("-bot agent: answering the bot back:");

                    String followUpPrompt =
                            prompt + "\nTool result for: "+trimmed_response+" :\n" + toolResult;
                    System.out.println(followUpPrompt);

                    String answer =
                            ollama.ask(followUpPrompt);
                    System.out.println("-bot agent: bot responded back with:");
                    System.out.println(answer);

                    System.out.println("-bot agent: parsing his final response");

                    decision =
                            BotDecisionParser.parse(answer);

                    if(usedTools==toolUseLimit) {
                        if (decision.getAction() == BotAction.ANSWER) {
                            System.out.println("-bot agent: setting final response");
                            complaintService.closeWithBotAnswer(
                                    complaint.getId(),
                                    decision.getText()
                            );
                        } else {
                            System.out.println("-bot agent: setting to wait for human after unsuccessful follow up response");
                            complaintService.setWaitingForHuman(complaint.getId());
                        }
                    }
                }
            }
        }
    }
}
