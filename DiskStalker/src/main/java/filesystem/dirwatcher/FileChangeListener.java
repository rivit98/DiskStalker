package filesystem.dirwatcher;

import model.events.EventType;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;

public class FileChangeListener extends FileAlterationListenerAdaptor {
    private final IFilesystemWatcher IFileSystemWatcher;
    public FileChangeListener(IFilesystemWatcher IFileSystemWatcher){
        this.IFileSystemWatcher = IFileSystemWatcher;
    }

    @Override
    public void onFileCreate(File file) {
        IFileSystemWatcher.emitEvent(file.toPath(), EventType.FILE_CREATED);
    }

    @Override
    public void onFileChange(File file) {
        IFileSystemWatcher.emitEvent(file.toPath(), EventType.FILE_MODIFIED);
    }

    @Override
    public void onFileDelete(File file) {
        IFileSystemWatcher.emitEvent(file.toPath(), EventType.FILE_DELETED);
    }

    @Override
    public void onDirectoryCreate(File directory) {
        IFileSystemWatcher.emitEvent(directory.toPath(), EventType.DIR_CREATED);
    }

    @Override
    public void onDirectoryChange(File directory) {
        IFileSystemWatcher.emitEvent(directory.toPath(), EventType.DIR_MODIFIED);
    }

    @Override
    public void onDirectoryDelete(File directory) {
        IFileSystemWatcher.emitEvent(directory.toPath(), EventType.DIR_DELETED);
    }
}
