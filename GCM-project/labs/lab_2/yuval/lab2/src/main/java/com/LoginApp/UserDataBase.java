package main.java.com.LoginApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class UserDataBase {
    ArrayList<User> userList;

    public UserDataBase() {
        ArrayList<User> userList = new ArrayList<User>(); //List of users
        File readFile = new File("Users.txt.txt"); //Getting the file contents

        Scanner reader;
        try {
            reader = new Scanner(readFile); //Creating a file reader instance
            while (reader.hasNextLine()) {
                String line = reader.nextLine(); //Reads current line
                String lineSpacesFixed = line.replaceAll("\\s+", " "); //Removes extra spaces

                String[] tokens = lineSpacesFixed.split(" "); //Separating the username and password into 2 different parts
                String username = tokens[0]; //Extract username
                String password = tokens[1]; //Extract password

                try {
                    //Creating a new user based on the info of the current line and add it to the list
                    User usr = new User(username, password);
                    userList.add(usr);
                } catch (Exception _) {}
            }
        } catch (FileNotFoundException e) {
            System.out.println("ERROR!!");
        }
    }

    public boolean validateInput(String username, String password) {
        try{
            User user = new User(username,password);
            return userList.contains(user);
        }
        catch(Exception e){
            return false;
        }
    }

}
