package levels;

import com.sun.org.apache.xerces.internal.dom.TextImpl;
import levels.cells.EmptyCell;
import levels.cells.FinishCell;
import levels.cells.StartCell;
import levels.cells.WallCell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility-class for loading levels.
 *
 */
public class XMLLevelLoader {
    /**
     * Loads package of levels.
     *
     * @param location location of {@code .xml} file with level package.
     * @return list with levels/level package.
     * @throws IOException
     * @throws RuntimeException &#x00AB;as wrapper of&#x00BB; {@link ParserConfigurationException}, {@link SAXException}
     *                          and {@link XPathExpressionException}.
     */
    public static List<Level> loadLevelPack(String location) throws IOException {
        try {
            // Collection for levels
            ArrayList<Level> levels = new ArrayList<>(0);

            // XML load utils initializing
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            // Document with level pack
            Document document = builder.parse(location);
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
                        if (!(columns.getChildNodes().item(column) instanceof TextImpl)) {
                            levelGrid.add(currentRow);

                            // Iterating rows
                            for (int cell = 0; cell < columns.getChildNodes().item(column).getChildNodes().getLength(); cell++) {
                                Node currentCell = columns.getChildNodes().item(column).getChildNodes().item(cell);

                                // Protection from texts
                                if (!(currentCell instanceof TextImpl)) {
                                    levels.cells.LevelCell cellToAdd = null;

                                    switch (((Element) currentCell).getTagName()) {
                                        case "wall":
                                            cellToAdd = new WallCell();
                                            break;

                                        case "empty":
                                            cellToAdd = new EmptyCell();
                                            break;

                                        case "start":
                                            // We need to set start cell's property "visited" to true, because we haven't stepped on
                                            // This cell before
                                            cellToAdd = new StartCell();
                                            cellToAdd.setVisited(true);
                                            break;

                                        case "finish":
                                            cellToAdd = new FinishCell();
                                            break;
                                    }

                                    currentRow.add(cellToAdd);
                                }
                            } // End of iterating cells in column
                        }
                    } // End of iterating columns

                    levels.add(new Level(Integer.parseUnsignedInt(levelElement.getAttribute("number")),
                            levelElement.getAttribute("name"), levelGrid));
                }
            }

            return levels;
        } catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}
