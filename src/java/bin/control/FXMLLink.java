package bin.control;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Works just like {@link javafx.fxml.FXML}, but all fields are processed by {@link FXController#registerElements()}.
 * There's difference between "parent" - you can specify {@code fx:id} as annotation parameter, but be default it is
 * empty string. If it is empty string - {@link FXController#registerElements()} sets {@code fx:id} to field name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FXMLLink {
    String fxId() default "";
}
