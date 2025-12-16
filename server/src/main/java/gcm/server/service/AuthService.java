package gcm.server.service;

import common.model.Poi;
import common.model.User;
import gcm.server.data.PoiRepo;
import gcm.server.data.RouteRepo;
import gcm.server.data.UserRepo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthService {

    private final UserRepo userRepository;

    private final List<User> users = new ArrayList<>();
    public   String Errormsg;

    public String getErrormsg() {
        return Errormsg;
    }

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
    public User login(String username, String password) throws SQLException {
        User u=userRepository.findByUsername(username); //check if user exist and provide
        if(u==null){
            Errormsg="User not found";
            return null;
        }
            if (u.getUsername().equals(username))
            {
                if( u.getPassword().equals(password)) {
                    return u;
                }
                    Errormsg="Wrong password";


            }else{
                Errormsg="Wrong username or password";
            }
        return null;
    }
//register check
public User register(String username, String password) throws SQLException {
    User check=userRepository.findByUsername(username); //check if name taken
    if(check!=null){
        Errormsg="Username is already in use";
        return null;
    }
    if(ValidUsername(username)&&ValidPassword(password)) {
        User user=new User(userRepository.getMaxUserId()+1, username,password,"user");
        if(userRepository.insertUser(user)) {
            return user;
        }
        Errormsg="faild to insert user";
    }

    return null;
}
    public List<User> getUsers() {
        return users;
    }



    // other methods:
    //username validation
    public boolean ValidUsername(String Username) {

        int Totalen,len1,len3,NumOfParts,atIndex,lastDot; //variables to split tokens

        //default checks:
        atIndex = Username.indexOf('@');
        lastDot = Username.lastIndexOf('.');
        if (atIndex == -1 || lastDot == -1 || lastDot < atIndex) {
            Errormsg= "Please enter a valid Email as username ";
            return false;
        }
        Totalen = Username.length();
        if (Totalen < 2 || Totalen > 50) {
            Errormsg="Username is too long, try something shorter ";
            return false;
        }
        // dividing to tokens
        String part1 = Username.substring(0, atIndex);
        String part2 = Username.substring(atIndex + 1, lastDot);
        String part3 = Username.substring(lastDot + 1);
        //first part check
        len1 = part1.length();
        if (len1 < 1) {
            Errormsg="Please enter a valid Email as username ";
            return false;
        }
        //second part
        boolean valid = part2.matches("[A-Za-z0-9.-]+");
        if (!valid) {
            Errormsg= "Please enter a valid Email as username ";
            return false;
        }
        //third part check
        len3 = part3.length();
        if (len3 < 2) {
            Errormsg= "Please enter a valid Email as username ";
            return false;
        }

        return true;
    }

    // password validation
    public boolean ValidPassword(String password) {
        int passwordLen = password.length();
        boolean hasCapital = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSign  = password.matches(".*[!@#$%^&*()_./?<>].*");
        if((hasCapital ||hasLower)==false) {
            Errormsg="Please enter a valid password";
            return false;
        }
        if((hasSign)==false) {
            Errormsg="Please enter a valid password";
            return false;
        }
        if((hasDigit)==false) {
            Errormsg="Please enter a valid password";
            return false;
        }
        if (passwordLen < 8 ) {
            Errormsg="Your password is too short, add more characters ";
            return false;
        }
        if(passwordLen > 12)
        {
            Errormsg="Your password is too long, try a shorter one  ";
            return false;
        }
        return true;
    }
}
