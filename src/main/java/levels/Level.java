package levels;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import levels.cells.LevelCell;

import java.util.ArrayList;

/**
 * Bean-class for storing data about <b>one</b> level. It's used in table, so contains property fields and methods like
 * <pre><code>
 * public {propertyType} {propertyName}property() {
 *     return {propertyName};
 * }</code></pre>
 * that will be used in
 * <p><code>tableColumn.setCellValueFactory(new PropertyValueFactory&lt;&gt;("{propertyName}"))</code></p>
 *
 * @see #number
 * @see #name
 * @see #completed
 * @see #grid
 *
 */
public class Level implements Cloneable {
    /** Level's number property. */
    private SimpleIntegerProperty number;
    public SimpleIntegerProperty numberProperty() {
        return number;
    }
    /** Level's name property. */
    private SimpleStringProperty name;
    public SimpleStringProperty nameProperty() {
        return name;
    }
    /** Level completion state property. */
    private SimpleBooleanProperty completed;
    public SimpleBooleanProperty completedProperty() {
        return completed;
    }
    /** Level's grid/game-field. */
    private ArrayList<ArrayList<LevelCell>> grid;
    public ArrayList<ArrayList<LevelCell>> getGrid() {
        return grid;
    }

    /**
     * Creates new {@link Level}.
     *
     * <p><i><b>Note:</b> field {@link #completed} by default has value {@code false}.</i></p>
     *
     * @param number level's number
     * @param name level's name
     * @param grid level's grid/game-field
     */
    public Level(int number, String name, ArrayList<ArrayList<levels.cells.LevelCell>> grid) {
        this.number = new SimpleIntegerProperty(number);
        this.name = new SimpleStringProperty(
                // This construction capitalizes first char of string
                name.substring(0, 1).toUpperCase().concat(name.substring(1)));
        this.completed = new SimpleBooleanProperty(false);
        this.grid = grid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Level clone() {
        try {
            return (Level) super.clone();
        } catch (CloneNotSupportedException e) {
            // Isn't reachable in this method
            throw new RuntimeException(e);
        }
    }
}
