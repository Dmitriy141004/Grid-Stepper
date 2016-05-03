package mvc.util;

import start.Main;
import util.javafx.scenes.SceneLoader;

import java.util.ResourceBundle;

/**
 * Interface for JavaFX controllers.
 */
public abstract class FXController implements Cloneable {
    private ResourceBundle resourceBundle;
    public void setResources(ResourceBundle resources) {
        resourceBundle = resources;
    }
    protected String getLocaleStr(String key) {
        return resourceBundle.getString(key);
    }

    /**
     * Initializes controller's components. This method is activated when {@link SceneLoader#loadAllFromDirectory(String)}
     * loads scene, this thing is like constructor or JavaFX's native {@code initialize}.
     */
    public abstract void init();

    /**
     * This method is called after {@link Main#changeScene(String, String)} have been used. Runs controller.
     */
    public abstract void run();

    /**
     * This method is called after {@link Main#changeScene(String, String)} have been used. Is used to reset controller's
     * old data.
     */
    public abstract void reset();

    /**
     * {@inheritDoc}
     */
    @Override
    public FXController clone() {
        try {
            return (FXController) super.clone();
        } catch (CloneNotSupportedException e) {
            // Isn't reachable in this case
            throw new RuntimeException(e);
        }
    }
}
