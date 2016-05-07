package levels;

import com.sun.org.apache.xerces.internal.dom.DeferredCommentImpl;
import com.sun.org.apache.xerces.internal.dom.TextImpl;
import levels.cells.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import start.Main;
import util.xml.XMLSerializable;
import util.xml.XMLUtils;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class for storing data about <b>one</b> level pack/campaign.
 */
public class LevelPack implements XMLSerializable {
    private List<Level> levels;
    // Here're some duplicates of methods in java.util.List
    public List<Level> getLevels() {
        return levels;
    }
    public Level getLevel(int i) {
        return levels.get(i);
    }
    public int indexOfLevel(Level level) {
        return levels.indexOf(level);
    }
    public int levelsCount() {
        return levels.size();
    }
    public boolean isEmpty() {
        return levels.isEmpty();
    }
    public Stream<Level> stream() {
        return levels.stream();
    }

    private String name;
    public String getName() {
        return name;
    }

    public LevelPack(List<Level> levels, String name) {
        this.levels = levels;
        this.name = name;
    }

    /**
     * Gets next level after specified.
     *
     * @param level source level
     * @return {@link Optional} with {@link Level} if there're level after other one, otherwise - {@link Optional#EMPTY}.
     */
    public Optional<Level> getLevelAfter(Level level) {
        int levelIndex = indexOfLevel(level);
        if (levelIndex < levelsCount() - 1)
            return Optional.of(getLevel(levelIndex + 1));
        else
            return Optional.empty();
    }

    //////////////////////////////////// Serializing ////////////////////////////////////
    public String toXML() {
        final String indent = Main.XML_SERIALIZE_INDENT;
        StringBuilder builder = new StringBuilder();

        builder.append(generateXMLHeaders());
        for (Level level : levels)
            builder.append(levelToXML(level, indent));
        builder.append(generateXMLClosers());

        return builder.toString();
    }

    private String levelToXML(Level level, String indent) {
        return generateXMLLevelHeaders(level, indent) +
                levelGameFieldToXML(level, indent) +
                generateXMLLevelClosers(indent);
    }

    private String levelGameFieldToXML(Level level, String indent) {
        StringBuilder builder = new StringBuilder();
        for (ArrayList<LevelCell> column : level.getGrid()) {
            builder.append(indent).append(indent).append(indent).append("<column>\n");

            for (LevelCell cell : column) {
                builder.append(indent).append(indent).append(indent).append(indent)
                        .append(xmlTagFromCell(cell)).append("\n");
            }

            builder.append(indent).append(indent).append(indent).append("</column>\n");
        }
        return builder.toString();
    }

    private String xmlTagFromCell(LevelCell cell) {
        switch (cell.getType()) {
            case EMPTY:
            case FINISH:
            case START:
            case WALL:
                return String.format("<%-11s />", cell.getType().toString().toLowerCase());

            case BACKGROUND_SQUARE:
                return "<background />";

            default:
                throw new NullPointerException();
        }
    }

    private String generateXMLLevelClosers(String indent) {
        return indent + indent + "</game-field>\n" +
                indent + "</level>\n";
    }

    private String generateXMLLevelHeaders(Level level, String indent) {
        return indent + "<level number=\"" + level.getNumber() + "\" completed=\"" + level.isCompleted() + "\">\n" +
                indent + indent + "<game-field>\n";
    }

    private String generateXMLHeaders() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n\n" +
                "<level-pack name=\"classic\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "            xsi:noNamespaceSchemaLocation=\"level-pack-schema.xsd\">\n";
    }

    private String generateXMLClosers() {
        return "</level-pack>\n";
    }


    //////////////////////////////////// De-Serializing ////////////////////////////////////
    public static LevelPack fromXML(String filename) {
        try {
            // Collection for levels
            ArrayList<Level> levels = new ArrayList<>(0);

            // Document with level pack
            Document document = XMLUtils.parseDocument(filename);
            XMLUtils.validate(document, Main.getResourcePath("levels/level-pack-schema.xsd"));
            XPath xPath = XPathFactory.newInstance().newXPath();

            // Root element with levels
            Element root = (Element) xPath.evaluate("/level-pack", document, XPathConstants.NODE);
            // Iterating each level
            for (int lvlI = 0; lvlI < root.getChildNodes().getLength(); lvlI++) {
                // Protection for texts
                if (!(root.getChildNodes().item(lvlI) instanceof TextImpl)) {
                    Element levelElement = (Element) root.getChildNodes().item(lvlI);

                    Element columns = (Element) xPath.evaluate("game-field", levelElement, XPathConstants.NODE);

                    ArrayList<ArrayList<levels.cells.LevelCell>> levelGrid = new ArrayList<>(0);

                    // Iterating columns
                    for (int column = 0; column < columns.getChildNodes().getLength(); column++) {
                        ArrayList<levels.cells.LevelCell> currentRow = new ArrayList<>(0);
                        if (!(columns.getChildNodes().item(column) instanceof TextImpl)
                                && !(columns.getChildNodes().item(column) instanceof DeferredCommentImpl)) {
                            levelGrid.add(currentRow);

                            // Iterating rows
                            for (int cell = 0; cell < columns.getChildNodes().item(column).getChildNodes().getLength(); cell++) {
                                Node currentCell = columns.getChildNodes().item(column).getChildNodes().item(cell);

                                // Protection from texts
                                if (!(currentCell instanceof TextImpl) && !(currentCell instanceof DeferredCommentImpl)) {
                                    levels.cells.LevelCell cellToAdd = null;

                                    switch (((Element) currentCell).getTagName()) {
                                        case "wall":
                                            cellToAdd = new WallCell();
                                            break;

                                        case "empty":
                                            cellToAdd = new EmptyCell();
                                            break;

                                        case "start":
                                            // We need to set start cell's property "visited" to true, because we haven't
                                            // stepped on this cell before
                                            cellToAdd = new StartCell();
                                            cellToAdd.setVisited(true);
                                            break;

                                        case "finish":
                                            cellToAdd = new FinishCell();
                                            break;

                                        case "background":
                                            cellToAdd = new BackgroundSquare();
                                            break;
                                    }

                                    currentRow.add(cellToAdd);
                                }
                            } // End of iterating cells in column
                        }
                    } // End of iterating columns

                    levels.add(new Level(levelElement.getAttribute("number"), levelGrid,
                            Boolean.parseBoolean(levelElement.getAttribute("completed"))));
                }
            }
            enableNeededLevelButtons(levels);

            return new LevelPack(levels, root.getAttribute("name"));
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This function enables all buttons of completed levels and enables first not-completed-level-button.
     *
     * @param levels list with levels
     * @return modified list with levels.
     */
    private static ArrayList<Level> enableNeededLevelButtons(ArrayList<Level> levels) {
        Iterator<Level> iterator = levels.iterator();
        boolean stopped = false;
        while (iterator.hasNext() && !stopped) {
            Level next = iterator.next();
            next.getButtonRepresentation().setDisable(false);
            stopped = !next.isCompleted();
        }

        return levels;
    }
}
