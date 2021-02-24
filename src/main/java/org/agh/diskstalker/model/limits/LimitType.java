package org.agh.diskstalker.model.limits;

import lombok.Getter;

public enum LimitType {
    TOTAL_SIZE("total size"),
    FILES_AMOUNT("files amount"),
    LARGEST_FILE("largest file");

    @Getter private final String label;

    LimitType(String label) {
        this.label = label;
    }
}
