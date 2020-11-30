import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class DiskStalker extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("views/mainView.fxml"));
        GridPane layout = loader.load();
        configureStage(primaryStage, layout);
        primaryStage.show();
    }

    private void configureStage(Stage primaryStage, GridPane layout) {
        Scene scene = new Scene(layout);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Disk Stalker");
        primaryStage.minWidthProperty().bind(layout.minWidthProperty());
        primaryStage.minHeightProperty().bind(layout.minHeightProperty());
    }
}
