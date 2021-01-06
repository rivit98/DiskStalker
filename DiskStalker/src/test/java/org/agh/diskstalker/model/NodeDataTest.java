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
        Path path = Mockito.mock(Path.class);
        Mockito.when(path.toFile()).thenReturn(Mockito.mock(File.class));
        Mockito.when(path.getFileName()).thenReturn(Mockito.mock(Path.class));
        nodeData = new NodeData(path);
    }

    @Test
    public void updateFileSize() {
        //given
        long newSize = 21;
        Mockito.when(nodeData.getActualSize()).thenReturn(newSize);

        //when then
        Assertions.assertEquals(nodeData.updateFileSize(), newSize);
    }

    @Test
    public void modifySize() {
        //given
        long newSize = 14;

        //when
        nodeData.modifySize(newSize);

        //then
        Assertions.assertEquals(nodeData.getSize(), newSize);
    }
}
