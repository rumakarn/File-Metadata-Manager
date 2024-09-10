package com.example;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class FileMetadataStorage implements MetadataStorage {

    private final DatabaseOperations dbOperations;

    public FileMetadataStorage(DatabaseOperations dbOperations) {
        this.dbOperations = dbOperations;
    }

    @Override
    public void storeMetadata(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files != null) {
            try (Connection conn = dbOperations.getConnection()) {
                String sql = "INSERT INTO file_metadata (filename, last_modified, file_type, file_size, directory_path) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (File file : files) {
                        if (file.isFile()) {
                            stmt.setString(1, file.getName());
                            stmt.setTimestamp(2, new Timestamp(file.lastModified()));
                            stmt.setString(3, getFileExtension(file));
                            stmt.setLong(4, file.length());
                            stmt.setString(5, directoryPath);
                            stmt.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        return lastIndexOf == -1 ? "" : name.substring(lastIndexOf);
    }
}
