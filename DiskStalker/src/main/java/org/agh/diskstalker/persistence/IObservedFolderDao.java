package org.agh.diskstalker.persistence;

import org.agh.diskstalker.model.ObservedFolder;

import java.util.List;

public interface IObservedFolderDao {
    void save(ObservedFolder observedFolder);

    void update(ObservedFolder observedFolder);

    void delete(ObservedFolder observedFolder);

    List<ObservedFolder> getAll();
}
