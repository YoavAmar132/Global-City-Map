package gcm.server.service;

import common.messages.GcmRequest;
import common.messages.GcmResponse;
import common.messages.RegisterPayload;
import common.model.User;
import gcm.server.data.UserRepo;
import gcm.server.model.UserEntity;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * removed user list ofc and loadUsersFromDb + getUsers
  */
public class AuthService {

    private static final int MAX_ATTEMPTS = 5;  // same as in lab3
    private static final int LOCK_MINUTES = 1;


    private final UserRepo userRepo;
    private String Errormsg;

    //private final List<User> users = new ArrayList<>();

    public String getErrormsg() {
        return Errormsg;
    }

    public AuthService(UserRepo userRepository) {
        this.userRepo = userRepository;
    }


    // LOGIN
    public User login(String username, String password) throws SQLException {

        UserEntity entity = userRepo.findByUsername(username);

        if (entity == null) {
            Errormsg = "User not found";
            return null;
        }

        // unlock if expired
        userRepo.unlockIfExpired(entity.getId());
        entity = userRepo.findByUsername(username);

        // still locked
        if (entity.isLocked()) {
            Timestamp until = entity.getLockedUntil();
            Errormsg = (until != null)
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
                Errormsg = "Account is locked until " + entity.getLockedUntil();
            } else {
                int attemptsLeft = MAX_ATTEMPTS - entity.getFailedAttempts();
                Errormsg = "Wrong password. Attempts left: " + attemptsLeft;
            }

            return null;
        }

        // success
        userRepo.recordLoginSuccess(entity.getId());
        return new User(entity.getId(), entity.getUsername(), entity.getRole());
    }



    //  REGISTER
    public User register(RegisterPayload payload) throws SQLException {

        if (userRepo.existsByUsername(payload.getUsername())) {
            Errormsg = "Username is already in use";
            return null;
        }

        if (!validUsername(payload.getUsername()) || !validPassword(payload.getPassword())) {
            return null;
        }

        // TEMP: store plaintext, later hash
        boolean inserted = userRepo.insertUser(payload, "Customer");

        if (!inserted) {
            Errormsg = "Failed to create user";
            return null;
        }

        UserEntity entity = userRepo.findByUsername(payload.getUsername());
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
            Errormsg ="Username is too long, try something shorter ";
            return false;
        }
        if (atIndex <= 0  || lastDot == -1 || lastDot < atIndex) {
            Errormsg = "Please enter a valid Email as username ";
            return false;
        }

        // dividing to tokens
        String part1 = Username.substring(0, atIndex);
        String part2 = Username.substring(atIndex + 1, lastDot);
        String part3 = Username.substring(lastDot + 1);
        //first part check
        len1 = part1.length();
        if (len1 < 1) {
            Errormsg ="Please enter a valid Email as username ";
            return false;
        }
        //second part
        boolean valid = part2.matches("[A-Za-z0-9.-]+");
        if (!valid) {
            Errormsg = "Please enter a valid Email as username ";
            return false;
        }
        //third part check
        len3 = part3.length();
        if (len3 < 2) {
            Errormsg = "Please enter a valid Email as username ";
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
            Errormsg = "Password must contain at least one uppercase letter";
            return false;
        }
        if (!hasLower) {
            Errormsg = "Password must contain at least one lowercase letter";
            return false;
        }
        if (!hasDigit) {
            Errormsg = "Password must contain at least one digit";
            return false;
        }
        if (!hasSign) {
            Errormsg = "Password must contain at least one special character";
            return false;
        }
        if (passwordLen < 8) {
            Errormsg = "Your password is too short, add more characters";
            return false;
        }
        if (passwordLen > 12) {
            Errormsg = "Your password is too long, try a shorter one";
            return false;
        }
        return true;
    }
    public GcmResponse getUsers() throws SQLException {
        ArrayList<RegisterPayload> users= userRepo.getAllUsers();
        if(users.isEmpty())
        {
            return GcmResponse.error("faild to load all users");
        }
        return GcmResponse.ok(users);
    }

}
