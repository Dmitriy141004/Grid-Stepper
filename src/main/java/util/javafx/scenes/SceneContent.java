package util.javafx.scenes;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import mvc.help.FXController;

/**
 * Struct-class for storing data about scenes.
 */
public class SceneContent {
    public Parent root;
    public FXMLLoader loader;
    public FXController controller;
    public Scene scene;

    public SceneContent(FXMLLoader loader) {
        this.loader = loader;
        root = loader.getRoot();

        controller = loader.getController();
        if (controller != null) {
            controller.setResources(loader.getResources());
            controller.init();
        }

        scene = new Scene(root);
    }
}
