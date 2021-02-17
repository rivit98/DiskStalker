package org.agh.diskstalker.events.filesystemEvents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;

@AllArgsConstructor
@Getter
@ToString
public class FilesystemEvent {
    private final Path targetDir;
    private final FilesystemEventType type;
}
