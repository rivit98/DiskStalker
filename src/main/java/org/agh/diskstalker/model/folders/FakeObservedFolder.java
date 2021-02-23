package org.agh.diskstalker.model.folders;


import io.reactivex.rxjava3.subjects.PublishSubject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.events.observedFolderEvents.AbstractObservedFolderEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderEvent;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;
import org.agh.diskstalker.model.limits.FolderLimits;
import org.agh.diskstalker.model.tree.NodeData;
import org.agh.diskstalker.model.tree.NodesTree;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.agh.diskstalker.statistics.TypeRecognizer;
import org.agh.diskstalker.statistics.TypeStatistics;

import java.nio.file.Path;
import java.util.Objects;

@Slf4j
@Getter
public class FakeObservedFolder implements ILimitableObservableFolder {
    private final ILimitableObservableFolder realObservedFolder;
    private final TreeFileNode fakeNode;

    public FakeObservedFolder(ILimitableObservableFolder folder) {
        this.realObservedFolder = folder;
        this.fakeNode = new TreeFileNode(new NodeData(folder.getPath()));
    }

    @Override
    public boolean containsNode(Path path) {
        return path.equals(realObservedFolder.getPath());
    }

    @Override
    public TreeFileNode getNodeByPath(Path path) {
        if (containsNode(path)) {
            return fakeNode;
        }
        return null;
    }

    @Override
    public Path getPath() {
        return realObservedFolder.getPath();
    }

    @Override
    public String getName() {
        return realObservedFolder.getName();
    }

    @Override
    public boolean isScanning() {
        return realObservedFolder.isScanning();
    }

    @Override
    public void destroy() {
        realObservedFolder.destroy();
    }

    @Override
    public FolderLimits getLimits() {
        return realObservedFolder.getLimits();
    }

    @Override
    public TypeStatistics getTypeStatistics() {
        return realObservedFolder.getTypeStatistics();
    }

    @Override
    public void setTypeRecognizer(TypeRecognizer typeRecognizer) {
        realObservedFolder.setTypeRecognizer(typeRecognizer);
    }

    @Override
    public TypeRecognizer getTypeRecognizer() {
        return realObservedFolder.getTypeRecognizer();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FakeObservedFolder that = (FakeObservedFolder) o;
        return Objects.equals(realObservedFolder, that.getRealObservedFolder());
    }




    private void unsupported() throws UnsupportedOperationException{
        log.warn("Not supported in FakeFolder");
        throw new UnsupportedOperationException("Not supported in FakeFolder");
    }

    @Override
    public void scan() {
        unsupported();
    }

    @Override
    public void emitEvent(ObservedFolderEvent event) {
        unsupported();
    }

    @Override
    public long getSize() {
        unsupported();
        return 0;
    }

    @Override
    public long getFilesAmount() {
        unsupported();
        return 0;
    }

    @Override
    public long getBiggestFileSize() {
        unsupported();
        return 0;
    }

    @Override
    public void setLimits(FolderLimits limits) {
        unsupported();
    }

    @Override
    public PublishSubject<ObservedFolderEvent> getEventStream() {
        unsupported();
        return null;
    }

    @Override
    public NodesTree getNodesTree() {
        unsupported();
        return null;
    }
}
