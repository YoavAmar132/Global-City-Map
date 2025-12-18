package gcm.server.service;

import common.model.User;
import gcm.server.data.UserRepo;
import gcm.server.model.UserEntity;

import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * removed user list ofc and loadUsersFromDb + getUsers
  */
public class AuthService {

    private static final int MAX_ATTEMPTS = 5;  // same as in lab3
    private static final int LOCK_MINUTES = 1;


    private final UserRepo userRepo;
    private String errorMsg;

    //private final List<User> users = new ArrayList<>();

    public String getErrorMsg() {
        return errorMsg;
    }

    public AuthService(UserRepo userRepository) {
        this.userRepo = userRepository;
    }


    // LOGIN
    public User login(String username, String password) throws SQLException {

        UserEntity entity = userRepo.findByUsername(username);

        if (entity == null) {
            errorMsg = "User not found";
            return null;
        }

        // unlock if expired
        userRepo.unlockIfExpired(entity.getId());
        entity = userRepo.findByUsername(username);

        // still locked
        if (entity.isLocked()) {
            Timestamp until = entity.getLockedUntil();
            errorMsg = (until != null)
                    ? "Account is locked until " + until
                    : "Account is locked. Try again later.";
            return null;
        }

        // wrong password
        if (!entity.getPasswordHash().equals(password)) {

            userRepo.recordLoginFailure(
                    entity.getId(),
                    MAX_ATTEMPTS,
                    LOCK_MINUTES
            );

            // reload after failure
            entity = userRepo.findByUsername(username);

            if (entity.isLocked() && entity.getLockedUntil() != null) {
                // 🔒 JUST got locked — show timestamp
                errorMsg = "Account is locked until " + entity.getLockedUntil();
            } else {
                int attemptsLeft = MAX_ATTEMPTS - entity.getFailedAttempts();
                errorMsg = "Wrong password. Attempts left: " + attemptsLeft;
            }

            return null;
        }

        // success
        userRepo.recordLoginSuccess(entity.getId());
        return new User(entity.getId(), entity.getUsername(), entity.getRole());
    }



    //  REGISTER
    public User register(String username, String password) throws SQLException {

        if (userRepo.existsByUsername(username)) {
            errorMsg = "Username is already in use";
            return null;
        }

        if (!validUsername(username) || !validPassword(password)) {
            return null;
        }

        // TEMP: store plaintext, later hash
        boolean inserted = userRepo.insertUser(username, password, "Customer");

        if (!inserted) {
            errorMsg = "Failed to create user";
            return null;
        }

        UserEntity entity = userRepo.findByUsername(username);
        return new User(entity.getId(), entity.getUsername(), entity.getRole());
    }




    // other methods:
    //username validation      (didn't really dive deep assumed its working :D)
    public boolean validUsername(String Username) {

        int totalLen = Username.length() ,len1,len3,NumOfParts,atIndex,lastDot; //variables to split tokens

        //default checks:
        atIndex = Username.indexOf('@');
        lastDot = Username.lastIndexOf('.');

        if (totalLen < 2 || totalLen > 50) {
            errorMsg="Username is too long, try something shorter ";
            return false;
        }
        if (atIndex <= 0  || lastDot == -1 || lastDot < atIndex) {
            errorMsg = "Please enter a valid Email as username ";
            return false;
        }

        // dividing to tokens
        String part1 = Username.substring(0, atIndex);
        String part2 = Username.substring(atIndex + 1, lastDot);
        String part3 = Username.substring(lastDot + 1);
        //first part check
        len1 = part1.length();
        if (len1 < 1) {
            errorMsg="Please enter a valid Email as username ";
            return false;
        }
        //second part
        boolean valid = part2.matches("[A-Za-z0-9.-]+");
        if (!valid) {
            errorMsg= "Please enter a valid Email as username ";
            return false;
        }
        //third part check
        len3 = part3.length();
        if (len3 < 2) {
            errorMsg= "Please enter a valid Email as username ";
            return false;
        }

        return true;
    }

    // password validation  (just gave better error messages )
    public boolean validPassword(String password) {

        int passwordLen = password.length();

        boolean hasCapital = password.matches(".*[A-Z].*");
        boolean hasLower   = password.matches(".*[a-z].*");
        boolean hasDigit   = password.matches(".*[0-9].*");
        boolean hasSign    = password.matches(".*[!@#$%^&*()_./?<>].*");

        if (!hasCapital) {
            errorMsg = "Password must contain at least one uppercase letter";
            return false;
        }
        if (!hasLower) {
            errorMsg = "Password must contain at least one lowercase letter";
            return false;
        }
        if (!hasDigit) {
            errorMsg = "Password must contain at least one digit";
            return false;
        }
        if (!hasSign) {
            errorMsg = "Password must contain at least one special character";
            return false;
        }
        if (passwordLen < 8) {
            errorMsg = "Your password is too short, add more characters";
            return false;
        }
        if (passwordLen > 12) {
            errorMsg = "Your password is too long, try a shorter one";
            return false;
        }
        return true;
    }

}
