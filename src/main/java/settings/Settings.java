package settings;

import com.sun.org.apache.xerces.internal.dom.TextImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import start.Main;
import util.xml.XMLSerializable;
import util.xml.XMLUtils;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for storing data about application settings.
 *
 * @see Main#appSettings
 */
public class Settings implements XMLSerializable {
    /**
     * Storage of loaded settings.
     * <ul>
     * <li>Key - setting name.</li>
     * <li>Value - setting value.</li>
     * </ul>
     */
    private Map<String, String> settingsStorage = new HashMap<>(0);
    public void setSetting(String key, Object value) {
        String stringVal = value.toString();
        if (settingsStorage.containsKey(key))
            settingsStorage.replace(key, stringVal);
        else
            settingsStorage.put(key, stringVal);
    }
    public String getSetting(String key) {
        return settingsStorage.get(key);
    }

    //////////////////////////////////// Serializing ////////////////////////////////////
    @Override
    public String toXML() {
        final String indent = Main.XML_SERIALIZE_INDENT;
        StringBuilder builder = new StringBuilder();

        builder.append(generateXMLHeaders(indent));
        for (String key : settingsStorage.keySet())
            builder.append(settingToXML(new Setting(key, settingsStorage.get(key)), indent));
        builder.append(generateXMLClosers(indent));

        return builder.toString();
    }

    private String settingToXML(Setting setting, String indent) {
        return indent + indent + "<setting key=\"" + setting.key + "\" value=\"" + setting.value + "\" />\n";
    }

    private String generateXMLHeaders(String indent) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n\n" +
                "<settings xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:noNamespaceSchemaLocation=\"settings-schema.xsd\">\n" +
                indent + "<storage>\n";
    }

    private String generateXMLClosers(String indent) {
        return indent + "</storage>\n" +
                "</settings>\n";
    }

    /**
     * Object for helping serializing.
     *
     * @see #key
     * @see #value
     */
    private class Setting {
        private String key;
        private String value;

        Setting(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    //////////////////////////////////// De-Serializing ////////////////////////////////////
    /**
     * Loads settings from {@code XML}. This's example of storage:
     *
     * <pre><code>
     * &lt;settings&gt;
     *     &lt;setting key="lang" value="ru" /&gt;
     * &lt;/settings&gt;
     * </code></pre>
     *
     * As you can see, settings stored in pairs {@code name-value}, so they are deserialized in a simple {@link HashMap}.
     *
     * @param filename filename
     * @return loaded settings.
     */
    public static Settings fromXML(String filename) {
        try {
            Settings settingsStorage = new Settings();

            // Document with settings
            Document settingsDoc = XMLUtils.parseDocument(filename);
            XMLUtils.validate(settingsDoc, Main.getResourcePath("settings/settings-schema.xsd"));

            // "XPath factories"
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            // Root config element
            Element configRootNode = (Element) xPath.evaluate("/settings/storage", settingsDoc, XPathConstants.NODE);

            for (int i = 0; i < configRootNode.getChildNodes().getLength(); i++) {
                Node settingNode = configRootNode.getChildNodes().item(i);

                // Protection from texts
                if (!(settingNode instanceof TextImpl)) {
                    Element setting = (Element) settingNode;

                    if (setting.getTagName().equals("setting"))
                        settingsStorage.setSetting(setting.getAttribute("key"), setting.getAttribute("value"));
                }
            }

            return settingsStorage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
