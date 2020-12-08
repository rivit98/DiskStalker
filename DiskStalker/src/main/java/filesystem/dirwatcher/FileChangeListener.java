package filesystem.dirwatcher;

import model.events.EventType;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;

public class FileChangeListener extends FileAlterationListenerAdaptor {
    private final IFilesystemWatcher filesystemWatcher;
    public FileChangeListener(IFilesystemWatcher filesystemWatcher){
        this.filesystemWatcher = filesystemWatcher;
    }

    @Override
    public void onFileCreate(File file) {
        filesystemWatcher.emitEvent(file.toPath(), EventType.FILE_CREATED);
    }

    @Override
    public void onFileChange(File file) {
        filesystemWatcher.emitEvent(file.toPath(), EventType.FILE_MODIFIED);
    }

    @Override
    public void onFileDelete(File file) {
        filesystemWatcher.emitEvent(file.toPath(), EventType.FILE_DELETED);
    }

    @Override
    public void onDirectoryCreate(File directory) {
        filesystemWatcher.emitEvent(directory.toPath(), EventType.DIR_CREATED);
    }

    @Override
    public void onDirectoryChange(File directory) {
        filesystemWatcher.emitEvent(directory.toPath(), EventType.DIR_MODIFIED);
    }

    @Override
    public void onDirectoryDelete(File directory) {
        filesystemWatcher.emitEvent(directory.toPath(), EventType.DIR_DELETED);
    }
}
