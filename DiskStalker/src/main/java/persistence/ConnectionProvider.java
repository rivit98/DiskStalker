package persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

//TODO: singleton logger? injecting
public final class ConnectionProvider {
    private static final String JDBC_DRIVER = "org.sqlite.JDBC";
    private static final Logger logger = Logger.getGlobal();

    private static Optional<Connection> connection = Optional.empty();

    public static void init(final String jdbcAddress) {
        try {
            close();
            Class.forName(JDBC_DRIVER);
            connection = Optional.of(DriverManager.getConnection(jdbcAddress));
        } catch (Exception e) {
            logger.info("Error during initialization: " + e.getMessage());
        }
    }

    private ConnectionProvider() {
    }

    public static Connection getConnection() {
        return connection.orElseThrow(() -> new RuntimeException("Connection is not valid."));
    }

    public static void close() throws SQLException {
        if (connection.isPresent()) {
            logger.info("Closing connection");
            connection.get().close();
            connection = Optional.empty();
        }
    }

}