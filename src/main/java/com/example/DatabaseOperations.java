package com.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Future;

public interface DatabaseOperations {
    Connection getConnection() throws SQLException;
    Future<Connection> getConnectionAsync();
    void closeConnection(Connection connection);
    void shutdownExecutorService();
}
