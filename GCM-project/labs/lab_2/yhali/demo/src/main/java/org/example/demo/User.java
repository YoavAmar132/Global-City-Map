package org.example.demo;

public class User {
    //attributes
    private String username;
    private String password;
    private String Errormsg;

    // constructor
    public User(String username, String password) {

        if(ValidUsername(username) && ValidPassword(password)) {
            this.username = username;
            this.password = password;
        }else{
            throw new IllegalArgumentException(this.Errormsg);
        }
    }
    //getters
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    //methods:

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
