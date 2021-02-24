package org.agh.diskstalker.persistence;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Service
public class QueryExecutor {
    @Getter
    private final ConnectionProvider connectionProvider;

    public QueryExecutor(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void createTables(){
        try {
            create("CREATE TABLE IF NOT EXISTS observedFolders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "path VARCHAR(1024) NOT NULL," +
                    "max_size_limit VARCHAR(128) NOT NULL DEFAULT '0'," +
                    "total_files_limit VARCHAR(128) NOT NULL DEFAULT '0'," +
                    "largest_file_limit VARCHAR(128) NOT NULL DEFAULT '0'" +
                    ");");

        } catch (SQLException e) {
            log.error("Error during creating tables: " + e.getMessage());
            throw new RuntimeException("Cannot create tables");
        }
    }

    public int createAndObtainId(final String insertSql, Object... args) throws SQLException {
        var ps = connectionProvider.get().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        mapParams(ps, args);
        ps.execute();
        try (var resultSet = ps.getGeneratedKeys()) {
            return readIdFromResultSet(resultSet);
        }
    }

    private int readIdFromResultSet(final ResultSet resultSet) throws SQLException {
        return resultSet.next() ? resultSet.getInt(1) : -1;
    }

    public void create(final String insertSql, Object... args) throws SQLException {
        var ps = connectionProvider.get().prepareStatement(insertSql);
        mapParams(ps, args);
        ps.execute();
    }

    public ResultSet read(final String sql, Object... args) throws SQLException {
        var ps = connectionProvider.get().prepareStatement(sql);
        mapParams(ps, args);
        return ps.executeQuery();
    }

    public void delete(final String sql, Object... args) throws SQLException {
        var ps = connectionProvider.get().prepareStatement(sql);
        mapParams(ps, args);
        ps.executeUpdate();
    }

    public void executeUpdate(final String sql, Object... args) throws SQLException {
        connectionProvider.get().setAutoCommit(false);

        var ps = connectionProvider.get().prepareStatement(sql);
        mapParams(ps, args);
        ps.executeUpdate();

        connectionProvider.get().commit();
        connectionProvider.get().setAutoCommit(true);
    }

    public void mapParams(PreparedStatement ps, Object... args) throws SQLException {
        var i = 1;
        for (var arg : args) {
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
