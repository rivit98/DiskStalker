package org.agh.diskstalker.graphics;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.nio.file.Path;

public final class Alerts {
    private Alerts() {
    }

    public static void tryingToAddSameFolderToObservedAlert() {
        var newAlert = createAlert("Warning", "You already observe this directory!");
        newAlert.showAndWait().filter(response -> response == ButtonType.OK);
    }

    public static void setMaxSizeAlert(String path, Long size) {
        var newAlert = createAlert("Information", "Max size for folder:\n" + path + "\nset to:\n" + size + " MB");
        newAlert.setAlertType(Alert.AlertType.INFORMATION);
        newAlert.showAndWait().filter(response -> response == ButtonType.OK);
    }

    public static void sizeExceededAlert(String path, Long size) {
        var newAlert = createAlert("Warning", "Folder:\n" + path + "\nexceeded size:\n" + size + " MB");
        Platform.runLater(newAlert::showAndWait);
    }

    public static void cannotAddFolderAlert(Path path) {
        var newAlert = createAlert("Error", "Cannot add " + path.toString(), AlertType.ERROR);
        Platform.runLater(newAlert::showAndWait);
    }

    private static Alert createAlert(String headerText, String information) {
        return createAlert(headerText, information, AlertType.WARNING);
    }

    private static Alert createAlert(String headerText, String information, AlertType type) {
        var alert = new Alert(type);
        alert.setHeaderText(headerText);
        alert.setContentText(information);
        return alert;
    }
}
