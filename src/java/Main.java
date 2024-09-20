package src.java;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] args) {
        try {
            String userId = UserAuthentication.authenticateUser();
            DatabaseManagement.manageDatabase(userId);

        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
