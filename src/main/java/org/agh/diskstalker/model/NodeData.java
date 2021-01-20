package org.agh.diskstalker.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Getter
public class NodeData {
    private static final int MILLIS_IN_SECOND = 1000;

    private final Path path;
    private final boolean isDirectory;
    private final SimpleLongProperty accumulatedSizeProperty;
    private final SimpleStringProperty filename;
    private final SimpleObjectProperty<FileTime> modificationDateProperty;
    private long size;
    @Setter
    private String type;

    public NodeData(Path path) {
        this(path, null);
    }

    public NodeData(Path path, BasicFileAttributes attributes){
        this.path = path;
        if(attributes != null){
            this.isDirectory = attributes.isDirectory();
            this.accumulatedSizeProperty = new SimpleLongProperty(isFile() ? attributes.size() : 0);
            this.modificationDateProperty = new SimpleObjectProperty<>(attributes.lastModifiedTime());
        }else{
            var file = path.toFile();
            this.isDirectory = file.isDirectory();
            this.accumulatedSizeProperty = new SimpleLongProperty(isFile() ? file.length() : 0);
            this.modificationDateProperty = new SimpleObjectProperty<>(
                    FileTime.from(file.lastModified()  / MILLIS_IN_SECOND, TimeUnit.SECONDS)
            );
        }
        this.filename = new SimpleStringProperty(path.getFileName().toString());
        this.size = accumulatedSizeProperty.get();
    }

    public boolean isFile() {
        return !isDirectory;
    }

    public void updateFileData() {
        if(isFile()){ //because folders are not displayed in additional tabs
            var file = path.toFile();
            modificationDateProperty.set(
                    FileTime.from(file.lastModified() / MILLIS_IN_SECOND, TimeUnit.SECONDS)
            );
            size = file.length();
            accumulatedSizeProperty.set(size);
        }
    }

    public void modifyAccumulatedSize(long size) {
        accumulatedSizeProperty.add(size);
    }

    public long getAccumulatedSize() {
        return accumulatedSizeProperty.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var nodeData = (NodeData) o;
        return Objects.equals(path, nodeData.path);
    }
}
