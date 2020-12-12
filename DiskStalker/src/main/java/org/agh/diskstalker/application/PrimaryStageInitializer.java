package org.agh.diskstalker.application;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.agh.diskstalker.controllers.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {
    private final FxWeaver fxWeaver;

    @Autowired
    public PrimaryStageInitializer(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        var stage = event.getStage();
        var scene = new Scene(fxWeaver.loadView(MainView.class));
        scene.getStylesheets().add("/styles/style.css");

        configureStage(stage);

        stage.setScene(scene);
        stage.show();
    }

    private void configureStage(Stage primaryStage/*, GridPane layout*/) {
//        Scene scene = new Scene(layout);
//        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Disk Stalker");
//        primaryStage.minWidthProperty().bind(layout.minWidthProperty());
//        primaryStage.minHeightProperty().bind(layout.minHeightProperty());
    }
}
