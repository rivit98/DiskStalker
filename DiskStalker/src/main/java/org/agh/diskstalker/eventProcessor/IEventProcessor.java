package org.agh.diskstalker.eventProcessor;

import org.agh.diskstalker.model.events.filesystemEvents.FilesystemEvent;

public interface IEventProcessor {
    void processEvent(FilesystemEvent filesystemEvent);
}
