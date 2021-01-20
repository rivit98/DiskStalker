package org.agh.diskstalker.controllers.cellFactories;

import javafx.css.PseudoClass;
import javafx.scene.control.TableCell;
import org.agh.diskstalker.model.NodeData;

import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateColumnCellFactory extends TableCell<NodeData, FileTime> {

    public static final String dateFormat = "uuuu-MM-dd HH:mm:ss";

    public DateColumnCellFactory() {
        pseudoClassStateChanged(PseudoClass.getPseudoClass("centered"), true);
    }

    @Override
    protected void updateItem(FileTime value, boolean empty) {
        super.updateItem(value, empty);
        if (value == null || empty) {
            setText(null);
        } else {
            var formattedDate = DateTimeFormatter.ofPattern(dateFormat)
                    .withZone(ZoneId.systemDefault())
                    .format(value.toInstant());
            setText(formattedDate);
        }
    }
}