package org.agh.diskstalker.model.limits;

import javafx.beans.property.SimpleBooleanProperty;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEventType;
import org.agh.diskstalker.events.observedFolderEvents.*;
import org.agh.diskstalker.model.folders.ObservedFolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

public class FolderLimits {
    private final ObservedFolder folder;

    private final HashMap<LimitType, SimpleBooleanProperty> flags = new HashMap<>();
    private final HashMap<LimitType, Long> limits = new HashMap<>();
    private final HashMap<LimitType, Supplier<Long>> consumers = new HashMap<>();
    private final HashMap<LimitType, Runnable> updaters = new HashMap<>();

    public FolderLimits(ObservedFolder folder) {
        this.folder = folder;

        Arrays.stream(LimitType.values()).forEach(type -> {
            flags.put(type, new SimpleBooleanProperty(false));
            limits.put(type, 0L);
        });
        consumers.put(LimitType.TOTAL_SIZE, folder::getSize);
        consumers.put(LimitType.FILES_AMOUNT, folder::getFilesAmount);
        consumers.put(LimitType.BIGGEST_FILE, folder::getBiggestFileSize);

        updaters.put(LimitType.TOTAL_SIZE, this::sendSizeChangedEvent);
        updaters.put(LimitType.FILES_AMOUNT, this::sendFileAmountChangedEvent);
        updaters.put(LimitType.BIGGEST_FILE, this::sendBiggestFileChangedEvent);
    }

    private void sendEvent(ObservedFolderEvent event) {
        folder.emitEvent(event);
    }

    private void sendSizeChangedEvent() {
        sendEvent(new ObservedFolderSizeChangedEvent(folder));
    }

    private void sendFileAmountChangedEvent() {
        sendEvent(new ObservedFolderFilesAmountChangedEvent(folder));
    }

    private void sendBiggestFileChangedEvent() {
        sendEvent(new ObservedBiggestFileChangedEvent(folder));
    }

    public void updateIfNecessary(FilesystemEvent event) {
        var eventType = event.getType();
        if (eventType.isFileEvent()) {
            sendBiggestFileChangedEvent(); //biggest size could change on every file event
            sendSizeChangedEvent(); //size could change on every file event

            if (eventType != FilesystemEventType.FILE_MODIFIED) { //create or delete
                sendFileAmountChangedEvent();
            }
        }
    }

    public void checkLimits(){
        Arrays.stream(LimitType.values()).filter(this::isLimitExceeded).forEach(this::setShown);
    }

    public void setLimit(LimitType limitType, long value){
        limits.put(limitType, value);
        updaters.get(limitType).run();
    }

    public boolean isLimitExceeded(LimitType type){
        var limit = limits.get(type);
        var consumer = consumers.get(type);
        return limit > 0 && consumer.get() > limit;
    }

    public boolean isAnyLimitExceeded() {
        return Arrays.stream(LimitType.values()).anyMatch(this::isLimitExceeded);
    }

    public Long get(LimitType type) {
        return Optional.ofNullable(limits.get(type)).orElse(0L);
    }

    public boolean wasShown(LimitType type) {
        return flags.get(type).get();
    }

    public void setShown(LimitType type) {
        setShown0(type, true);
    }

    public void clearShown(LimitType type) {
        setShown0(type, false);
    }

    private void setShown0(LimitType type, boolean value) {
        flags.get(type).set(value);
    }

    @Override
    public String toString() {
        var builder = new StringBuilder("FolderLimits{");

        limits.forEach((k, v) -> {
            builder.append(k.toString());
            builder.append("=");
            builder.append(v.toString());
            builder.append(", ");
        });

        builder.append("}");
        return builder.toString();
    }
}
