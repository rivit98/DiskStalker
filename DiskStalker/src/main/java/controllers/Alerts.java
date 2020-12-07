package controllers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Alerts {
    public Alerts(){}

    public void tryingToAddSameFolderToObservedAlert(){
        var newAlert = createAlert("Warning", "You already observe this directory!");
        newAlert.showAndWait().filter(response -> response == ButtonType.OK);
    }

    public void setMaxSizeAlert(String path, Long size){
        var newAlert = createAlert("Information", "Max size for folder:\n" + path + "\nset to:\n" + size + " MB");
        newAlert.setAlertType(Alert.AlertType.INFORMATION);
        newAlert.showAndWait().filter(response -> response == ButtonType.OK);
    }

    public void sizeExceededAlert(String path, Long size){
        var newAlert = createAlert("Warning", "Folder:\n" + path + "\nexceeded size:\n" + size + " MB");
        Platform.runLater(newAlert::showAndWait);
    }

    private Alert createAlert(String headerText, String information) {
        var alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(headerText);
        alert.setContentText(information);
        return alert;
    }
}
