package org.agh.diskstalker.application;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.agh.diskstalker.controllers.MainView;
import org.agh.diskstalker.persistence.ConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {
    @Value("${applicationTitle}")
    private String applicationTitle;

    private final FxWeaver fxWeaver;

    static { //TODO: where am I supposed to put this? :D
        ConnectionProvider.init("jdbc:sqlite:observed_folders.db");
    }

    @Autowired
    public PrimaryStageInitializer(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        var stage = event.getStage();
        var fxControllerAndView = fxWeaver.load(MainView.class);
        var controller = fxControllerAndView.getController();
        fxControllerAndView.getView().ifPresent(view -> {
            var scene = new Scene((Parent) view);
            scene.getStylesheets().add("/styles/style.css");

            configureStage(stage, controller);

            stage.setScene(scene);
            stage.show();
        });
    }

    private void configureStage(Stage primaryStage, MainView controller) {
        primaryStage.setResizable(false);
        primaryStage.setTitle(applicationTitle);

        primaryStage.setOnCloseRequest(event -> {
            controller.onExit();
            try {
                ConnectionProvider.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }
}
