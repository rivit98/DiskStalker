package org.agh.diskstalker.persistence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

@Slf4j
@Service
public class ConnectionProvider {
    private final String jdbcAddress;
    private Optional<Connection> connection = Optional.empty();

    public ConnectionProvider(@Value("${dbFileName}") String jdbcAddress) {
        this.jdbcAddress = jdbcAddress;
    }

    public void init() {
        try {
            connection = Optional.of(DriverManager.getConnection(jdbcAddress));
            log.info("Connected to DB");
        } catch (Exception e) {
            log.error("Error during database initialization: " + e.getMessage());
        }
    }

    public Connection get() {
        return connection.orElseThrow(() -> new RuntimeException("Connection is not valid."));
    }

    public void close() throws SQLException {
        if (connection.isPresent()) {
            connection.get().close();
            log.info("DB connection closed");
            connection = Optional.empty();
        }
    }

}