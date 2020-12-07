package controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Alerts {
    public Alerts(){}

    public void tryingToAddSameFolderToObservedAlert(){
        var newAlert = createAlert("Warning", "You already observe this directory!");
        newAlert.showAndWait().filter(response -> response == ButtonType.OK);
    }

    private Alert createAlert(String headerText, String information) {
        var alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(headerText);
        alert.setContentText(information);
        return alert;
    }
}
