package org.agh.diskstalker.application;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {
    @Value("${applicationTitle}")
    private String applicationTitle;

    private final String cssPath = "/styles/style.css";
    private final GraphicsFactory graphicsFactory;
    private final FxWeaver fxWeaver;

    @Autowired
    public PrimaryStageInitializer(FxWeaver fxWeaver, GraphicsFactory graphicsFactory) {
        this.fxWeaver = fxWeaver;
        this.graphicsFactory = graphicsFactory;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        var stage = event.getStage();
        stage.getIcons().add(graphicsFactory.getApplicationIcon());
        var fxControllerAndView = fxWeaver.load(MainController.class);
        var controller = fxControllerAndView.getController();
        fxControllerAndView.getView().ifPresent(view -> {
            var scene = new Scene((Parent) view);
            scene.getStylesheets().add(cssPath);

            configureStage(stage, controller);

            stage.setScene(scene);
            stage.show();
        });
    }

    private void configureStage(Stage primaryStage, MainController controller) {
        primaryStage.setResizable(false);
        primaryStage.setTitle(applicationTitle);

        primaryStage.setOnCloseRequest(event -> {
            controller.onExit();
        });
    }
}
