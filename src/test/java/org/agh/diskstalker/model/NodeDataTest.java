package org.agh.diskstalker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeDataTest {
    private static final long fileSize = 2137L;
    private NodeData nodeDataFile;
    private NodeData nodeDataDir;

    @BeforeEach
    public void setUp() {
        nodeDataFile = new NodeData(mockPath("fileA"), mockAttributes(false));
        nodeDataDir = new NodeData(mockPath("dirA"), mockAttributes(true));
    }

    private Path mockPath(String filename){
        var path = mock(Path.class);
        var file = mock(File.class);
        when(path.toFile()).thenReturn(file);
        when(path.getFileName()).thenReturn(Path.of(filename));
        return path;
    }

    private BasicFileAttributes mockAttributes(boolean isDirectory){
        var attributes = mock(BasicFileAttributes.class);
        when(attributes.size()).thenReturn(isDirectory ? 0 : fileSize);
        when(attributes.lastModifiedTime()).thenReturn(FileTime.fromMillis(System.currentTimeMillis()));
        when(attributes.isDirectory()).thenReturn(isDirectory);

        return attributes;
    }

    @Test
    public void isFile(){
        assertTrue(nodeDataFile.isFile());
        assertFalse(nodeDataFile.isDirectory());
    }

    @Test
    public void updateFileDataForFile(){
        var expectedSize = 4000L;

        when(nodeDataFile.getPath().toFile().length()).thenReturn(expectedSize);
        nodeDataFile.updateFileData();

        assertEquals(expectedSize, nodeDataFile.getSize());
    }

    @Test
    public void updateFileDataForDirectory(){
        var expectedSize = 4000L;

        when(nodeDataDir.getPath().toFile().length()).thenReturn(expectedSize);
        nodeDataDir.updateFileData();

        assertEquals(0, nodeDataDir.getSize());
    }

    @Test
    public void modifySize() {
        //given
        var newSize = 14L;
        var sizeBefore = nodeDataFile.getAccumulatedSize();

        //when
        nodeDataFile.modifyAccumulatedSize(newSize);

        //then
        assertEquals(sizeBefore + newSize, nodeDataFile.getAccumulatedSize());
    }

    @Test
    public void equals(){
        assertNotEquals(nodeDataFile, nodeDataDir);
    }

    @Test
    public void comparatorTest(){
        var nodeDataFile2 = new NodeData(mockPath("fileB"), mockAttributes(false));
        var nodeDataDir2 = new NodeData(mockPath("dirB"), mockAttributes(true));

        //comparing by type
        assertEquals(1, nodeDataFile.compareTo(nodeDataDir));
        assertEquals(1, nodeDataFile2.compareTo(nodeDataDir2));
        assertEquals(0, nodeDataFile.compareTo(nodeDataFile));

        //comparing by name
        assertEquals(-1, nodeDataFile.compareTo(nodeDataFile2));
        assertEquals(1, nodeDataFile2.compareTo(nodeDataFile));
        assertEquals(-1, nodeDataDir.compareTo(nodeDataDir2));
        assertEquals(1, nodeDataDir2.compareTo(nodeDataDir));


        nodeDataDir2.modifyAccumulatedSize(1);
        nodeDataFile2.modifyAccumulatedSize(1);

        //comparing by size
        assertEquals(1, nodeDataFile.compareTo(nodeDataFile2));
        assertEquals(1, nodeDataDir.compareTo(nodeDataDir2));
    }
}
