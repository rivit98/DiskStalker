package org.agh.diskstalker.controllers.alerts;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;

//TODO: inject as spring service
public class Alerts {
    private Alerts() {
    }

    public static void tryingToAddSameFolderToObservedAlert() {
        var newAlert = createAlert(
                "You already observe this directory!"
        );
        newAlert.showAndWait().filter(response -> response == ButtonType.OK);
    }

    public static void setMaxSizeAlert(String path, Long size) {
        var newAlert = createAlert(
                "Max size for folder:\n" + path + "\nset to:\n" + FileUtils.byteCountToDisplaySize(size),
                AlertType.INFORMATION
        );
        newAlert.showAndWait().filter(response -> response == ButtonType.OK);
    }

    public static void setMaxFilesAmountAlert(String path, Long amount) {
        var newAlert = createAlert(
                "Max files amount for folder:\n" + path + "\nset to:\n" + amount,
                AlertType.INFORMATION
        );
        newAlert.showAndWait().filter(response -> response == ButtonType.OK);
    }

    public static void setBiggestFileAlert(String path, Long size) {
        var newAlert = createAlert(
                "Max size of file for folder:\n" + path + "\nset to:\n" + FileUtils.byteCountToDisplaySize(size),
                AlertType.INFORMATION
        );
        newAlert.showAndWait().filter(response -> response == ButtonType.OK);
    }

    public static void sizeExceededAlert(String path, Long size) {
        var newAlert = createAlert(
                "Folder:\n" + path + "\nexceeded size:\n" + FileUtils.byteCountToDisplaySize(size)
        );
        Platform.runLater(newAlert::showAndWait);
    }

    public static void filesAmountExceededAlert(String path, Long amount) {
        var newAlert = createAlert(
                "Folder:\n" + path + "\nexceeded files amount:\n" + amount
        );
        Platform.runLater(newAlert::showAndWait);
    }

    public static void biggestFileExceededAlert(String path, Long amount) {
        var newAlert = createAlert(
                "Folder:\n" + path + "\nexceeded biggest file limit:\n" + amount
        );
        Platform.runLater(newAlert::showAndWait);
    }

    public static void genericErrorAlert(Path path, String message) {
        var newAlert = createAlert(
                path.toString() + "\n" + message,
                AlertType.ERROR
        );
        Platform.runLater(newAlert::showAndWait);
    }

    public static ButtonType yesNoDeleteAlert(Path path) {
        var newAlert = new Alert(Alert.AlertType.CONFIRMATION, "Do you really want to delete: "
                + path.getFileName() + "?", ButtonType.YES, ButtonType.NO);
        return newAlert.showAndWait().orElse(ButtonType.NO);
    }

    private static Alert createAlert(String information) {
        return createAlert(information, AlertType.WARNING);
    }

    private static Alert createAlert(String information, AlertType type) {
        var alert = new Alert(type);
        alert.setContentText(information);
        return alert;
    }
}
