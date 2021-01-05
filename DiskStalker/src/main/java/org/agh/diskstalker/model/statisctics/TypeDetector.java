package org.agh.diskstalker.model.statisctics;

import javafx.collections.ObservableList;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class TypeDetector {
    private final Tika fileTypeDetector = new Tika();

    public String detectType(Path file, ObservableList<Type> typeStatistics) {
        try{
            var typeName = fileTypeDetector.detect(new File(String.valueOf(file)));
            addFileType(typeName, typeStatistics);
            return typeName;

        } catch (IOException e){
            System.out.println("Cannot detect file" + file);
        }
        return null;
    }

    private void addFileType(String fileType, ObservableList<Type> typeStatistics) {
        var foundedType = typeStatistics.stream()
                .filter(type -> type.getType().equals(fileType))
                .findFirst();

        foundedType.ifPresentOrElse(type -> foundedType.get().increment(), () -> {
            typeStatistics.add(new Type(fileType));
        });
    }
}
