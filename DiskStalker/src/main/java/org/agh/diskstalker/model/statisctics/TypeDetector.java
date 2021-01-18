package org.agh.diskstalker.model.statisctics;

import javafx.collections.ObservableList;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TypeDetector {
    private final Tika fileTypeDetector = new Tika(); //TODO: Service with thread pool

    public String detectType(Path file, ObservableList<Type> typeStatistics) {
        try{
            var typeName = fileTypeDetector.detect(new File(String.valueOf(file)));
            addFileType(typeName, typeStatistics);
            return typeName;

        } catch (IOException e){
            var logger = Logger.getGlobal();
            logger.log(Level.WARNING, "Cannot detect file:" + file.toString());
        }
        return null;
    }

    private void addFileType(String fileType, ObservableList<Type> typeStatistics) {
        var foundType = typeStatistics.stream()
                .filter(type -> type.getType().equals(fileType))
                .findFirst();

        foundType.ifPresentOrElse(
                Type::increment,
                () -> typeStatistics.add(new Type(fileType))
        );
    }
}
