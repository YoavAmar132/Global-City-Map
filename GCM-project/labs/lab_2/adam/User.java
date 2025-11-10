package Lab2;

/**
 * Represents a user with an email and password.
 * Provides getters, setters, and validation for these fields.
 *
 * @author ADAM
 * @version 11/7/2025
 */
public class User {

    private String Email;
    private String PassWord;
    /**
     * trying to create a new user given an email and password
     * @param email of the user
     * @param passWord of the user
     * @throws IllegalArgumentException if the email or password is invalid
     */
    public User(String email, String passWord) {
        //^ for start of string and $ for end, first we have to start with letter or number so [a-zA-Z0-9] (ignore this
        // doesn't have to start with letter or number :D)
        // then all the allowed chars [a-zA-Z0-9+\\-%_] not that \\ so we don't confuse with -
        //* for 0 or more occurrences.
        // then separator @ into same thing but with different chars - and . and we don't add \\ cuz its end of string :D
        // then //. as separator into at least 2 letters :D a{2,} => aa or aaa or aaaa .... at
        // $ is end of string
        //^[a-zA-Z0-9][a-zA-Z0-9+\\-%_]*@[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}$ //fail
        //"^[a-zA-Z0-9!#$%^&*()_+]+@[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}$" // fail
        String emailRegex = "^[a-zA-Z0-9.+%_-]+@[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}$";
        //(?=.*[A-Za-z]) look a head and ensure that we have at least one letter and same for number and symbol and the
        // rest is classic and the + at the end for one or more ...
        // ^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*+\\-])[a-zA-Z\\d!@#$%^&*+\\-]+$ // fail
        String passwordRegex = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*)(_+])[a-zA-Z0-9!@#$%^&*)(_+]+$";

        if(!email.matches(emailRegex))
            throw new IllegalArgumentException("Please enter a valid Email as username");

        if (email.length() > 50)
            throw new IllegalArgumentException("Username is too long, try something shorter");

        if(!passWord.matches(passwordRegex))
            throw new IllegalArgumentException("Please enter a valid password");

        if(passWord.length() < 8)
            throw new IllegalArgumentException("Your password is too short, add more characters");

        if(passWord.length() > 12)
            throw new IllegalArgumentException("Your password is too long, try a shorter one");

        Email = email;
        PassWord = passWord;
    }

    /**
     * converting user data, email and password to string in order to print it
     */
    @Override
    public String toString() {
        return Email + " " + PassWord ;
    }



    // no need for this just in case for future

    /**
     * default getter
     * @return email
     */
    public String getName() {
        return Email;
    }
    /**
     * default setter
     * @param email to set
     */
    public void setName(String email) {
        Email = email;
    }

    /**
     * default getter
     * @return password
     */
    public String getPassWord() {
        return PassWord;
    }

    /**
     * default setter
     * @param passWord to set
     */
    public void setPassWord(String passWord) {
        PassWord = passWord;
    }

}
