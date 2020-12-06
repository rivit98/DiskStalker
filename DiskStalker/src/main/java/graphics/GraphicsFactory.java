package graphics;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class GraphicsFactory { //TODO: inject as singleton? :D
    private static final Image FOLDER_IMAGE =
            new Image(
                    Objects.requireNonNull(
                            Thread.currentThread().getContextClassLoader().getResource("images/folder-16.png")
                    ).toString()
            );

    private static final Image FILE_IMAGE =
            new Image(
                    Objects.requireNonNull(
                            Thread.currentThread().getContextClassLoader().getResource("images/file-16.png")
                    ).toString()
            );

    public static ImageView getGraphic(boolean isDirectory) {
        return isDirectory ? new ImageView(FOLDER_IMAGE) : new ImageView(FILE_IMAGE);
    }
}