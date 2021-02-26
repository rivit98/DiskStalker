package org.agh.diskstalker.alerts;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import org.agh.diskstalker.model.limits.LimitType;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class AlertsFactory {
    public void tryingToAddSameFolderToObservedAlert() {
        createAlert(
                "You already observe this directory!",
                AlertType.WARNING
        ).showAndWait();
    }

    public void setLimit(String path, LimitType limitType, Long value) {
        var valueLabel = formatLimitValue(limitType, value);

        createAlert(
                String.format("Folder:\n%s\nset %s limit to:\n%s", path, limitType.getLabel(), valueLabel),
                AlertType.INFORMATION
        ).showAndWait();
    }

    public void limitExceededAlert(String path, LimitType limitType, Long value) {
        var valueLabel = formatLimitValue(limitType, value);

        createAlert(
                String.format("Folder:\n%s\nexceeded %s limit:\n%s", path, limitType.getLabel(), valueLabel),
                AlertType.WARNING
        ).showAndWait();
    }

    private String formatLimitValue(LimitType limitType, Long value){
        return switch (limitType) {
            case FILES_AMOUNT -> value.toString();
            default -> FileUtils.byteCountToDisplaySize(value);
        };
    }

    public void genericErrorAlert(Path path, String message) {
        createAlert(
                path.toString() + "\n" + message,
                AlertType.ERROR
        ).showAndWait();
    }

    public ButtonType yesNoDeleteAlert(Path path) {
        return new Alert(
                Alert.AlertType.CONFIRMATION,
                "Do you really want to delete: " + path.getFileName() + "?",
                ButtonType.YES, ButtonType.NO
        )
                .showAndWait()
                .orElse(ButtonType.NO);
    }

    private Alert createAlert(String information, AlertType type) {
        return new Alert(type, information);
    }
}
