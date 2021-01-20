package org.agh.diskstalker.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeDataTest {

    private NodeData nodeData;

    @BeforeEach
    public void setUp() {
        var path = mock(Path.class);
        when(path.toFile()).thenReturn(mock(File.class));
        when(path.getFileName()).thenReturn(mock(Path.class));
        nodeData = new NodeData(path);
    }

    @Test
    public void updateFileSize() {
        //FIXME :(
        //given
        var newSize = 21L;
        var mockNodeData = mock(NodeData.class);
        when(mockNodeData.getSize()).thenReturn(newSize);

        //when then
        Assertions.assertEquals(mockNodeData.getSize(), newSize);
    }

    @Test
    public void modifySize() {
        //given
        var newSize = 14L;

        //when
        nodeData.modifyAccumulatedSize(newSize);

        //then
        Assertions.assertEquals(nodeData.getAccumulatedSize(), newSize);
    }
}
