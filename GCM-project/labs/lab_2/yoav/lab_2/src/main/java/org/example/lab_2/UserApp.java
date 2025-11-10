package org.example.lab_2;

import javafx.application.Application;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import javafx.application.Application;

public final class UserApp {
    // attributes
    private static ArrayList<User> users;
    // private constructor
    private UserApp() {
        this.users = new ArrayList<>();
    }
    // Singleton holder ( thread-safe)
    private static class Singelton {
        private static final UserApp INSTANCE = new UserApp();
    }

    public static UserApp getInstance() {
        return Singelton.INSTANCE;
    }



    // loading method
    public static void loadUsers(String filename)throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        String username="",password="";


        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] tokens = line.trim().split("\\s+");

            if (tokens.length != 2) {
                System.out.println("Invalid line: " + line);
            } else {
                username = tokens[0];
                password = tokens[1];
            }
            try{
                User user=new User(username,password);
                users.add(user);

            }
            catch (IllegalArgumentException e) {
                String error = "At: " + line + " ERROR:" + e.getMessage();
                try (PrintWriter err = new PrintWriter(new FileWriter("errors.txt",true))) {
                    err.println(error); // write to error file
                } catch (IOException er) {
                    e.printStackTrace();
                }
            }
        }
        sc.close();

    }
    //printing method
    public static void printUsers() {
        //sort Arraylist
        users.sort((u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()));

        try (PrintWriter out = new PrintWriter(new FileWriter("out.txt"))) {
            for (User user : users) {
                String line = user.getUsername() + " " + user.getPassword();
                out.println(line);         // write to out file
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static boolean ConfirmUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
        }

        return false;
    }


    public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) {
            System.out.println("file did not pass");
            return;
        }
        UserApp userApp = new UserApp();
      loadUsers( args[0]);
      printUsers();
        Application.launch(WindowApplication.class, args);


    }
}
