package org.agh.diskstalker.model.statisctics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.statisctics.Type;
import org.agh.diskstalker.model.tree.TreeBuilder;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class Statistics {
    private final TreeBuilder treeBuilder;
    private final Tika fileTypeDetector = new Tika();
    private ObservableList<Type> types;
    private ObservableList<NodeData> sizes;
    private ObservableList<NodeData> modificationDates;

    public Statistics(TreeBuilder treeBuilder) {
        this.treeBuilder = treeBuilder;
    }

    public void createTypeStatistics() {
        types = FXCollections.observableArrayList();
        treeBuilder.getPathToTreeMap().forEach((path, node) -> {
            if(node.getValue().isFile()) {
                detectType(path);
            }
        });
    }

    public void createSizeStatistics() {
        sizes = FXCollections.observableArrayList();
        treeBuilder.getPathToTreeMap().forEach((path, node) -> {
            var nodeData = node.getValue();
            if(nodeData.isFile()) {
                sizes.add(nodeData);
            }
        });
    }

    public void createModificationDateStatistics() {
        modificationDates = FXCollections.observableArrayList();
        treeBuilder.getPathToTreeMap().forEach((path, node) -> {
            var nodeData = node.getValue();
            if(nodeData.isFile()) {
                try{
                    BasicFileAttributes attributes =
                            Files.readAttributes(nodeData.getPath(), BasicFileAttributes.class);
                    var date = attributes.lastModifiedTime().toString()
                            .replace("T", " ")
                            .replace("Z", " ");
                    nodeData.setModificationDateProperty(date);
                    modificationDates.add(nodeData);
                } catch(IOException e) {
                    System.out.println("Cannot load last modified date in file " + nodeData.getPath());
                }
            }
        });
    }

    private void detectType(Path file) {
        try{
            var typeName = fileTypeDetector.detect(new File(String.valueOf(file)));
            addFileType(typeName);

        } catch (IOException e){
            System.out.println("Cannot detect file" + file);
        }
    }

    private void addFileType(String fileType) {
        var foundedType = types.stream()
                .filter(type -> type.getType().equals(fileType))
                .findFirst();

        foundedType.ifPresentOrElse(type -> foundedType.get().increment(), () -> createNewType(fileType));

    }

    private void createNewType(String dir) {
      types.add(new Type(dir));
    }

    public ObservableList<Type> getTypes() {
        return types;
    }

    public ObservableList<NodeData> getSizes() {
        return sizes;
    }

    public ObservableList<NodeData> getModificationDates() {
        return modificationDates;
    }
}
