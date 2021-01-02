package org.agh.diskstalker.application;

import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.UnaryOperator;

public class StringToIntFormatter extends TextFormatter<Integer>{
    private static final UnaryOperator<TextFormatter.Change> filter = change -> {
        var newText = change.getControlNewText();
        if (newText.matches("\\d*")) {
            return change;
        }
        return null;
    };

    public StringToIntFormatter() {
        super(new IntegerStringConverter(), 0, filter);
    }
}
