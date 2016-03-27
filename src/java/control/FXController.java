package control;

import javafx.scene.Node;
import javafx.scene.Parent;
import start.Main;

import java.lang.reflect.Field;
import java.util.ResourceBundle;

/**
 * Interface for JavaFX controllers.
 *
 * @author Dmitriy Meleshko
 * @since v. 1.0
 *
 */
public abstract class FXController {
    /** Interface object of controller's {@code .fxml}. Is used to {@link Parent#lookup(String) "look for"} elements
     * by {@code fx:id}. */
    private Parent parent;
    /** Current resource bundle. */
    ResourceBundle resourceBundle;

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
     * Alternative for method {@code initialize} in standard JavaFX controllers. But, there's a difference. When
     * {@link Main#changeScene(String, String)} changes scene - this method activates.
     *
     */
    public abstract void init();

    /**
     * Sets all fields annotated as {@link FXMLLink} to real links with {@code .fxml} elements.
     *
     */
    public void registerElements() {
        // Getting all declared fields, because we need also to set private fields
        Field[] fields = getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(FXMLLink.class)) {

                // Note: you can't annotate one thing with duplicate annotations, so received array will be always with
                // size 0
                FXMLLink annotation = field.getDeclaredAnnotationsByType(FXMLLink.class)[0];
                // If annotation's parameter "fxId" is empty string - fx:id is  field's name; Otherwise, it's this parameter
                String fxId = (annotation.fxId().isEmpty()) ? field.getName() : annotation.fxId();

                Node node = parent.lookup("#" + fxId);

                // If field is private, we make it accessible and then turn it back
                boolean fieldIsPrivate = !field.isAccessible();

                if (fieldIsPrivate) field.setAccessible(true);

                try {
                    field.set(this, node);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (fieldIsPrivate) field.setAccessible(false);
            }
        }
    }

    /**
     * Method returns new instance of controller, like factory.
     *
     * @return new instance of controller.
     */
    public abstract FXController newInstance();

    public static void initController(FXController target, Parent parent, ResourceBundle resourceBundle) {
        target.setParent(parent);
        target.setResources(resourceBundle);
        target.registerElements();
        target.init();
    }
}
