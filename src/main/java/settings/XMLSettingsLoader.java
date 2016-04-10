package settings;

import com.sun.org.apache.xerces.internal.dom.TextImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import start.Main;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;

/**
 * Loader for {@code XML} settings. By the way, how settings are stored? This's example of storage:
 *
 * <pre><code>
 * &lt;settings&gt;
 *     &lt;setting key="lang" value="ru" /&gt;
 * &lt;/settings&gt;
 * </code></pre>
 *
 * As you can see, settings stored in pairs {@code name-value}, so they are deserialized in a simple {@link HashMap}.
 *
 */
public class XMLSettingsLoader {
    /** Storage of loaded settings.
     * <ul>
     *     <li>Key - setting name.</li>
     *     <li>Value - setting value.</li>
     * </ul> */
    private static HashMap<String, String> settingsStorage;

    /**
     * Returns setting value from storage.
     *
     * @param key string key for setting (in other words - setting's name)
     * @return value of setting.
     */
    public static String getSetting(String key) {
        settingsLoadCheck();
        return settingsStorage.get(key);
    }

    /**
     * Sets setting from storage.
     *
     * @param key name of setting
     * @param value new value of setting
     *
     */
    public static void setSetting(String key, String value) {
        settingsLoadCheck();
        settingsStorage.put(key, value);
    }

    /**
     * Getter for {@link #settingsStorage}
     *
     * @return value of {@link #settingsStorage}.
     *
     */
    public static HashMap<String, String> getSettingsStorage() {
        settingsLoadCheck();
        return settingsStorage;
    }

    /**
     * Checks if settings are loaded. If they aren't loaded - it loads them.
     *
     */
    private static void settingsLoadCheck() {
        if (settingsStorage == null) {
            settingsStorage = new HashMap<>(0);

            try {
                // Some XML util classes are used at once
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);
                DocumentBuilder parser = documentBuilderFactory.newDocumentBuilder();

                // Document with settings
                Document settingsDoc =
                        parser.parse(Main.getResource("settings/settings.xml"));

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
                            settingsStorage.put(setting.getAttribute("key"), setting.getAttribute("value"));
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
