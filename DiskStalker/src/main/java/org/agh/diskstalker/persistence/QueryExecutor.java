package org.agh.diskstalker.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class QueryExecutor {

    private static final Logger logger = Logger.getGlobal(); //TODO: inject

    static {
        try {
            create("CREATE TABLE IF NOT EXISTS observedFolders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "path VARCHAR(1024) NOT NULL, " +
                    "max_size INT NOT NULL, " +
                    "limit_exceeded INT NOT NULL" +
                    ");");

        } catch (SQLException e) {
            logger.info("Error during creating tables: " + e.getMessage());
            throw new RuntimeException("Cannot create tables");
        }
    }

    private QueryExecutor() {
    }

    public static int createAndObtainId(final String insertSql, Object... args) throws SQLException {
        PreparedStatement statement = ConnectionProvider.getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        mapParams(statement, args);
        statement.execute();
        try (final ResultSet resultSet = statement.getGeneratedKeys()) {
            return readIdFromResultSet(resultSet);
        }
    }

    private static int readIdFromResultSet(final ResultSet resultSet) throws SQLException {
        return resultSet.next() ? resultSet.getInt(1) : -1;
    }

    public static void create(final String insertSql, Object... args) throws SQLException {
        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(insertSql);
        mapParams(ps, args);
        ps.execute();
    }

    public static ResultSet read(final String sql, Object... args) throws SQLException {
        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql);
        mapParams(ps, args);
        return ps.executeQuery();
    }

    public static void delete(final String sql, Object... args) throws SQLException {
        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql);
        mapParams(ps, args);
        ps.executeUpdate();
    }

    public static void executeUpdate(final String sql, Object... args) throws SQLException {
        ConnectionProvider.getConnection().setAutoCommit(false);

        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql);
        mapParams(ps, args);
        ps.executeUpdate();

        ConnectionProvider.getConnection().commit();
        ConnectionProvider.getConnection().setAutoCommit(true);
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
