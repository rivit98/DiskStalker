package org.agh.diskstalker.model;

import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.folders.FolderList;
import org.agh.diskstalker.model.folders.ObservedFolder;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;
import org.agh.diskstalker.model.tree.NodeData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FolderListTest {
    private FolderList folderList;
    private Path path;
    private ILimitableObservableFolder folder;

    @BeforeEach
    public void setUp() {
        folderList = new FolderList();
        path = mock(Path.class);
        when(path.toFile()).thenReturn(mock(File.class));
        when(path.getFileName()).thenReturn(mock(Path.class));
        folder = new ObservedFolder(path);
        folder.getNodesTree().addNode(new NodeData(path));
    }

    @Test
    public void addToList() {
        //given

        //when
        folderList.add(folder);

        //then
        Assertions.assertFalse(folderList.isEmpty());
        Assertions.assertTrue(folderList.contains(folder));
    }

    @Test
    public void removeFromList() {
        //given
        folderList.add(folder);

        //when
        folderList.remove(folder);

        //then
        Assertions.assertTrue(folderList.isEmpty());
        Assertions.assertFalse(folderList.contains(folder));
    }

    @Test
    public void getObservedFolderFromTreePath() {
        //given
        folderList.add(folder);

        //when
        var observedFolder = folderList.getObservedFolderFromTreePath(path);

        //then
        Assertions.assertTrue(observedFolder.isPresent());
    }

    @Test
    public void getObservedFolderFromTreeItem() {
        //given
        folderList.add(folder);

        //when
        var observedFolder = folderList
                .getObservedFolderFromTreeItem(new TreeItem<>(new NodeData(path)));

        //then
        Assertions.assertTrue(observedFolder.isPresent());
    }
}
