package common.model;

import java.io.Serializable;

/**
 * I agree with not sending the password thus ill remove it :D
 */
public class User implements Serializable {

    private final int id;
    private final String username;
    //private final String password; // or passwordHash, but don't send it to client if you care about security
    private final String role;

    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        //this.password = password;
        this.role = role;
    }

    public int getId() { return id; }

    public String getUsername() { return username; }

    //public String getPassword() { return password; }

    public String getRole() { return role; }
}
