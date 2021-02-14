package org.agh.diskstalker.graphics;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.stereotype.Service;

@Service
public final class GraphicsFactory {
    private static final Image APPLICATION_ICON =
            new Image(GraphicsFactory.class.getResource("/images/app_icon.png").toString());

    private static final Image FOLDER_IMAGE =
            new Image(GraphicsFactory.class.getResource("/images/folder-16.png").toString());

    private static final Image FOLDER_LOADING_IMAGE =
            new Image(GraphicsFactory.class.getResource("/images/Rolling-1s-16px.gif").toString());

    private static final Image FILE_IMAGE =
            new Image(GraphicsFactory.class.getResource("/images/file-16.png").toString());

    private static final Image FOLDER_IMAGE_RED =
            new Image(GraphicsFactory.class.getResource("/images/folder-red-16.png").toString());

    public ImageView getGraphic(boolean isDirectory, boolean sizeExceeded) {
        if(isDirectory && sizeExceeded) return new ImageView(FOLDER_IMAGE_RED);
        return isDirectory ? new ImageView(FOLDER_IMAGE) : new ImageView(FILE_IMAGE);
    }

    public Image getApplicationIcon(){
        return APPLICATION_ICON;
    }

    public ImageView getLoadingGraphics() {
        return new ImageView(FOLDER_LOADING_IMAGE);
    }
}