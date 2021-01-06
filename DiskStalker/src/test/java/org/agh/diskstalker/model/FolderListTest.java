package org.agh.diskstalker.model;

import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class FolderListTest {
    FolderList folderList = new FolderList();
    Path path = Mockito.mock(Path.class);
    ObservedFolder folder;

    @BeforeEach
    public void setUp() {
        Mockito.when(path.toFile()).thenReturn(Mockito.mock(File.class));
        Mockito.when(path.getFileName()).thenReturn(Mockito.mock(Path.class));
        folder = new ObservedFolder(path);
        folder.getTreeBuilder().processNodeData(new NodeData(path));
    }

    @Test
    public void addToList() {
        //given

        //when
        folderList.get().add(folder);

        //then
        Assertions.assertFalse(folderList.get().isEmpty());
        Assertions.assertTrue(folderList.get().contains(folder));
    }

    @Test
    public void removeFromList() {
        //given
        folderList.get().add(folder);

        //when
        folderList.get().remove(folder);

        //then
        Assertions.assertTrue(folderList.get().isEmpty());
        Assertions.assertFalse(folderList.get().contains(folder));
    }

    @Test
    public void getObservedFolderFromTreePath() { //todo: more folders test
        //given
        folderList.get().add(folder);

        //when
        Optional<ObservedFolder> observedFolder = folderList.getObservedFolderFromTreePath(path);

        //then
        Assertions.assertTrue(observedFolder.isPresent());
    }

    @Test
    public void getObservedFolderFromTreeItem() {
        //given
        folderList.get().add(folder);

        //when
        Optional<ObservedFolder> observedFolder = folderList
                .getObservedFolderFromTreeItem(new TreeItem<>(new NodeData(path)));

        //then
        Assertions.assertTrue(observedFolder.isPresent());
    }
}
