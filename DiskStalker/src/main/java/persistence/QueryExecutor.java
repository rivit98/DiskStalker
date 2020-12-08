package persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public final class QueryExecutor {

    private static final Logger LOGGER = Logger.getGlobal();
    private static Lock lock = new ReentrantLock();

    private QueryExecutor() {
        throw new UnsupportedOperationException();
    }

    static {
        try {
            create("CREATE TABLE IF NOT EXISTS observedFolders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "path VARCHAR(1024) NOT NULL, " +
                    "max_size INT NOT NULL" +
                    ");");

        } catch (SQLException e) {
            LOGGER.info("Error during create tables: " + e.getMessage());
            throw new RuntimeException("Cannot create tables");
        }
    }

    public static int createAndObtainId(final String insertSql, Object... args) throws SQLException {
        lock.lock();
        PreparedStatement statement = ConnectionProvider.getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        mapParams(statement, args);
        statement.execute();
        try (final ResultSet resultSet = statement.getGeneratedKeys()) {
            lock.unlock();
            return readIdFromResultSet(resultSet);
        }
    }

    private static int readIdFromResultSet(final ResultSet resultSet) throws SQLException {
        return resultSet.next() ? resultSet.getInt(1) : -1;
    }

    public static void create(final String insertSql, Object... args) throws SQLException {
        lock.lock();
        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(insertSql);
        mapParams(ps, args);
        ps.execute();
        lock.unlock();
    }

    public static ResultSet read(final String sql, Object... args) throws SQLException {
        lock.lock();
        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql);
        mapParams(ps, args);
        final ResultSet resultSet = ps.executeQuery();
        lock.unlock();
        return resultSet;
    }

    public static void delete(final String sql, Object... args) throws SQLException {
        lock.lock();
        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql);
        mapParams(ps, args);
        ps.executeUpdate();
        lock.unlock();
    }

    public static void executeUpdate(final String sql, Object... args) throws SQLException {
        lock.lock();
        ConnectionProvider.getConnection().setAutoCommit(false);

        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql);
        mapParams(ps, args);
        ps.executeUpdate();

        ConnectionProvider.getConnection().commit();
        ConnectionProvider.getConnection().setAutoCommit(true);
        lock.unlock();
    }

    public static void mapParams(PreparedStatement ps, Object... args) throws SQLException {
        int i = 1;
        for (Object arg : args) {
            if (arg instanceof Integer) {
                ps.setInt(i++, (Integer) arg);
            } else if (arg instanceof Long) {
                ps.setLong(i++, (Long) arg);
            } else if (arg instanceof Double) {
                ps.setDouble(i++, (Double) arg);
            } else if (arg instanceof Float) {
                ps.setFloat(i++, (Float) arg);
            } else {
                ps.setString(i++, (String) arg);
            }
        }
    }
}
