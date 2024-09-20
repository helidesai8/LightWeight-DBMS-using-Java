package src.java;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Executes SQL-like queries on the database, including transactions, table creation, data insertion, and data selection.
 */
public class QueryExecutor {
    private final String delimiter = ":|"; // Delimiter for separating values in files
    private final String dbDirectory;
    private boolean inTransaction = false;
    private List<String> transactionCommands = new ArrayList<>();

    /**
     * Constructs a java.QueryExecutor for a specific database directory.
     * @param dbDirectory The directory of the database to execute queries on
     */
    public QueryExecutor(String dbDirectory) {
        this.dbDirectory = dbDirectory;
        File dbDir = new File(dbDirectory);
        if (!dbDir.exists()) {
            dbDir.mkdirs(); // Ensure the database directory exists
        }
    }

    /**
     * Executes a given query. Supports transactions, table creation, data insertion, and data selection.
     * @param query The SQL-like query to execute
     * @throws IOException If an input or output exception occurred
     */
    public void executeQuery(String query) throws IOException {
        switch (query.trim().toUpperCase()) {
            case "BEGIN TRANSACTION":
                beginTransaction();
                break;
            case "COMMIT":
                commitTransaction();
                break;
            case "ROLLBACK":
                rollbackTransaction();
                break;
            default:
                if (inTransaction) {
                    transactionCommands.add(query);
                } else {
                    processQuery(query);
                }
                break;
        }
    }

    /**
     * Begins a transaction, accumulating commands without executing them immediately.
     */
    private void beginTransaction() {
        inTransaction = true;
        transactionCommands.clear();
    }

    /**
     * Commits a transaction, executing all accumulated commands.
     * @throws IOException If an input or output exception occurred
     */
    private void commitTransaction() throws IOException {
        for (String command : transactionCommands) {
            processQuery(command);
        }
        inTransaction = false;
        transactionCommands.clear();
    }

    /**
     * Rolls back a transaction, discarding all accumulated commands.
     */
    private void rollbackTransaction() {
        inTransaction = false;
        transactionCommands.clear();
    }

    /**
     * Processes an individual query outside of a transaction.
     * @param query The SQL-like query to process
     * @throws IOException If an input or output exception occurred
     */
    private void processQuery(String query) throws IOException {
        String[] tokens = query.split("\\s+", 4);
        if (tokens.length < 4) {
            System.out.println("Unsupported or incomplete query.");
            return;
        }

        switch (tokens[0].toUpperCase()) {
            case "CREATE":
                createTable(tokens[2], tokens[3]);
                break;
            case "INSERT":
                insertInto(tokens[2], tokens[3]);
                break;
            case "SELECT":
                selectFrom(tokens[1], tokens[3]);
                break;
            default:
                System.out.println("Unsupported query type.");
                break;
        }
    }
    /**
     * Used to handle the Create table queries..
     * @param tableName The tablename to be created.
     * @param columnData The column and its meta Data
     * @throws IOException If an input or output exception occurred
     */
    private void createTable(String tableName, String columnData) throws IOException {
        File tableFile = new File(dbDirectory, tableName + ".txt");
        File metaDataFile = new File(dbDirectory, tableName + ".metadata.txt");

        if (!tableFile.exists() && metaDataFile.createNewFile()) {
            try (PrintWriter out = new PrintWriter(new FileOutputStream(metaDataFile))) {
                String processedDefinitions = columnData
                        .replaceAll("^.*?\\(", "")
                        .replaceAll("\\);?$", "")
                        .trim();

                String[] columns = processedDefinitions.split(",");
                for (String column : columns) {
                    String cleanedColumn = column.trim().replaceAll("\\s+", " ");
                    out.println(cleanedColumn.replace(" ", delimiter));
                }
            }
            System.out.println("Table and metadata for " + tableName + " created successfully.");
            tableFile.createNewFile();
        } else {
            System.out.println("Table " + tableName + " already exists or could not create metadata.");
        }
    }
    /**
     * Used to handle the Create table queries..
     * @param tableName The tablename to be created.
     * @param columnData The column and its meta Data
     * @throws IOException If an input or output exception occurred
     */
    private void insertInto(String tableName, String values) throws IOException {
        File tableFile = new File(dbDirectory, tableName + ".txt");
        File metaDataFile = new File(dbDirectory, tableName + ".metadata.txt");

        if (!tableFile.exists() || !metaDataFile.exists()) {
            System.out.println("Table " + tableName + " does not exist.");
            return;
        }

        List<String[]> columnDefinitions = new ArrayList<>();
        try (Scanner metaScanner = new Scanner(metaDataFile)) {
            while (metaScanner.hasNextLine()) {
                String[] definitionParts = metaScanner.nextLine().split(Pattern.quote(delimiter));
                columnDefinitions.add(definitionParts);
            }
        }

        String[] vals = values.substring(values.indexOf('(') + 1, values.lastIndexOf(')')).split(",");
        if (vals.length != columnDefinitions.size()) {
            System.out.println("Invalid number of values provided for insertion.");
            return;
        }

        for (int i = 0; i < vals.length; i++) {
            String val = vals[i].trim().replaceAll("^'|'$", "");
            String[] definition = columnDefinitions.get(i);
            String type = definition[1];

            if (type.startsWith("varchar")) {
                int size = Integer.parseInt(type.substring(type.indexOf('(') + 1, type.indexOf(')')));
                if (val.length() > size) {
                    System.out.println("Value \"" + val + "\" exceeds size limit for column " + definition[0]);
                    return;
                }
            } else if ("int".equals(type) && !val.matches("\\d+")) {
                System.out.println("Type mismatch for column " + definition[0] + "; expected int, got \"" + val + "\"");
                return;
            }
        }

        try (PrintWriter out = new PrintWriter(new FileOutputStream(tableFile, true))) {
            out.println(String.join(delimiter, vals));
        }
        System.out.println("Data inserted into table " + tableName + ".");
    }

    private void selectFrom(String columns, String tableName) throws FileNotFoundException {
        File metaDataFile = new File(dbDirectory, tableName + ".metadata.txt");
        if (!metaDataFile.exists()) {
            System.out.println("Metadata for table " + tableName + " does not exist.");
            return;
        }

        List<String> columnNames = new ArrayList<>();
        try (Scanner metaScanner = new Scanner(metaDataFile)) {
            while (metaScanner.hasNextLine()) {
                String line = metaScanner.nextLine();
                String[] parts = line.split(Pattern.quote(delimiter));
                if (parts.length > 0) {
                    columnNames.add(parts[0]);
                }
            }
        }

        List<String> selectedColumns = columns.trim().equals("*") ? columnNames : Arrays.asList(columns.split(","));
        selectedColumns.retainAll(columnNames);
        if (!columns.trim().equals("*")) {
            selectedColumns = columnNames.stream().filter(selectedColumns::contains).collect(Collectors.toList());
        }

        System.out.println(String.join(" ", selectedColumns));

        File tableFile = new File(dbDirectory, tableName + ".txt");
        if (!tableFile.exists()) {
            System.out.println("Table " + tableName + " does not exist.");
            return;
        }

        try (Scanner scanner = new Scanner(tableFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                List<String> rowData = Arrays.asList(line.split(Pattern.quote(delimiter), -1));

                for (String col : selectedColumns) {
                    int index = columnNames.indexOf(col);
                    if (index != -1 && index < rowData.size()) {
                        System.out.print(rowData.get(index).trim() + " ");
                    } else {
                        System.out.print("-");
                    }
                }
                System.out.println();
            }
        }
    }
}
