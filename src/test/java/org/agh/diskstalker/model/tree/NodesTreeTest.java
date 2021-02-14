package org.agh.diskstalker.model.tree;

import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.NodeData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodesTreeTest {
    private Path path;
    private NodesTree builder;

    @BeforeEach
    public void setUp() {
        builder = new NodesTree();
        path = mock(Path.class);
        when(path.toFile()).thenReturn(mock(File.class));
        when(path.getFileName()).thenReturn(mock(Path.class));
    }

    @Test
    public void processNodeData() {
        //when
        builder.processNodeData(new NodeData(path));

        //then
        Assertions.assertTrue(builder.containsNode(path));
        Assertions.assertFalse(builder.containsNode(mock(Path.class)));
    }

    @Test
    public void insertNode() {
        //given
        var parentPath = mock(Path.class);
        when(parentPath.toFile()).thenReturn(mock(File.class));
        when(parentPath.getFileName()).thenReturn(mock(Path.class));
        when(path.getParent()).thenReturn(parentPath);
        builder.processNodeData(new NodeData(parentPath));

        //when
        builder.insertNewNode(new TreeFileNode(new NodeData(path)));

        //then
        Assertions.assertTrue(builder.containsNode(path));
        Assertions.assertTrue(builder.containsNode(parentPath));
    }

    @Test
    public void removeMappedDirsRecursively() {
        //given
        var nodeData = new NodeData(path);
        builder.processNodeData(nodeData);
        //when
        builder.removeMappedDirs(new TreeItem<>(nodeData));

        //then
        Assertions.assertFalse(builder.containsNode(path));
    }

    @Test
    public void rootEmitted(){
        var subscriber = builder.getRootSubject().test();
        subscriber.assertEmpty();

        builder.processNodeData(new NodeData(path));

        subscriber.assertValueCount(1);
        subscriber.assertComplete();
        subscriber.assertNoErrors();
    }
}
