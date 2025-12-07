package gcm.server.service;

import common.model.User;
import gcm.server.data.UserRepo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthService {

    private final UserRepo userRepository;
    private final List<User> users = new ArrayList<>();

    public AuthService(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    // Call this once when server starts
    public void loadUsersFromDb() throws SQLException {
        users.clear();
        users.addAll(userRepository.loadAllUsers());
        System.out.println("Loaded " + users.size() + " users from DB");
    }

    // Simple login check using the in-memory ArrayList
    public User login(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equals(username) &&
                    u.getPassword().equals(password)) { // hash later
                return u;
            }
        }
        return null;
    }

    public List<User> getUsers() {
        return users;
    }
}
