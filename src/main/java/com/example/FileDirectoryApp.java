package com.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;


public class FileDirectoryApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DatabaseOperations dbOperations = new DatabaseUtil();
        MetadataStorage metadataStorage = new FileMetadataStorage(dbOperations);

        try {
            while (true) {
                System.out.println("1. Enter a new directory path");
                System.out.println("2. Query info about data in the database");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); 
                switch (choice) {
                    case 1:
                        System.out.print("Enter directory path: ");
                        String path = scanner.nextLine();
                        metadataStorage.storeMetadata(path);
                        break;
                    case 2:
                        queryDatabase(dbOperations);
                        break;
                    case 3:
                        System.out.println("Exiting...");
                        dbOperations.shutdownExecutorService();
                        return;
                    default:
                        System.out.println("Invalid option");
                }
            }
        } finally {
            scanner.close(); 
        }
    }

    private static void queryDatabase(DatabaseOperations dbOperations) {
        try (Connection conn = dbOperations.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT directory_path) FROM file_metadata");
            if (rs.next()) {
                System.out.println("Total unique directories: " + rs.getInt(1));
            }

            rs = stmt.executeQuery("SELECT SUM(file_size) FROM file_metadata");
            if (rs.next()) {
                System.out.println("Total file size: " + rs.getLong(1) + " bytes");
            }

            rs = stmt.executeQuery("SELECT DISTINCT file_type FROM file_metadata");
            System.out.println("File types:");
            while (rs.next()) {
                System.out.println(rs.getString("file_type"));
            }

            rs = stmt.executeQuery("SELECT directory_path, COUNT(*) AS file_count FROM file_metadata GROUP BY directory_path ORDER BY file_count DESC");
            System.out.println("Directories by number of files:");
            while (rs.next()) {
                System.out.println(rs.getString("directory_path") + ": " + rs.getInt("file_count") + " files");
            }

            stmt.executeUpdate("DELETE FROM file_metadata");
            System.out.println("All entries deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
