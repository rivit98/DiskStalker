package org.agh.diskstalker.events.eventProcessor;

import org.agh.diskstalker.events.filesystemEvents.FilesystemEvent;

public interface IEventProcessor {
    void processEvent(FilesystemEvent filesystemEvent);
}
