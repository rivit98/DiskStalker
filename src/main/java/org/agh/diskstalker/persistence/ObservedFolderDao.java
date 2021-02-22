package org.agh.diskstalker.persistence;

import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.builders.FolderLimitsBuilder;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.limits.LimitType;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class ObservedFolderDao implements IObservedFolderDao {
    private final QueryExecutor queryExecutor;

    public ObservedFolderDao(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    @Override
    public void create(IObservedFolder IObservedFolder) {
        var path = IObservedFolder.getPath().toString();
        var insertIntoDB = "INSERT INTO observedFolders (path) VALUES (?);";
        Object[] args = {path};

        try {
            queryExecutor.createAndObtainId(insertIntoDB, args);
        } catch (SQLException e) {
            log.error("Error during saving to database: " + e.getMessage());
        }
    }

    @Override
    public void update(ILimitableObservableFolder observedFolder) {
        var path = observedFolder.getPath().toString();
        var limits = observedFolder.getLimits();
        var updateDB = "UPDATE observedFolders SET max_size_limit = (?), total_files_limit = (?), biggest_file_limit = (?) WHERE path = (?);";
        Object[] args = {
                String.valueOf(limits.get(LimitType.TOTAL_SIZE)),
                String.valueOf(limits.get(LimitType.FILES_AMOUNT)),
                String.valueOf(limits.get(LimitType.BIGGEST_FILE)),
                path
        };

        try {
            queryExecutor.executeUpdate(updateDB, args);
        } catch (SQLException e) {
            log.error("Error during updating in database: " + e.getMessage());
        }
    }

    @Override
    public void delete(IObservedFolder IObservedFolder) {
        deletePath(IObservedFolder.getPath());
    }

    private void deletePath(Path path){
        var deleteObservedFolder = "DELETE FROM observedFolders WHERE path = (?);";
        Object[] args = {path.toString()};

        try {
            queryExecutor.delete(deleteObservedFolder, args);
        } catch (SQLException e) {
            log.error("Error during deleting from database: " + e.getMessage());
        }
    }

    @Override
    public List<IObservedFolder> getAll() {
        var findObservedFolders = "SELECT * FROM observedFolders;";

        var resultList = new LinkedList<IObservedFolder>();
        var toDelete = new LinkedList<Path>();
        try (var rs = queryExecutor.read(findObservedFolders)) {
            while (rs.next()) {
                var path = Path.of(rs.getString("path"));
                if (Files.isDirectory(path)) {
                    var folder = new ObservedFolder(path);
                    var limits = new FolderLimitsBuilder()
                            .withFolder(folder)
                            .withTotalSize(Long.parseLong(rs.getString("max_size_limit")))
                            .withFileAmount(Long.parseLong(rs.getString("total_files_limit")))
                            .withBiggestFileSize(Long.parseLong(rs.getString("biggest_file_limit")))
                            .build();

                    folder.setLimits(limits);
                    resultList.add(folder);
                    log.info("Loaded " + folder.getPath() + " with limits: " + limits);
                } else {
                    toDelete.add(path);
                }
            }
        } catch (SQLException e) {
            log.error("Error during executing database query: " + e.getMessage());
        }
        toDelete.forEach(this::deletePath);

        return resultList;
    }
}
