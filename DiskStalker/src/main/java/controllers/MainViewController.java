package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainViewController {
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TextField directorySize;

    @FXML
    public void initialize(){
        initializeAddButton();
    }

    private void initializeAddButton(){
        addButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose directory");
            directoryChooser.showDialog(new Stage());
        });
    }
}
