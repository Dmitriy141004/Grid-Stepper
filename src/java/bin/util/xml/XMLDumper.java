package bin.util.xml;

import com.sun.org.apache.xerces.internal.dom.TextImpl;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class for dumping (converting to simple string) {@code XML DOM} node tree. For example, following {@code .xml} file:
 *
 * <pre><code>
 * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
 *
 * &lt;bundle-descriptor&gt;
 *     &lt;bundle-container&gt;
 *         &lt;available-cases&gt;
 *             &lt;case key="ru" full-name="&#x420;&#x443;&#x441;&#x441;&#x43a;&#x438;&#x439;" /&gt;
 *             &lt;case key="ua" full-name="&#x423;&#x43a;&#x440;&#x430;&#x457;&#x43d;&#x441;&#x44c;&#x43a;&#x430;" /&gt;
 *             &lt;case key="en" full-name="English" /&gt;
 *             &lt;case key="de" full-name="Deutsch" /&gt;
 *             &lt;case key="fr" full-name="Fran&ccedil;ais" /&gt;
 *         &lt;/available-cases&gt;
 *     &lt;/bundle-container&gt;
 *
 *     &lt;paths&gt;
 *         &lt;bundle-root&gt;./&lt;/bundle-root&gt;
 *         &lt;file-name-pattern&gt;Locale_(\\w{2}).properties&lt;/file-name-pattern&gt;
 *     &lt;/paths&gt;
 * &lt;/bundle-descriptor&gt;
 *
 * </code></pre>
 *
 * After deserialize and serialize using {@link #dump(Document)} will be turned into such string:
 *
 * <pre><code>
 * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
 *
 * &lt;bundle-descriptor&gt;
 *     &lt;bundle-container&gt;
 *
 *         &lt;available-cases&gt;
 *
 *             &lt;case full-name="&#x420;&#x443;&#x441;&#x441;&#x43a;&#x438;&#x439;" key="ru" /&gt;
 *             &lt;case full-name="&#x423;&#x43a;&#x440;&#x430;&#x457;&#x43d;&#x441;&#x44c;&#x43a;&#x430;" key="ua" /&gt;
 *             &lt;case full-name="English" key="en" /&gt;
 *             &lt;case full-name="Deutsch" key="de" /&gt;
 *             &lt;case full-name="Fran&ccedil;ais" key="fr" /&gt;
 *         &lt;/available-cases&gt;
 *     &lt;/bundle-container&gt;
 *     &lt;paths&gt;
 *
 *         &lt;bundle-root&gt;./&lt;/bundle-root&gt;
 *         &lt;file-name-pattern&gt;Locale_(\\w{2}).properties&lt;/file-name-pattern&gt;
 *     &lt;/paths&gt;
 * &lt;/bundle-descriptor&gt;
 *
 * </code></pre>
 *
 * <i><b>Note:</b> this is true after-work result!</i>
 *
 */
public final class XMLDumper {
    private XMLDumper() {

    }

    /**
     * Turns {@link Document Document object} into string.
     *
     * @param document document to dump
     * @return {@code .xml}-file-like string.
     *
     * @see XMLDumper Javadoc from XMLDumper to view exmaples.
     */
    public static String dump(Document document) {
        StringBuilder out = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n\n");

        if (document == null) return out.toString();

        Element rootElement = document.getDocumentElement();

        boolean tagHasChildren = rootElement.getChildNodes().getLength() > 0;

        // Adding root element
        StringBuilder openingTag = new StringBuilder("<");
        openingTag.append(rootElement.getTagName());
        if (!tagHasChildren) openingTag.append(" />");
        else openingTag.append(">");
        out.append(openingTag);

        // This long code does just one thing - if root element has only inner text, it will be added to string
        if (rootElement.getFirstChild() != null && rootElement.getFirstChild().getNodeValue() != null
                && nodesToList(rootElement.getChildNodes()).stream()
                        .filter(item -> item instanceof TextImpl)
                        .count() == 1) {
            out.append(rootElement.getFirstChild().getNodeValue());
        }

        if (tagHasChildren) {
            parseChildren(rootElement, 1, out);
            out.append("\n</").append(rootElement.getTagName()).append(">");
        }

        return out.append("\n").toString();
    }

    /**
     * Function for recursive parsing nodes.
     *
     * @param element current node with children
     * @param multiplicity "How deep we are in stack?" value. Used to add spaces
     * @param builder {@link StringBuilder} for appending
     *
     */
    private static void parseChildren(Element element, int multiplicity, StringBuilder builder) {
        for (Node node : nodesToList(element.getChildNodes())) {
            if (node instanceof Element) {

                // Creating indent
                StringBuilder indent = new StringBuilder("\n");
                for (int i = 0; i < multiplicity; i++) indent.append("    ");
                builder.append(indent);

                // Filtering all children with type "Element" and counting them
                boolean tagHasChildren = nodesToList(node.getChildNodes()).stream()
                        .filter(item -> item instanceof Element)
                        .count() > 0;
                boolean tagHasText = false;
                if (node.getFirstChild() != null && node.getFirstChild() instanceof TextImpl && !tagHasChildren) {
                    tagHasChildren = true;
                    tagHasText = true;
                }

                builder.append("<");
                builder.append(((Element) node).getTagName());

                // Adding attributes
                if (node.getAttributes().getLength() > 0) {
                    builder.append(" ");

                    NamedNodeMap nodeMap = node.getAttributes();

                    // Wrapping NamedNodeMap object to HashMap. Key - name of attribute, value - its value
                    HashMap<String, String> nodeAttributes = new HashMap<>(nodeMap.getLength());

                    for (int i = 0; i < nodeMap.getLength(); i++)
                        nodeAttributes.put(nodeMap.item(i).getNodeName(), nodeMap.item(i).getNodeValue());

                    // Inserting each attribute
                    for (Iterator iter = nodeAttributes.keySet().iterator(); iter.hasNext(); ) {
                        String attrName = (String) iter.next();
                        builder.append(attrName)
                                .append("=\"")
                                .append(nodeAttributes.get(attrName))
                                .append("\"");

                        if (iter.hasNext()) builder.append(" ");
                    }
                }

                if (!tagHasChildren) builder.append(" />");
                else builder.append(">");

                if (node.getFirstChild() != null && node.getFirstChild().getNodeValue() != null)
                    builder.append(node.getFirstChild().getNodeValue());

                // If tag has children (at looks like "directory"), recursive call of function
                if (tagHasChildren) {
                    parseChildren((Element) node, multiplicity + 1, builder);
                    if (!tagHasText) builder.append(indent);
                    builder.append("</").append(((Element) node).getTagName()).append(">");
                }
            }
        }
    }

    /**
     * Makes from {@link NameList} object {@link ArrayList} with {@link Node nodes}.
     *
     * @param nodeList object to wrap
     * @return wrapped object.
     */
    private static ArrayList<Node> nodesToList(NodeList nodeList) {
        ArrayList<Node> out = new ArrayList<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) out.add(nodeList.item(i));

        return out;
    }
}
