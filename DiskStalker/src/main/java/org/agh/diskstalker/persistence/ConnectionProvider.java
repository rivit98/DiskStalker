package org.agh.diskstalker.persistence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

//TODO: service spring, inject this to class
@Slf4j
@Service
public final class ConnectionProvider {
    private static final String JDBC_DRIVER = "org.sqlite.JDBC";
    private static Optional<Connection> connection = Optional.empty();

    private ConnectionProvider() {
    }

    public static void init(final String jdbcAddress) {
        try {
            close();
            Class.forName(JDBC_DRIVER);
            connection = Optional.of(DriverManager.getConnection(jdbcAddress));
            log.info("Connected to DB");
        } catch (Exception e) {
            log.error("Error during database initialization: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        return connection.orElseThrow(() -> new RuntimeException("Connection is not valid."));
    }

    public static void close() throws SQLException {
        if (connection.isPresent()) {
            log.info("Closing connection");
            connection.get().close();
            connection = Optional.empty();
        }
    }

}