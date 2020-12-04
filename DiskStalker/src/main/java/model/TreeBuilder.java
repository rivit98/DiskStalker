package model;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.internal.operators.single.SingleToObservable;
import io.reactivex.rxjava3.subjects.SingleSubject;

import java.nio.file.Path;


public class TreeBuilder {
    private final SingleSubject<TreeFileNode> rootSubject = SingleSubject.create();
    private TreeFileNode root;

    public TreeFileNode addItem(FileData item) {
        var node = new TreeFileNode(item);
        if (root == null) {
            root = node;
            rootSubject.onSuccess(root);
        }else{
            root.addNode(node);
        }
        return node;
    }

    public SingleSubject<TreeFileNode> getRoot() {
        return rootSubject;
    }
}

