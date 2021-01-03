package org.agh.diskstalker.persistence;

import org.agh.diskstalker.model.ObservedFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ObservedFolderDao {
    public static void save(ObservedFolder observedFolder) {
        var path = observedFolder.getPath().toString();
        var insertIntoDB = "INSERT INTO observedFolders (path, max_size) VALUES (?, ?);";
        Object[] args = {path, 0};

        try {
            QueryExecutor.createAndObtainId(insertIntoDB, args);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(ObservedFolder observedFolder) {
        var path = observedFolder.getPath().toString();
        var maxSize = observedFolder.getMaximumSize();
        var updateDB = "UPDATE observedFolders SET max_size = (?) WHERE path = (?);";
        Object[] args = {maxSize, path};

        try {
            QueryExecutor.executeUpdate(updateDB, args);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void delete(ObservedFolder observedFolder) {
        var path = observedFolder.getPath().toString();
        var deleteObservedFolder = "DELETE FROM observedFolders WHERE path = (?);";
        Object[] args = {path};

        try {
            QueryExecutor.delete(deleteObservedFolder, args);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<ObservedFolder> getAll() {
        var findObservedFolders = "SELECT * FROM observedFolders;";

        var resultList = new LinkedList<ObservedFolder>();
        try (var rs = QueryExecutor.read(findObservedFolders)) {
            while (rs.next()) {
                var path = Path.of(rs.getString("path"));
                if (Files.isDirectory(path)) {
                    var maxSize = rs.getInt("max_size");
                    resultList.add(new ObservedFolder(path, maxSize));
                } else {
                    var delete = "DELETE FROM observedFolders WHERE path = (?);";
                    Object[] args = {path.toString()};

                    QueryExecutor.delete(delete, args);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
