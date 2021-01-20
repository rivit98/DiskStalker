package org.agh.diskstalker.events.filesystemEvents;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

@AllArgsConstructor
@Getter
public class FilesystemEvent {
    private final Path targetDir;
    private final FilesystemEventType type;
}
