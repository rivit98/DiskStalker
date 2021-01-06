package org.agh.diskstalker.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;

public class NodeDataTest {

    NodeData nodeData;

    @BeforeEach
    public void setUp() {
        var path = Mockito.mock(Path.class);
        Mockito.when(path.toFile()).thenReturn(Mockito.mock(File.class));
        Mockito.when(path.getFileName()).thenReturn(Mockito.mock(Path.class));
        nodeData = new NodeData(path);
    }

    @Test
    public void updateFileSize() {
        //given
        var newSize = 21L;
        Mockito.when(nodeData.getActualSize()).thenReturn(newSize);

        //when then
        Assertions.assertEquals(nodeData.updateFileSize(), newSize);
    }

    @Test
    public void modifySize() {
        //given
        var newSize = 14L;

        //when
        nodeData.modifySize(newSize);

        //then
        Assertions.assertEquals(nodeData.getSize(), newSize);
    }
}
