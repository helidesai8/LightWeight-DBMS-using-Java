package src.java;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Handles user authentication including registration and login.
 */
public class UserAuthentication {
    private final Map<String, String> users = new HashMap<String, String>();
    private final Scanner scanner = new Scanner(System.in);
    private static final String USERS_FILE = "users_authentication_information.txt";
    private static final String DELIMITER = ":|";

    /**
     * Initializes the java.UserAuthentication class by loading user details from file.
     * @throws IOException If an input or output exception occurred
     * @throws NoSuchAlgorithmException If the algorithm for password hashing is not available
     */
    public UserAuthentication() throws IOException, NoSuchAlgorithmException {
        loadUserAuthDetailsFromFile();
    }

    /**
     * Loads user authentication details from a file.
     * @throws IOException If an input or output exception occurred
     */
    private void loadUserAuthDetailsFromFile() throws IOException {
        File file = new File(USERS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isFirstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    String[] parts = Arrays.asList(line.split(Pattern.quote(DELIMITER), -1)).toArray(new String[0]);
                    if (parts.length == 3) {
                        users.put(parts[1], Arrays.toString(new String[]{parts[1], parts[2]}));
                    }
                }
            }
        }
    }

    /**
     * Saves user details to a file.
     * @param userID The unique identifier for the user
     * @param username The username of the user
     * @param hashedPassword The hashed password of the user
     * @throws IOException If an input or output exception occurred
     */
    private void saveUserToFile(String userID, String username, String hashedPassword) throws IOException {
        File file = new File(USERS_FILE);
        boolean addHeader = !file.exists() || file.length() == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (addHeader) {
                writer.write("UUID" + DELIMITER + "Username" + DELIMITER + "Password" + System.lineSeparator());
            }
            String userRecord = userID + DELIMITER + username + DELIMITER + hashedPassword + System.lineSeparator();
            writer.write(userRecord);
        }
    }

    /**
     * Hashes the password using MD5 algorithm.
     * @param password The password to hash
     * @return The hashed password
     * @throws NoSuchAlgorithmException If the MD5 hashing algorithm is not available
     */
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(password.getBytes());
        byte[] bytes = md5.digest();
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }

    /**
     * Verifies Captca, Here the captcha used is simple addition of 2 numbers.
     * @return A boolean flag if the captcha is correct or incorrect
     */
    public boolean verifyCaptcha() {
        Random random = new Random();
        int number1 = random.nextInt(10);
        int number2 = random.nextInt(10);
        System.out.println("Please Solve Captcha: Enter What is " + number1 + " + " + number2 + "?");
        int answer = scanner.nextInt();
        return (number1 + number2) == answer;
    }

    public void register(String username, String password) throws NoSuchAlgorithmException, IOException {
        String hashedPassword = hashPassword(password);
        String userID = UUID.randomUUID().toString();
        String uniqueId = Arrays.toString(new String[]{username, hashedPassword});
        users.put(userID, uniqueId);
        saveUserToFile(userID, username, hashedPassword);
        System.out.println("User registered successfully with User ID: " + userID);
    }

    public void login(String username, String password) throws NoSuchAlgorithmException {
        for (Map.Entry<String, String> entry : users.entrySet()) {
            String[] userDetails = new String[]{Arrays.toString(new String[]{entry.getValue()})};
            if (userDetails.length >= 1) {
                String[] parts = userDetails[0].replaceAll("^\\[\\[|\\]\\]$", "").split(",\\s*");// userDetails[0] is username, userDetails[1] is hashedPassword
                if (parts[0].equals(username) && parts[1].equals(hashPassword(password))) {
                    if (verifyCaptcha()) {
                        System.out.println("Login successful! :)");
                        return;
                    } else {
                        System.out.println("Captcha verification failed!! :(");
                        return;
                    }
            }
            }
        }
        System.out.println("Invalid credentials!! :(");
    }

    public static String authenticateUser() throws NoSuchAlgorithmException, IOException {
        UserAuthentication auth = new UserAuthentication();
        System.out.println("Please type R if you want to register a new user and L for login");
        String authType = auth.scanner.nextLine().trim().toUpperCase();
        switch (authType) {
            case "R":
                System.out.println("Register with your credentials.");
                System.out.println("Enter user ID:");
                String rID = auth.scanner.nextLine();
                System.out.println("Enter password:");
                String rPassword = auth.scanner.nextLine();
                auth.register(rID, rPassword);
                return rID;
            case "L":
                System.out.println("Login with your credentials.");
                System.out.println("Enter user ID:");
                String lID = auth.scanner.nextLine();
                System.out.println("Enter password:");
                String lPassword = auth.scanner.nextLine();
                auth.login(lID, lPassword);
                return lID;
            case "X":
                System.out.println("Exiting application. Goodbye!");
                break;
            default:
                System.out.println("Invalid option. Please type 'R' to register or 'L' to login or 'X' to EXIT");
                break;
        }
        return "";
    }
}
