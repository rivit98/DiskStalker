package model;

import filesystem.DirWatcher;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;

public class FileChangeListener extends FileAlterationListenerAdaptor {
    private final DirWatcher dirWatcher;
    public FileChangeListener(DirWatcher dirWatcher){
        this.dirWatcher = dirWatcher;
    }

    @Override
    public void onFileCreate(File file) {
        dirWatcher.emitKey(file.toPath(), EventType.FILE_CREATED);
    }

    @Override
    public void onFileChange(File file) {
        dirWatcher.emitKey(file.toPath(), EventType.FILE_MODIFIED);
    }

    @Override
    public void onFileDelete(File file) {
        dirWatcher.emitKey(file.toPath(), EventType.FILE_DELETED);
    }

    @Override
    public void onDirectoryCreate(File directory) {
        dirWatcher.emitKey(directory.toPath(), EventType.DIR_CREATED);
    }

    @Override
    public void onDirectoryChange(File directory) {
        dirWatcher.emitKey(directory.toPath(), EventType.DIR_MODIFIED);
    }

    @Override
    public void onDirectoryDelete(File directory) {
        dirWatcher.emitKey(directory.toPath(), EventType.DIR_DELETED);
    }
}
