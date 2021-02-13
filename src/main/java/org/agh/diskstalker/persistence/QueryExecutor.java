package org.agh.diskstalker.persistence;

import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class QueryExecutor {
    public QueryExecutor() {
        try {
            create("CREATE TABLE IF NOT EXISTS observedFolders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "path VARCHAR(1024) NOT NULL," +
                    "max_size_limit INT NOT NULL DEFAULT 0," +
                    "total_files_limit INT NOT NULL DEFAULT 0," +
                    "biggest_file_limit INT NOT NULL DEFAULT 0" +
                    ");");

        } catch (SQLException e) {
            log.error("Error during creating tables: " + e.getMessage());
            throw new RuntimeException("Cannot create tables");
        }
    }

    public int createAndObtainId(final String insertSql, Object... args) throws SQLException {
        PreparedStatement statement = ConnectionProvider.getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        mapParams(statement, args);
        statement.execute();
        try (final ResultSet resultSet = statement.getGeneratedKeys()) {
            return readIdFromResultSet(resultSet);
        }
    }

    private int readIdFromResultSet(final ResultSet resultSet) throws SQLException {
        return resultSet.next() ? resultSet.getInt(1) : -1;
    }

    public void create(final String insertSql, Object... args) throws SQLException {
        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(insertSql);
        mapParams(ps, args);
        ps.execute();
    }

    public ResultSet read(final String sql, Object... args) throws SQLException {
        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql);
        mapParams(ps, args);
        return ps.executeQuery();
    }

    public void delete(final String sql, Object... args) throws SQLException {
        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql);
        mapParams(ps, args);
        ps.executeUpdate();
    }

    public void executeUpdate(final String sql, Object... args) throws SQLException {
        ConnectionProvider.getConnection().setAutoCommit(false);

        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql);
        mapParams(ps, args);
        ps.executeUpdate();

        ConnectionProvider.getConnection().commit();
        ConnectionProvider.getConnection().setAutoCommit(true);
    }

    public void mapParams(PreparedStatement ps, Object... args) throws SQLException {
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
