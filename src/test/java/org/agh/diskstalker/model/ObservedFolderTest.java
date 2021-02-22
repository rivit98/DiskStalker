package org.agh.diskstalker.model;

import org.agh.diskstalker.model.limits.LimitType;
import org.agh.diskstalker.model.tree.NodeData;
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
        folder.getNodesTree().addNode(new NodeData(path));

        //when then
        Assertions.assertTrue(folder.containsNode(path));
        Assertions.assertFalse(folder.containsNode(Mockito.mock(Path.class)));
    }

    @Test
    public void setMaximumSize() {
        //given
        var maxSize = 15L;

        //when
        folder.getLimits().setMaxTotalSize(maxSize);

        //then
        Assertions.assertEquals(folder.getLimits().get(LimitType.TOTAL_SIZE), maxSize);
    }

    @Test
    public void isSizeLimitExceeded() {
        //given
        var maxSize = 2L;
        var exceededSize = 5L;
        folder.getLimits().setMaxTotalSize(maxSize);
        NodeData nodeData = new NodeData(path);
        nodeData.modifyAccumulatedSize(exceededSize);

        //when
        folder.getNodesTree().addNode(nodeData);

        //then
        Assertions.assertTrue(folder.getLimits().isLimitExceeded(LimitType.TOTAL_SIZE));
    }
}
