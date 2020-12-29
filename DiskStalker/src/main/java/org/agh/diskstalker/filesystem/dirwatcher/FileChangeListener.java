package org.agh.diskstalker.filesystem.dirwatcher;

import org.agh.diskstalker.model.events.FilesystemEventType;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;

public class FileChangeListener extends FileAlterationListenerAdaptor {
    private final IFilesystemWatcher filesystemWatcher;

    public FileChangeListener(IFilesystemWatcher filesystemWatcher) {
        this.filesystemWatcher = filesystemWatcher;
    }

    @Override
    public void onFileCreate(File file) {
        filesystemWatcher.emitEvent(file.toPath(), FilesystemEventType.FILE_CREATED);
    }

    @Override
    public void onFileChange(File file) {
        filesystemWatcher.emitEvent(file.toPath(), FilesystemEventType.FILE_MODIFIED);
    }

    @Override
    public void onFileDelete(File file) {
        filesystemWatcher.emitEvent(file.toPath(), FilesystemEventType.FILE_DELETED);
    }

    @Override
    public void onDirectoryCreate(File directory) {
        filesystemWatcher.emitEvent(directory.toPath(), FilesystemEventType.DIR_CREATED);
    }

    @Override
    public void onDirectoryChange(File directory) {
        filesystemWatcher.emitEvent(directory.toPath(), FilesystemEventType.DIR_MODIFIED);
    }

    @Override
    public void onDirectoryDelete(File directory) {
        filesystemWatcher.emitEvent(directory.toPath(), FilesystemEventType.DIR_DELETED);
    }
}
