package org.agh.diskstalker.application;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class JavaFXPrimaryStage {
    private final String applicationTitle;
    private final String cssPath = "/styles/style.css";
    private final GraphicsFactory graphicsFactory;
    private final FxWeaver fxWeaver;

    @Autowired
    public JavaFXPrimaryStage(
            @Value("${applicationTitle}") String title,
            FxWeaver fxWeaver,
            GraphicsFactory graphicsFactory) {
        this.applicationTitle = title;
        this.fxWeaver = fxWeaver;
        this.graphicsFactory = graphicsFactory;
    }

    public void showMainView(Stage stage) {
        var fxControllerAndView = fxWeaver.load(MainController.class);
        fxControllerAndView.getView().ifPresent(view -> {
            var controller = fxControllerAndView.getController();
            var scene = new Scene((Parent) view);

            stage.getIcons().add(graphicsFactory.getApplicationIcon());
            scene.getStylesheets().add(cssPath);

            stage.setResizable(false);
            stage.setTitle(applicationTitle);

            stage.setOnCloseRequest(windowEvent -> {
                controller.onExit();
            });

            stage.setScene(scene);
            stage.show();
        });
    }
}
