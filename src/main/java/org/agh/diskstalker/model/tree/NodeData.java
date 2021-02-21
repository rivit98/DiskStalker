package org.agh.diskstalker.model.tree;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Getter
public class NodeData implements Comparable<NodeData>{
    private static final int MILLIS_IN_SECOND = 1000;

    private final SimpleLongProperty accumulatedSizeProperty = new SimpleLongProperty();
    private final SimpleStringProperty filename = new SimpleStringProperty();
    private final SimpleObjectProperty<FileTime> modificationDateProperty = new SimpleObjectProperty<>();
    private final Path path;
    private final boolean isDirectory;
    private long size;
    @Setter private boolean removed;
    @Setter private String type;

    public NodeData(Path path) {
        this(path, null);
    }

    public NodeData(Path path, BasicFileAttributes attributes){
        this.path = path;
        if(attributes != null){
            this.isDirectory = attributes.isDirectory();
            this.accumulatedSizeProperty.set(isFile() ? attributes.size() : 0);
            this.modificationDateProperty.set(attributes.lastModifiedTime());
        }else{
            var file = path.toFile();
            this.isDirectory = file.isDirectory();
            this.accumulatedSizeProperty.set(isFile() ? file.length() : 0);
            this.modificationDateProperty.set(
                    FileTime.from(file.lastModified()  / MILLIS_IN_SECOND, TimeUnit.SECONDS)
            );
        }
        this.filename.set(path.getFileName().toString());
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

    public void modifyAccumulatedSize(long addSize) {
        accumulatedSizeProperty.set(getAccumulatedSize() + addSize);
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

    @Override
    public int compareTo(NodeData other) {
        var isFile1 = isFile() ? 1 : 0;
        var isFile2 = other.isFile() ? 1 : 0;

        if((isFile1 ^ isFile2) == 0){ // both files or both directories
            return Comparator.comparingLong(NodeData::getAccumulatedSize).reversed()
                    .thenComparing(nodeData -> nodeData.getFilename().get())
                    .compare(this, other);
        }

        return isFile() ? 1 : -1;
    }
}
