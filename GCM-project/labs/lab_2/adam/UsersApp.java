package Lab2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.*;

/**
 * Represents an application to manage a list of users with their emails and passwords.
 * Provides functionality to read users from a file, validate them, sort them,
 * and write the sorted list to an output file.
 *
 * @author ADAM
 * @version 11/7/2025
 */
public class UsersApp {
    private ArrayList<User> UsersList ;

    /**
     * default constructor that initiate the Lab1.UsersApp with new empty ArrayList of users
     */
    public UsersApp() {

        UsersList = new ArrayList<>();
    }

    /**
     * given an email and password trying to create a new user and adding it to the arrayList
     * @param email of user
     * @param password of user
     */
    public void addUser(String email, String password) {
        try {
            User newUser = new User(email, password); // may throw exception
            UsersList.add(newUser); //we get here only if there are no exceptions

        } catch (IllegalArgumentException e) {  //if we catch here this means we got wrong format for email or password
            System.err.println(e.getMessage()); // should change this to print into file maybe ?
        }
    }

    /**
     * sorting arrayList lexicographically by email
     */
    public void usersSorted() {
        //sort users by email
        //UsersList.sort((u1, u2) -> u1.getName().compareTo(u2.getName()));
        UsersList.sort(Comparator.comparing(User::getName));

    }



    //no need for all of this, it's just for future if needed

    /**
     * creating new UserApp using the given ArrayList of users
     * @param usersList ArrayList of users
     */
    public UsersApp(ArrayList<User> usersList) {
        super();
        UsersList = usersList;
    }

    /**
     * returns ArrayList of users
     * @return ArrayList of users
     */
    public ArrayList<User> getUsersList() {
        return UsersList;
    }

    /**
     * sets the ArrayList of users
     * @param usersList the new ArrayList
     */
    public void setUsersList(ArrayList<User> usersList) {
        UsersList = usersList;
    }

    /**
     * converting ArrayList elements to string in order to print them
     */
    @Override
    public String toString() {
        return "Lab1.UsersApp [UsersList=" + UsersList + "]";
    }


}

