package common.messages;

import java.io.Serializable;

public class Message implements Serializable {
    private final String title;
    private String message;
    public Message(String title,String message)
    {
        this.title=title;
        this.message=message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }
}
