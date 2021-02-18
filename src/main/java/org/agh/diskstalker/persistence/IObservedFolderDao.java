package org.agh.diskstalker.persistence;

import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;
import org.agh.diskstalker.model.interfaces.IObservedFolder;

import java.util.List;

public interface IObservedFolderDao {
    void create(IObservedFolder IObservedFolder);

    void update(ILimitableObservableFolder observedFolder);

    void delete(IObservedFolder IObservedFolder);

    List<IObservedFolder> getAll();
}
