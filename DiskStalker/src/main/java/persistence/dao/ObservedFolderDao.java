package persistence.dao;

import model.ObservedFolder;
import persistence.QueryExecutor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

//TODO: check if folder exists after loading
//TODO: test this
//TODO: add update, save

public class ObservedFolderDao implements GenericDao<ObservedFolder> {

    public void save(ObservedFolder observedFolder) {
        var path = observedFolder.getDirToWatch().toString();
        var maxSize = -1;

        String insertIntoDB = "INSERT INTO observedFolders (path, max_size) VALUES (?, ?);";
        Object[] args = {path, maxSize};

        System.out.println(path);

        try {
            QueryExecutor.createAndObtainId(insertIntoDB, args);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(ObservedFolder observedFolder) {
        var path = observedFolder.getDirToWatch().toString();
        var observedFolderTreeRoot = observedFolder.getTree().getValue();
        var maxSize = observedFolderTreeRoot.getValue().getMaximumSize();

        String updateDB = "UPDATE observedFolders SET max_size = (?) WHERE path = (?);";
        Object[] args = {path, maxSize};


        try {
            QueryExecutor.executeUpdate(updateDB, args);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(ObservedFolder observedFolder){
        var path = observedFolder.getDirToWatch().toString();

        String deleteObservedFolder = "DELETE FROM observedFolders WHERE path = (?);";
        Object[] args = {path};

        try {
            QueryExecutor.delete(deleteObservedFolder, args);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    //TODO: size to observed folder? - to return here maxSize effectively
    public static List<ObservedFolder> getAll() {
        String findObservedFolders = "SELECT * FROM observedFolders;";

        var resultList = new LinkedList<ObservedFolder>();
        try (var rs = QueryExecutor.read(findObservedFolders)) {
            while(rs.next()){
                var path = Path.of(rs.getString("path"));
                if (Files.exists(path) && Files.isDirectory(path)) {
                    resultList.add(new ObservedFolder(path));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
