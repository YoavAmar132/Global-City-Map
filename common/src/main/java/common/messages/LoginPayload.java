package common.messages;

import java.io.Serializable;

public class LoginPayload implements Serializable {

    private final String username;
    private final String password;

    public LoginPayload(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
