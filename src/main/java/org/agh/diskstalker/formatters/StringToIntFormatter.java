package org.agh.diskstalker.formatters;

import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.UnaryOperator;

public class StringToIntFormatter extends TextFormatter<Integer>{
    private static final UnaryOperator<TextFormatter.Change> filter = change -> {
        if (change.getControlNewText().matches("\\d*")) {
            return change;
        }
        return null;
    };

    public StringToIntFormatter() {
        super(new IntegerStringConverter(), null, filter);
    }
}
