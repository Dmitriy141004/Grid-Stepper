package util.javafx.nodes;

import javafx.scene.Node;
import javafx.scene.Parent;

import java.util.function.Consumer;

/**
 * Utility-class for doing something with JavaFX nodes.
 */
public class JFXNodes {
    /**
     * Iterates all nodes in tree.
     *
     * @param root root node of tree
     * @param function {@link Consumer} function that will be applied with all nodes
     */
    public static void forAllChildren(Parent root, Consumer<Node> function) {
        for (Node child : root.getChildrenUnmodifiable()) {
            if (child instanceof Parent)
                forAllChildren((Parent) child, function);
            function.accept(child);
        }
    }
}
