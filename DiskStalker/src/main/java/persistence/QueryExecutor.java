package persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

public final class QueryExecutor {

    private static final Logger LOGGER = Logger.getGlobal();

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
        PreparedStatement statement = ConnectionProvider.getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        QueryHelper.mapParams(statement, args);
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
        QueryHelper.mapParams(ps, args);
        ps.execute();
    }

    public static ResultSet read(final String sql, Object... args) throws SQLException {
        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql);
        QueryHelper.mapParams(ps, args);
        final ResultSet resultSet = ps.executeQuery();
        return resultSet;
    }

    public static void delete(final String sql, Object... args) throws SQLException {
        PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql);
        QueryHelper.mapParams(ps, args);
        ps.executeUpdate();
    }

    public static void executeUpdate(final List<String> sql, List<List<Object>> args) throws SQLException {
        ConnectionProvider.getConnection().setAutoCommit(false);
        for (int i = 0; i < sql.size(); i++) {
            PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(sql.get(i));
            QueryHelper.mapParams(ps, args.get(i));
            ps.executeUpdate();
        }
        ConnectionProvider.getConnection().commit();
        ConnectionProvider.getConnection().setAutoCommit(true);
    }
}
