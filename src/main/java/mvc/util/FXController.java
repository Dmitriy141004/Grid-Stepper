package mvc.util;

import javafx.scene.Parent;
import start.Main;

import java.util.ResourceBundle;

/**
 * Interface for JavaFX controllers.
 *
 */
public abstract class FXController implements Cloneable {
    /** Interface object of controller's {@code .fxml}. Is used to {@link Parent#lookup(String) "look for"} elements
     * by {@code fx:id}. */
    protected Parent parent;
    /** Current resource bundle. */
    private ResourceBundle resourceBundle;

    /**
     * Setter for field {@link #resourceBundle}.
     *
     * @param resources new value of {@link #resourceBundle}
     */
    public void setResources(ResourceBundle resources) {
        resourceBundle = resources;
    }

    protected String getLocaleStr(String key) {
        return resourceBundle.getString(key);
    }

    /**
     * Setter for private field {@link #parent}.
     *
     * @param parent new property
     */
    public void setParent(Parent parent) {
        this.parent = parent;
    }

    /**
     * Initializes controller's components. This method is activated when {@link util.SceneLoader#loadPackage(String)}
     * loads scene, this thing is like constructor or JavaFX's native {@code initialize}.
     *
     */
    public abstract void init();

    /**
     * This method is called after {@link Main#changeScene(String, String)} have been used. Runs controller.
     *
     */
    public abstract void run();

    /**
     * This method is called after {@link Main#changeScene(String, String)} have been used. Is used to reset controller's
     * old data.
     *
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
