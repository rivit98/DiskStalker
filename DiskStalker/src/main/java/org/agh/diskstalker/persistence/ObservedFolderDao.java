package org.agh.diskstalker.persistence;

import org.agh.diskstalker.model.ObservedFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ObservedFolderDao {

    protected static void save(ObservedFolder observedFolder) {
        var path = observedFolder.getDirToWatch().toString();
        var maxSize = -1;

        String insertIntoDB = "INSERT INTO observedFolders (path, max_size) VALUES (?, ?);";
        Object[] args = {path, maxSize};

        try {
            QueryExecutor.createAndObtainId(insertIntoDB, args);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected static void update(ObservedFolder observedFolder) {
        var path = observedFolder.getDirToWatch().toString();
        var observedFolderTreeRoot = observedFolder.getTree().getValue();
        var maxSize = observedFolderTreeRoot.getValue().getMaximumSize();

        String updateDB = "UPDATE observedFolders SET max_size = (?) WHERE path = (?);";
        var conversionFromBToMB = 1024 * 1024;
        Object[] args = {maxSize / conversionFromBToMB, path};

        try {
            QueryExecutor.executeUpdate(updateDB, args);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected static void delete(ObservedFolder observedFolder) {
        var path = observedFolder.getDirToWatch().toString();

        String deleteObservedFolder = "DELETE FROM observedFolders WHERE path = (?);";
        Object[] args = {path};

        try {
            QueryExecutor.delete(deleteObservedFolder, args);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TODO: size to observed folder? - to return here maxSize effectively
    public static List<ObservedFolder> getAll() {
        String findObservedFolders = "SELECT * FROM observedFolders;";

        var resultList = new LinkedList<ObservedFolder>();
        try (var rs = QueryExecutor.read(findObservedFolders)) {
            while (rs.next()) {
                var path = Path.of(rs.getString("path"));
                if (Files.exists(path) && Files.isDirectory(path)) {
                    resultList.add(new ObservedFolder(path));
                } else {
                    String delete = "DELETE FROM observedFolders WHERE path = (?);";
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
