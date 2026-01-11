
package gcm.server.bot;

import gcm.server.data.ComplaintRepo;
import common.model.Complaint;

import java.util.Optional;

public class BotAgent implements Runnable {

    private final ComplaintRepo complaintRepo;
    private final OllamaClient ollamaClient;

    private volatile boolean running = true;

    public BotAgent() {
        this.complaintRepo = new ComplaintRepo();
        this.ollamaClient = new OllamaClient();
    }

    /**
     * Main loop – single bot, FIFO processing
     */
    @Override
    public void run() {
        while (running) {
            try {
                processNextComplaint();

                // Prevent tight loop when no work exists
                Thread.sleep(2000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * One atomic complaint processing cycle
     */
    private void processNextComplaint() throws Exception {

        // 1. Try to claim next complaint
        boolean claimed = complaintRepo.claimNextComplaint();
        if (!claimed) {
            // No complaints waiting for bot
            return;
        }

        // 2. Fetch the complaint we just claimed
        Optional<Complaint> optionalComplaint =
                complaintRepo.getNextInProgressComplaint();

        if (optionalComplaint.isEmpty()) {
            return;
        }

        Complaint complaint = optionalComplaint.get();

        // 3. Ask the LLM
        String prompt = buildPrompt(complaint);
        String llmRawResponse = ollamaClient.ask(prompt);

        // 4. Decide what to do with the answer
        if (canBotAnswer(llmRawResponse)) {
            String finalAnswer = extractAnswer(llmRawResponse);

            complaintRepo.closeWithBotAnswer(
                    complaint.getId(),
                    finalAnswer
            );
        } else {
            complaintRepo.setWaitingForHuman(complaint.getId());
        }
    }

    /**
     * Stop bot gracefully
     */
    public void stop() {
        running = false;
    }

    // -----------------------------
    // LLM helpers
    // -----------------------------

    private String buildPrompt(Complaint complaint) {
        return """
        You are a customer support assistant for a city map system.

        User complaint:
        "%s"

        If you cannot answer using system data, reply exactly with:
        CANNOT_ANSWER
        """.formatted(complaint.getText());
    }

    /**
     * Simple decision rule (can be improved later)
     */
    private boolean canBotAnswer(String rawResponse) {
        return !rawResponse.contains("CANNOT_ANSWER");
    }

    /**
     * Extract actual text from Ollama JSON
     * (very simple version for now)
     */
    private String extractAnswer(String rawResponse) {
        // Ollama returns JSON

        int idx = rawResponse.indexOf("\"response\"");
        if (idx == -1) return rawResponse;

        int start = rawResponse.indexOf(":", idx) + 1;
        return rawResponse.substring(start).replace("\"", "").trim();
    }
}
