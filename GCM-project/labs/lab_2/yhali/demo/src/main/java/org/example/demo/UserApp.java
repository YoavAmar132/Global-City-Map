package org.example.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

public class UserApp {
    // attributes
    private ArrayList<User> users;

    //default constructor
    public UserApp() {
        users= new ArrayList<User>();
    }

    // loading method
    public void loadUsers(String filename) throws IOException {
        // Load from resources folder
        Scanner sc = new Scanner(getClass().getResourceAsStream("/org/example/demo/" + filename));
        String username = "", password = "";

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] tokens = line.trim().split("\\s+");

            if (tokens.length != 2) {
                System.out.println("Invalid line: " + line);
            } else {
                username = tokens[0];
                password = tokens[1];
            }
            try {
                User user = new User(username, password);
                users.add(user);

            } catch (IllegalArgumentException e) {
                String error = "At: " + line + " ERROR: " + e.getMessage();
                try (PrintWriter err = new PrintWriter(new FileWriter("errors.txt", true))) {
                    err.println(error); // write to error file
                    System.err.println(error); // write to error console
                } catch (IOException er) {
                    e.printStackTrace();
                }
            }
        }
        sc.close();
    }

    //printing method
    public void printUsers() {
        //sort Arraylist
        users.sort((u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()));

        try (PrintWriter out = new PrintWriter(new FileWriter("usersOutput.txt"))) {
            for (User user : users) {
                String line = user.getUsername() + " " + user.getPassword();
                out.println(line);         // write to out file
            }
            System.out.println("Users saved to usersOutput.txt successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


   /* public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) {
            System.out.println("file did not pass");
            return;
        }
        UserApp userApp = new UserApp();
        loadUsers( userApp,args[0]);
        printUsers(userApp);


    }*/
}
