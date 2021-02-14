package org.agh.diskstalker.controllers.alerts;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class AlertsFactory {
    public void tryingToAddSameFolderToObservedAlert() {
        createAlert(
                "You already observe this directory!"
        ).showAndWait();
    }

    public void setMaxSizeAlert(String path, Long size) {
        createAlert(
                "Max size for folder:\n" + path + "\nset to:\n" + FileUtils.byteCountToDisplaySize(size),
                AlertType.INFORMATION
        ).showAndWait();
    }

    public void setMaxFilesAmountAlert(String path, Long amount) {
        createAlert(
                "Max files amount for folder:\n" + path + "\nset to:\n" + amount,
                AlertType.INFORMATION
        ).showAndWait();
    }

    public void setBiggestFileAlert(String path, Long size) {
        createAlert(
                "Max size of file for folder:\n" + path + "\nset to:\n" + FileUtils.byteCountToDisplaySize(size),
                AlertType.INFORMATION
        ).showAndWait();
    }

    public void sizeExceededAlert(String path, Long size) {
        createAlert(
                "Folder:\n" + path + "\nexceeded size:\n" + FileUtils.byteCountToDisplaySize(size)
        ).showAndWait();
    }

    public void filesAmountExceededAlert(String path, Long amount) {
        createAlert(
                "Folder:\n" + path + "\nexceeded files amount:\n" + amount
        ).showAndWait();
    }

    public void biggestFileExceededAlert(String path, Long size) {
        createAlert(
                "Folder:\n" + path + "\nexceeded biggest file limit:\n" + FileUtils.byteCountToDisplaySize(size)
        ).showAndWait();
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

    private Alert createAlert(String information) {
        return createAlert(information, AlertType.WARNING);
    }

    private Alert createAlert(String information, AlertType type) {
        return new Alert(type, information);
    }
}
