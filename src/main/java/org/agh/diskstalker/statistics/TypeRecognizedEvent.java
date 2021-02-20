package org.agh.diskstalker.statistics;

import lombok.Data;

@Data
public class TypeRecognizedEvent {
    public static final String UNKNOWN_TYPE = "unknown";

    private String type;
}
