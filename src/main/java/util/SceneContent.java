package util;

import control.FXController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Class for storing data about scenes (yes, I mean "it's {@code struct} from C").
 *
 * @see #root
 * @see #loader
 *
 */
public class SceneContent {
    /** Root-pane/object of scene. */
    public Parent root;
    /** FXMLLoader which loaded scene. */
    public FXMLLoader loader;
    /** Controller of scene. */
    public FXController controller;
    /** Scene object. */
    public Scene scene;
    /**
     * Creates new instance of class
     *
     * @param loader {@link FXMLLoader} which loaded {@link Parent} object
     */
    public SceneContent(FXMLLoader loader) {
        this.loader = loader;
        root = loader.getRoot();

        controller = loader.getController();
        if (controller != null) {
            controller.setResources(loader.getResources());
            controller.setParent(root);
            controller.init();
        }

        scene = new Scene(root);
    }
}
