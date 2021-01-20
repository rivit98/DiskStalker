package org.agh.diskstalker.application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.DiskStalkerMain;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class DiskStalkerSpringBootstrap extends Application {
    private JavaFXPrimaryStage javaFXPrimaryStage;
    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        log.info("Starting JavaFX DiskStalker");
        this.context = new SpringApplicationBuilder()
                .sources(DiskStalkerMain.class)
                .run(getParameters().getRaw().toArray(new String[0]));

        javaFXPrimaryStage = this.context.getBean(JavaFXPrimaryStage.class);
    }

    @Override
    public void start(Stage primaryStage) {
        javaFXPrimaryStage.showMainView(primaryStage);
    }

    @Override
    public void stop() {
        log.info("Closing JavaFX DiskStalker");
        context.close();
        Platform.exit();
    }
}
