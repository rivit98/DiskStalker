package persistence;

import model.ObservedFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;


//TODO: check if folder exists after loading
//TODO: test this
//TODO: add update, save
public class ObservedFoldersSQL {
    public static List<ObservedFolder> loadFolders(){
        String findStudentListSql = "SELECT * FROM observedFolders o;";

        List<ObservedFolder> resultList = new LinkedList<>();
        try (var rs = QueryExecutor.read(findStudentListSql)){
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

