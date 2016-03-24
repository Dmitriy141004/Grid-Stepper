package bin.levels;

import com.sun.org.apache.xerces.internal.dom.TextImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import bin.start.Main;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Level loader for Plates game.
 *
 * All levels are stored in {@code .xml} files. Here's structure of this file:
 *
 * <pre><code>
 * &lt;levels pack-name="some name here"&gt;
 *     &lt;level id="name of level"&gt;
 *         &lt;grid&gt;
 *             &lt;column&gt;
 *                 &lt;cell type="start" /&gt;
 *                 &lt;cell type="empty" /&gt;
 *             &lt;/column&gt;
 *             &lt;column&gt;
 *                 &lt;cell type="empty" /&gt;
 *                 &lt;cell type="finish" /&gt;
 *             &lt;/column&gt;
 *         &lt;/grid&gt;
 *     &lt;/level&gt;
 * &lt;/levels&gt;
 *     </code></pre>
 *
 * Levels stored in level packages, level packs stored in files. So, to make own level, you must create pack for this
 * level. That's caused by two things:
 * <ol>
 *     <li>I don't want to store single levels in packs,</li>
 *     <li>If they'll stored in multiple files - there'll be more time for loading.</li>
 * </ol>
 *
 * This class uses singleton construction. That's because to load {@code .xml} file using Java, you need to use some
 * classes ({@link DocumentBuilder}, {@link XPathFactory} and {@link XPath}), and I didn't want to initialize them in each
 * call of load method (some of this constructors can throw exception). And, I don't like using this construction
 * (anyway, I can change class to this construction, but I don't what to do this):
 *
 * <pre><code>
 * public class SomeClass {
 *     private static String someStaticField;
 *
 *     static {
 *         someStaticField = "field";
 *     }
 *
 *     public static void foo() {
 *         System.out.println(someStaticField);
 *     }
 * }
 *</code></pre>
 *
 * So, my first solution was to make function {@code setup()}:
 *
 * <pre><code>
 * public class SomeClass {
 *     private static String someStaticField;
 *
 *     public static void setup() {
 *         someStaticField = "field";
 *     }
 *
 *     public static void foo() {
 *         System.out.println(someStaticField);
 *     }
 * }
 * </code></pre>
 *
 * And call it in the {@link Main} class. But, I've found better solution for me. I have private instance of class,
 * and private constructor. All initializing run in it.
 *
 * @author Dmitriy Meleshko
 * @since v. 1.0
 *
 */
public class XMLLevelLoader {
    /** Private instance. */
    private static XMLLevelLoader xmlLevelLoader = new XMLLevelLoader();

    // Here're some XML utils
    private DocumentBuilder builder;
    private XPathFactory xPathFactory = XPathFactory.newInstance();
    private XPath xPath = xPathFactory.newXPath();

    /**
     * <b>Function</b> for loading level.
     *
     * @param path path to file. <i><b>Note:</b> it's {@link java.net.URI}, not {@link URL}!</i>
     * @param levelName name of level. See structure of file: all levels have name
     * @return 2D grid of level cells It's <code>ArrayList&lt;ArrayList&lt;String&gt;&gt;</code> because it's easier to operate
     * with {@link ArrayList} than with {@code Array}.
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     *
     * @see #loadLevel(URL, String)
     * @see #innerLoadLevel(String, String)
     *
     */
    private static ArrayList<ArrayList<LevelCell>> loadLevel(String path, String levelName) throws IOException, SAXException,
            XPathExpressionException {
        return xmlLevelLoader.innerLoadLevel(path, levelName);
    }

    /**
     * <b>Function</b> for loading level.
     *
     * @param path path to file. <i><b>Note:</b> it's {@link URL}, not {@link java.net.URI}!</i>
     * @param levelName name of level. See structure of file: all levels have name
     * @return 2D grid of level cells It's <code>ArrayList&lt;ArrayList&lt;String&gt;&gt;</code> because it's easier to operate
     * with {@link ArrayList} than with {@code Array}.
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     *
     * @see #loadLevel(String, String)
     * @see #innerLoadLevel(String, String)
     *
     */
    public static ArrayList<ArrayList<LevelCell>> loadLevel(URL path, String levelName) throws IOException, SAXException,
            XPathExpressionException {
        try {
            return loadLevel((new File(path.toURI())).getAbsolutePath(), levelName);
        } catch (URISyntaxException e) {
            throw new InvalidPathSyntaxException(path.toString());
        }
    }

    /**
     * As I said, here I setup some private fields.
     *
     */
    private XMLLevelLoader() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        try {
            builder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * <b>Method</b> for loading levels. {@link #loadLevel(String, String)} and {@link #loadLevel(URL, String)} just call
     * This method of private instance.
     *
     * @param path path to file. <i><b>Note:</b> it's {@link URL}, not {@link java.net.URI}!</i>
     * @param levelId id of level. See structure of file: all levels have id
     * @return 2D grid of level cells. It's <code>ArrayList&lt;ArrayList&lt;String&gt;&gt;</code> because it's easier to operate
     * with {@link ArrayList} than with {@code Array}.
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    private ArrayList<ArrayList<LevelCell>> innerLoadLevel(String path, String levelId) throws IOException, SAXException,
            XPathExpressionException {
        ArrayList<ArrayList<LevelCell>> level = new ArrayList<>(0);

        Document document = builder.parse(path);


        Element columns = (Element) xPath.evaluate(String.format("/level-pack/level[@id=\"%s\"]/game-field", levelId),
                document, XPathConstants.NODE);

        // Iterating columns
        for (int column = 0; column < columns.getChildNodes().getLength(); column++) {
            ArrayList<LevelCell> currentRow = new ArrayList<>(0);
            if (!(columns.getChildNodes().item(column) instanceof TextImpl)) {
                level.add(currentRow);

                // Iterating rows
                for (int cell = 0; cell < columns.getChildNodes().item(column).getChildNodes().getLength(); cell++) {
                    Node currentCell = columns.getChildNodes().item(column).getChildNodes().item(cell);

                    // It's protection from "very famous" NullPointerException
                    if (!(currentCell instanceof TextImpl)) {
                        LevelCell cellToAdd = null;

                        switch (((Element) currentCell).getAttribute("type")) {
                            case "wall":
                                cellToAdd = new LevelCell(LevelCell.CellType.WALL);
                                break;

                            case "empty":
                                cellToAdd = new LevelCell(LevelCell.CellType.EMPTY);
                                break;

                            case "start":
                                // We need to set start cell's property "visited" to true, because we haven't stepped on
                                // This cell before
                                cellToAdd = new LevelCell(LevelCell.CellType.START);
                                cellToAdd.setVisited(true);
                                break;

                            case "finish":
                                cellToAdd = new LevelCell(LevelCell.CellType.FINISH);
                                break;
                        }

                        currentRow.add(cellToAdd);
                    }
                }
            }
        }

        return level;
    }
}
