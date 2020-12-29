package org.agh.diskstalker.application;

import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.UnaryOperator;

public class StringToIntFormatter {

    public StringToIntFormatter() {
    }

    public TextFormatter<Integer> getFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            var newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        };
        return new TextFormatter<>(new IntegerStringConverter(), 0, filter);
    }
}
