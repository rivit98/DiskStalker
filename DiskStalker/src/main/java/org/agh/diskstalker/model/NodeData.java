package org.agh.diskstalker.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class NodeData {
    private final Path path;
    private final boolean isDirectory;
    private final SimpleLongProperty sizeProperty;
    private final SimpleStringProperty nameProperty;
    private SimpleStringProperty modificationDateProperty;
    @Setter
    private String type;

    public NodeData(Path path) {
        this.path = path;
        var f = path.toFile();
        this.isDirectory = f.isDirectory();
        this.sizeProperty = new SimpleLongProperty(isFile() ? f.length() : 0);
        this.nameProperty = new SimpleStringProperty(path.getFileName().toString());
    }

    public boolean isFile() {
        return !isDirectory;
    }

    public String getName() {
        return nameProperty.get();
    }

    public String getModificationDate(){
        return modificationDateProperty.get();
    }

    public void setModificationDate() {
        try{
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            var date = attributes.lastModifiedTime().toString()
                    .replace("T", " ")
                    .replace("Z", " ");
            if(date.contains(".")) {
                date = date.split("\\.")[0];
            }
            modificationDateProperty = new SimpleStringProperty(date);
        } catch(IOException e) {
            var logger = Logger.getGlobal();
            logger.log(Level.WARNING, "Cannot load last modification date of file:", path);
            modificationDateProperty = new SimpleStringProperty("NO DATA");
        }
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
