package common.messages;

public class ComplaintResponsePayload implements java.io.Serializable {
    private boolean answeredByBot;
    private String answer;

    public ComplaintResponsePayload(boolean answeredByBot, String answer) {
        this.answeredByBot = answeredByBot;
        this.answer = answer;
    }
}

