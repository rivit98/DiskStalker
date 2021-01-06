package org.agh.diskstalker.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;

public class ObservedFolderTest {
    Path path;
    ObservedFolder folder;

    @BeforeEach
    public void setUp() {
        path = Mockito.mock(Path.class);
        Mockito.when(path.toFile()).thenReturn(Mockito.mock(File.class));
        Mockito.when(path.getFileName()).thenReturn(Mockito.mock(Path.class));
        folder = new ObservedFolder(path);
    }

    @Test
    public void containsNode() {
        //given
        folder.getTreeBuilder().processNodeData(new NodeData(path));

        //when then
        Assertions.assertTrue(folder.containsNode(path));
        Assertions.assertFalse(folder.containsNode(Mockito.mock(Path.class)));
    }

    @Test
    public void setMaximumSize() {
        //given
        long maxSize = 15;

        //when
        folder.setMaximumSize(maxSize);

        //then
        Assertions.assertEquals(folder.getMaximumSize(), maxSize);
    }

    @Test
    public void isSizeLimitExceeded() {
        //given
        long maxSize = 2;
        long exceededSize = 5;
        folder.setMaximumSize(maxSize);
        NodeData nodeData = new NodeData(path);
        nodeData.modifySize(exceededSize);

        //when
        folder.getTreeBuilder().processNodeData(nodeData);

        //then
        Assertions.assertTrue(folder.isSizeLimitExceeded());
    }
}
