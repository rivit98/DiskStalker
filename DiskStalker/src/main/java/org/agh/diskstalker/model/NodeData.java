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
            var f = path.toFile();
            this.isDirectory = f.isDirectory();
            this.accumulatedSizeProperty = new SimpleLongProperty(isFile() ? f.length() : 0);
            this.modificationDateProperty = new SimpleObjectProperty<>(FileTime.from(f.lastModified(), TimeUnit.SECONDS));
        }
        this.filename = new SimpleStringProperty(path.getFileName().toString());
    }

    public boolean isFile() {
        return !isDirectory;
    }

    public void updateModificationTime() {
        modificationDateProperty.set(
                FileTime.from(path.toFile().lastModified() / MILLIS_IN_SECOND, TimeUnit.SECONDS)
        );
    }

    public long getAccumulatedSize() {
        return accumulatedSizeProperty.getValue();
    }

    public long getActualSize() { //TODO: eliminate this, track actual size in constructor and event processor
        return path.toFile().length();
    }

    public long updateFileSize() { // update size and return the old one
        var actualSize = getActualSize();
        accumulatedSizeProperty.set(actualSize);
        return actualSize;
    }

    public void setSize(long size) {
        var newSize = getAccumulatedSize() + size;
        accumulatedSizeProperty.set(newSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var nodeData = (NodeData) o;
        return Objects.equals(path, nodeData.path);
    }
}
