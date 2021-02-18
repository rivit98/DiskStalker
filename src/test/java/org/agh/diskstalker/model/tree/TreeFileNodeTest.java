package org.agh.diskstalker.model.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TreeFileNodeTest {
    private static final long parentCurrentSize = 12L;
    private TreeFileNode node;

    @BeforeEach
    public void setUp() {
        node = new TreeFileNode(new NodeData(mockPath()));
    }

    private Path mockPath(){
        var path = mock(Path.class);
        var file = mock(File.class);
        when(path.toFile()).thenReturn(file);
        when(path.toFile().isFile()).thenReturn(true);
        when(path.toFile().length()).thenReturn(parentCurrentSize);
        when(path.getFileName()).thenReturn(Path.of("asdf"));

        return path;
    }

    @Test
    public void insertMe(){
        var node2 = new TreeFileNode(mock(NodeData.class));

        node.insertNode(node2);

        assertTrue(node.getChildren().contains(node2));
        assertFalse(node.getChildren().isEmpty());
    }

    @Test
    public void deleteMe(){
        var node2 = new TreeFileNode(mock(NodeData.class));

        node.insertNode(node2);
        node2.deleteMe();

        assertFalse(node.getChildren().contains(node2));
        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    public void updateMe(){
        var childCurrentSize = 3L;
        var childNewSize = 14L;
        var parentNewSize = childNewSize + parentCurrentSize;
        var mockedPath = mockPath();
        when(mockedPath.toFile().length()).thenReturn(childCurrentSize);
        var node2 = new TreeFileNode(new NodeData(mockedPath));

        node.insertNode(node2);
        when(node2.getValue().getPath().toFile().length()).thenReturn(childNewSize);
        node2.updateMe();

        assertEquals(parentNewSize, node.getValue().getAccumulatedSize());
        assertEquals(childNewSize, node2.getValue().getAccumulatedSize());
    }

    @Test
    public void equals(){
        var node2 = new TreeFileNode(mock(NodeData.class));

        assertNotEquals(node, node2);
    }
}
