package src.java;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Manages databases for each user, including creation, deletion, and loading from directories.
 */
public class DatabaseManagement {
    private static final String BASE_DIR = "users_databases";
    private final Scanner scanner = new Scanner(System.in);
    private final Map<String, String> userDatabases = new HashMap<>();

    /**
     * Initializes the java.DatabaseManagement class by loading existing databases from directories.
     *
     * @throws IOException If an input or output exception occurred
     */
    public DatabaseManagement() throws IOException {
        loadDatabasesFromDirectories();
    }

    /**
     * Loads existing databases from directories into the userDatabases map.
     */
    private void loadDatabasesFromDirectories() {
        File baseDir = new File(BASE_DIR);
        if (baseDir.exists()) {
            File[] userDirs = baseDir.listFiles();
            if (userDirs != null) {
                for (File userDir : userDirs) {
                    if (userDir.isDirectory()) {
                        File[] dbs = userDir.listFiles();
                        if (dbs != null && dbs.length > 0) {
                            userDatabases.put(userDir.getName(), dbs[0].getName());
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles the process of database creation for a user, including checking for existing databases and optionally deleting them.
     *
     * @param userID The unique identifier for the user
     * @return The path to the database, whether newly created or existing
     * @throws IOException If an input or output exception occurred
     */
    public String handleDatabaseCreation(String userID) throws IOException {
        String dbPath;
        if (userDatabases.containsKey(userID)) {
            String existingDbName = userDatabases.get(userID);
            System.out.println("A database already exists for user ID: " + userID + " with name \"" + existingDbName + "\".");
            System.out.println("Do you want to still want to DELETE it and create a new one? (Y/N)");
            String response = scanner.nextLine().trim().toUpperCase();
            if ("Y".equals(response)) {
                deleteDatabaseForUser(userID);
                dbPath = askAndCreateNewDatabase(userID);
            } else {
                System.out.println("Continuing with the existing database \"" + existingDbName + "\".");
                dbPath = BASE_DIR + File.separator + userID + File.separator + existingDbName;
            }
        } else {
            dbPath = askAndCreateNewDatabase(userID);
        }
        return dbPath;
    }

    /**
     * Asks the user for a new database name and creates it.
     *
     * @param userID The unique identifier for the user
     * @return The path to the newly created database
     * @throws IOException If an input or output exception occurred
     */
    private String askAndCreateNewDatabase(String userID) throws IOException {
        System.out.println("Please enter the new database name:");
        String dbName = scanner.nextLine();
        return createDatabaseForUser(userID, dbName);
    }

    /**
     * Creates a database for the specified user.
     *
     * @param userID       The unique identifier for the user
     * @param databaseName The name of the database to create
     * @return The absolute path of the newly created database
     * @throws IOException If an input or output exception occurred
     */
    public String createDatabaseForUser(String userID, String databaseName) throws IOException {
        File userDir = new File(BASE_DIR + File.separator + userID);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
        File dbDir = new File(userDir, databaseName);
        if (!dbDir.exists()) {
            dbDir.mkdir();
            userDatabases.put(userID, dbDir.getName());
            System.out.println("Database \"" + databaseName + "\" created successfully for user ID: " + userID);
            return dbDir.getAbsolutePath();
        } else {
            System.out.println("A database with this name already exists. No new database created.");
            return dbDir.getAbsolutePath();
        }
    }

    /**
     * Deletes the database for a given user.
     *
     * @param userID The unique identifier for the user whose database should be deleted
     */
    private void deleteDatabaseForUser(String userID) {
        File userDir = new File(BASE_DIR + File.separator + userID);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) {
                file.delete();
            }
            userDir.delete();
            userDatabases.remove(userID);
            System.out.println("Existing database deleted successfully.");
        }
    }

    public static void manageDatabase(String userId) throws IOException {
        DatabaseManagement dm = new DatabaseManagement();
        String userID = String.valueOf(userId);
        String dbPath = dm.handleDatabaseCreation(userID);
        System.out.println("Database path: " + dbPath);
        QueryExecutor qb = new QueryExecutor(dbPath);
        String query;
        do {
            System.out.println("Please enter the query you want to execute or type X to exit:");
            query = dm.scanner.nextLine();
            if (query.trim().toUpperCase().equals("X")) {
                break;
            }
            qb.executeQuery(query);
        } while (true);
    }
}
