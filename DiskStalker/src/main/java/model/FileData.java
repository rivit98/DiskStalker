package model;

import javafx.beans.property.SimpleLongProperty;

import java.nio.file.Path;
import java.util.Objects;

//TODO: rename this to something more meaningful (ex. NodeData or sth like this)
public class FileData {
    private final Path path;
    private final boolean isDirectory;
    private final SimpleLongProperty sizeProperty;
    private final SimpleLongProperty maximumSizeProperty;
    private boolean maximumSizeSet;


    public FileData(Path path) {
        this.path = path;
        var f = path.toFile();
        this.isDirectory = f.isDirectory();
        this.sizeProperty = new SimpleLongProperty(isFile() ? f.length() : 0);
        this.maximumSizeProperty = new SimpleLongProperty(sizeProperty.longValue());
        this.maximumSizeSet = false;
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

    public SimpleLongProperty sizePropertyProperty() {
        return sizeProperty;
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
        if (!maximumSizeSet) {
            this.maximumSizeProperty.set(newSize);
        }
    }

    public SimpleLongProperty getMaximumSizeProperty() {
        return this.maximumSizeProperty;
    }

    public void setMaximumSizeProperty(long maximumSizeProperty) {
        this.maximumSizeProperty.set(maximumSizeProperty);
        maximumSizeSet = true;
    }

    public long getMaximumSize() {
        return maximumSizeProperty.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var fileData = (FileData) o;
        return Objects.equals(path, fileData.path);
    }
}
