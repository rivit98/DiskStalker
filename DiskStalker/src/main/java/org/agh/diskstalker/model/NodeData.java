package org.agh.diskstalker.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.nio.file.Path;
import java.util.Objects;

public class NodeData {
    private final Path path;
    private final boolean isDirectory;
    private final SimpleLongProperty sizeProperty;
    private final SimpleStringProperty nameProperty;
    private SimpleStringProperty modificationProperty;

    public NodeData(Path path) {
        this.path = path;
        var f = path.toFile();
        this.isDirectory = f.isDirectory();
        this.sizeProperty = new SimpleLongProperty(isFile() ? f.length() : 0);
        this.nameProperty = new SimpleStringProperty(path.getFileName().toString());
    }

    public Path getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isFile() {
        return !isDirectory;
    }

    public SimpleLongProperty getSizeProperty() {
        return sizeProperty;
    }

    public String getName() {
        return nameProperty.get();
    }

    public String getModification(){
        return modificationProperty.get();
    }

    public void setModificationDateProperty(String modificationDateProperty) {
        this.modificationProperty = new SimpleStringProperty(modificationDateProperty);
    }

    public long getSize() {
        return sizeProperty.getValue();
    }

    public long getActualSize() {
        return path.toFile().length();
    }

    public long updateFileSize() { // update size and return the old one
        var actualSize = getActualSize();
        sizeProperty.set(actualSize);
        return actualSize;
    }

    public void modifySize(long size) {
        var newSize = getSize() + size;
        sizeProperty.set(newSize);
    }

    @Override
    public String toString() {
        return path.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var nodeData = (NodeData) o;
        return Objects.equals(path, nodeData.path);
    }
}
