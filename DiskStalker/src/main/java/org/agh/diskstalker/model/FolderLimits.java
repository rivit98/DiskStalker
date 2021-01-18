package org.agh.diskstalker.model;

import lombok.Getter;
import lombok.Setter;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEventType;
import org.agh.diskstalker.events.observedFolderEvents.AbstractObservedFolderEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedBiggestFileChangedEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderFilesAmountChangedEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderSizeChangedEvent;

@Getter
public class FolderLimits {
    private final ObservedFolder folder;

    @Setter
    private boolean totalSizeExceededFlag = false;
    @Setter
    private boolean filesAmountExceededFlag = false;
    @Setter
    private boolean biggestFileExceededFlag = false;

    private long totalSizeLimit = 0;
    private long filesAmountLimit = 0;
    private long biggestFileLimit = 0;


    public FolderLimits(ObservedFolder folder) {
        this.folder = folder;
    }

    private void sendEvent(AbstractObservedFolderEvent event){
        folder.sendEvent(event);
    }

    private void sendSizeChangedEvent(){
        sendEvent(new ObservedFolderSizeChangedEvent(folder));
    }

    private void sendFileAmountChangedEvent(){
        sendEvent(new ObservedFolderFilesAmountChangedEvent(folder));
    }

    private void sendBiggestFileChangedEvent(){
        sendEvent(new ObservedBiggestFileChangedEvent(folder));
    }

    public void updateIfNecessary(FilesystemEvent event) {
        var eventType = event.getType();
        if(eventType.isFileEvent()){
            sendBiggestFileChangedEvent(); //biggest size could change on every file event
            sendSizeChangedEvent(); //size could change on every file event

            if(eventType != FilesystemEventType.FILE_MODIFIED){ //create or delete
                sendFileAmountChangedEvent();
            }
        }
    }

    public void setMaxTotalSize(long limit){
        totalSizeLimit = limit;
        sendSizeChangedEvent();
    }

    public void setMaxFilesAmount(long limit){
        filesAmountLimit = limit;
        sendFileAmountChangedEvent();
    }

    public void setBiggestFileLimit(long limit){
        biggestFileLimit = limit;
        sendBiggestFileChangedEvent();
    }

    public boolean isTotalSizeExceeded(){
        return totalSizeLimit > 0 && folder.getSize() > totalSizeLimit;
    }

    public boolean isFilesAmountExceeded(){
        return filesAmountLimit > 0 && folder.getFilesAmount() > filesAmountLimit;
    }

    public boolean isBiggestFileLimitExceeded(){
        return biggestFileLimit > 0 && folder.getBiggestFileSize() > biggestFileLimit;
    }

    public boolean isAnyLimitExceeded(){
        return isTotalSizeExceeded() || isFilesAmountExceeded() || isBiggestFileLimitExceeded();
    }
}
