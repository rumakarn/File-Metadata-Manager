package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.*;

public class DatabaseUtil implements DatabaseOperations {
    private static final ExecutorService executorService = new ThreadPoolExecutor(
        2,  
        50, 
        60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>() 
    );

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        
        Properties props = new Properties();
        try (InputStream input = DatabaseUtil.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
              
            }
            props.load(input);
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL JDBC Driver registered successfully.");
        } catch (IOException ex) {
            System.err.println("Error loading properties file.");
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found. Include it in your library path.");
            e.printStackTrace();
            throw new RuntimeException("PostgreSQL JDBC Driver not found.", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Connection failed. Check the connection URL, username, and password.");
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Future<Connection> getConnectionAsync() {
        return executorService.submit(() -> {
            try {
                return getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to get a connection asynchronously", e);
            }
        });
    }

    @Override
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void shutdownExecutorService() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
