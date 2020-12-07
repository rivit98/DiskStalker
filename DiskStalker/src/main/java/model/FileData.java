package model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;

public class FileData {
    private final Path file;
    private final boolean isDirectory;
    private final SimpleLongProperty sizeProperty;
    private final SimpleLongProperty maximumSizeProperty;
    private boolean maximumSizeSet;


    public FileData(Path file) {
        this.file = file;
        var f = file.toFile();
        this.isDirectory = f.isDirectory();
        this.sizeProperty = new SimpleLongProperty(isFile() ? f.length() : 0);
        this.maximumSizeProperty = new SimpleLongProperty(sizeProperty.longValue());
        this.maximumSizeSet = false;
    }

    public Path getPath() {
        return file;
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
        return file.toFile().length();
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

    public StringProperty getMaximumSizePropertyAsStringProperty() {
        var longPropertyAsString = Long.toString(maximumSizeProperty.get());
        return new SimpleStringProperty(longPropertyAsString);
    }

    public void setMaximumSizeProperty(long maximumSizeProperty) {
        this.maximumSizeProperty.set(maximumSizeProperty);
        maximumSizeSet = true;
    }
}
