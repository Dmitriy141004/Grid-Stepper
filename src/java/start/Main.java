package start;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import control.MainMenuController;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import settings.XMLSettingsLoader;
import util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class for Plates game.
 *
 * @author Dmitriy Meleshko
 * @since v. 1.0
 *
 */
public class Main extends Application {
    /** Default width of window. */
    private static final int DEFAULT_WINDOW_WIDTH = 800;
    /** Default height of window. */
    private static final int DEFAULT_WINDOW_HEIGHT = 557;
    /** Minimal width of window. */
    private static final int MIN_WINDOW_WIDTH = 767;
    /** Minimal height of window. */
    private static final int MIN_WINDOW_HEIGHT = 556;

    /** Primary stage of application. */
    public static Stage primaryStage;
    /** Already loaded {@code .fxml} files from scene directory ({@code fxml}).
     * {@code key} - name of file
     * {@code value} - {@code .fxml} file. */
    private static HashMap<String, SceneContent> loadedScenes = new HashMap<>(0);

    /** Exit dialog object. I store this object in this class, instead of {@link MainMenuController},
     * because it is shown when shutdown hook activates. */

    private static Alert exitDialog;

    /** Exit action of exit dialog. */
    public static ButtonType EXIT_OPTION;
    /** Cancel action of exit dialog. */
    public static ButtonType CANCEL_OPTION;
    /** Available locales from {@code Locale} bundle. */
    private static ArrayList<String> availableLocales;
    /** Regexp for searching locale properties files (with extension {@code .properties}). For example, text
     * <p>{@code Locale_ch.properties}</p>
     * will be found, but text
     * <p>{@code some-file.xml}</p>
     * won't be found. */
    private static final Pattern LOCALE_RB_SEARCH_PATTERN = Pattern.compile("Locale_(\\w{2})\\.properties");
    /** Path to directory with resources. I don't put slash at the end, because it is everywhere between this constant
     * and path to certain resource. */
    private static String RESOURCES_ROOT;
    /** Field for build version. It looks like this:
     *
     * <pre><code>1.23.40</code></pre>
     *
     * Description for numbers:
     * <ol>
     *     <li><b>GLOBAL VERSION.</b> It's number of realize with many differences.</li>
     *     <li><b>COMMIT VERSION.</b> It's number of "git commit"s.</li>
     *     <li><b>BUILD VERSION.</b> After each launch of Ant build script this number increments by one. </li>
     * </ol>
     */
    private static String PRODUCT_VERSION;
    /** Locale resource bundle object. */
    public static ResourceBundle resourceBundle;

    /**
     * Getter for field {@link #PRODUCT_VERSION}.
     *
     * @return value of {@link #PRODUCT_VERSION}.
     */
    public static String getProductVersion() {
        return PRODUCT_VERSION;
    }

    /**
     * Getter for {@link #availableLocales} variable.
     *
     * @return value of {@link #availableLocales}.
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<String> getAvailableLocales() {
        return (ArrayList<String>) availableLocales.clone();
    }

    /**
     * Returns relative path to resource. At start adds constant {@link #RESOURCES_ROOT}, and at the end - resource name.
     *
     * @param pathSuffix name of resource. It will be added at the end.
     * @return real path to resource.
     */
    public static String getResource(String pathSuffix) {
        return PathsUtil.realPath(Main.RESOURCES_ROOT + pathSuffix);
    }

    /**
     * Works like {@link #getResource(String)}, but returns {@link URL} with path to resource.
     *
     * <p style="font-size: 13pt; font-weight: bold;">From {@link #getResource(String)}:</p>
     * @param pathSuffix name of resource. It will be added at the end.
     * @return real path to resource.
     */
    public static URL getResourceURL(String pathSuffix) {
        try {
            return new URL("file", null, PathsUtil.realPath(Main.RESOURCES_ROOT + pathSuffix));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Getter for field {@link #exitDialog}.
     *
     * @return value of {@link #exitDialog}.
     */
    public static Alert getExitDialog() {
        return exitDialog;
    }

    public static void main(String[] args) throws Exception {
        // Setting path to root of resources
        String callerClassFile = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();

        StringBuilder builder = new StringBuilder();
        if (callerClassFile.matches("(.+).jar")) builder.append("../");
        builder.append("../../resources/");
        RESOURCES_ROOT = builder.toString();

        // Loading product version
        try {
            PRODUCT_VERSION = "v. " + FileIO.load(getResource(".build_version"));
        } catch (IOException e) {
            e.printStackTrace();
            PRODUCT_VERSION = "Unknown version";
        }

        // Shutdown hook for saving app data
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Some XML util classes are used at once
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);


                // Creating document for saving
                Document settingsDocument = documentBuilderFactory.newDocumentBuilder().newDocument();

                // Creating root node for document and storage
                Element rootSettingsElement = settingsDocument.createElement("settings");
                settingsDocument.appendChild(rootSettingsElement);
                // Adding settings schema
                rootSettingsElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                rootSettingsElement.setAttribute("xsi:noNamespaceSchemaLocation", "settings-schema.xsd");

                Element storageElement = settingsDocument.createElement("storage");
                rootSettingsElement.appendChild(storageElement);

                for (String settingName : XMLSettingsLoader.getSettingsStorage().keySet()) {
                    Element newElement = settingsDocument.createElement("setting");
                    newElement.setAttribute("key", settingName);
                    newElement.setAttribute("value", XMLSettingsLoader.getSetting(settingName));
                    storageElement.appendChild(newElement);
                }

                // Serializing XML (it uses Apache's XMLSerializer, because it does work more clearly than Transformer class)
                // Some format settings
                OutputFormat format = new OutputFormat(settingsDocument);
                format.setEncoding("UTF-8");
                format.setIndenting(true);
                // Doing needed thing
                StringWriter dumpedDocument = new StringWriter();
                XMLSerializer dumper = new XMLSerializer(dumpedDocument, format);
                dumper.serialize(settingsDocument);

                // Saving data
                FileIO.write(dumpedDocument.toString(), Main.getResource("settings/settings.xml"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        launch(args);
    }
    /**
     * Returns string value from resource bundle "Locale".
     *
     * @param key locale/RB string key to get
     * @return string value.
     */
    public static String getLocaleStr(String key) {
        return resourceBundle.getString(key);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Setup media and bundles
        // Class loader for bundles, uses URL location
        ClassLoader bundleClassLoader = new URLClassLoader(new URL[] {
                new File(getResource("bundles/")).toURI().toURL()
        });

        resourceBundle = ResourceBundle.getBundle("Locale",
                new Locale(XMLSettingsLoader.getSetting("lang")), bundleClassLoader, new UTF8Control());

        // This code gets available locales
        availableLocales = new ArrayList<>(0);
        File localeRBDir = new File(getResource("bundles/"));
        for (String fileName : localeRBDir.list()) {
            Matcher fileMatcher = LOCALE_RB_SEARCH_PATTERN.matcher(fileName);
            if (fileMatcher.find()) {
                availableLocales.add(fileMatcher.group(1));
            }
        }

        EXIT_OPTION = new ButtonType(getLocaleStr("exit"), ButtonBar.ButtonData.OK_DONE);
        CANCEL_OPTION = new ButtonType(getLocaleStr("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        // Setting primary stage
        Main.primaryStage = primaryStage;
        primaryStage.setWidth(DEFAULT_WINDOW_WIDTH);
        primaryStage.setHeight(DEFAULT_WINDOW_HEIGHT);
        // Loading scenes
        loadedScenes = (HashMap<String, SceneContent>) SceneLoader.loadPackage(Main.getResource("fxml/"));

        // Exit dialog setup
        exitDialog = new Alert(Alert.AlertType.CONFIRMATION, getLocaleStr("dialogs.body.exit-from-game"),
                EXIT_OPTION, CANCEL_OPTION);
        exitDialog.setTitle(getLocaleStr("header.base") + " - " + getLocaleStr("exit"));
        exitDialog.setHeaderText(getLocaleStr("dialogs.head.exit-from-game"));

        // Adding CSS-Stylesheet to customize dialog, for example, fonts
        exitDialog.getDialogPane().getStylesheets().add(Main.getResourceURL("styles/bigger-dialog-fonts.css").toExternalForm());
        exitDialog.getDialogPane().getStyleClass().add("dialog-body");

        // And, customizing dialog with setters
        Label contentLabel = (Label) exitDialog.getDialogPane().lookup(".content");
        contentLabel.setWrapText(true);

        exitDialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        exitDialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        exitDialog.getDialogPane().setMaxHeight(Region.USE_PREF_SIZE);


        primaryStage.setOnCloseRequest(event -> {
            Optional result = getExitDialog().showAndWait();

            if (result.isPresent() && result.get() == EXIT_OPTION) {
                System.exit(0);
            } else if (result.isPresent() && result.get() == CANCEL_OPTION) {
                getExitDialog().hide();
                event.consume();
            }
        });


        // Changing scene to default
        changeScene("main.fxml", getLocaleStr("header.base"));
    }

    /**
     * Changes primary stage to given scene.
     *
     * @param sceneIdentifier name of scene's {@code .fxml} file
     * @param windowTitle window's title
     * @throws RuntimeException when loading fails.
     *
     */
    public static void changeScene(String sceneIdentifier, String windowTitle) {
        try {
            // Getting scene content
            SceneContent sceneContent = loadedScenes.get(sceneIdentifier);

            primaryStage.setTitle(windowTitle);
            primaryStage.setMinWidth(Main.MIN_WINDOW_WIDTH);
            primaryStage.setMinHeight(Main.MIN_WINDOW_HEIGHT);
            primaryStage.setScene(sceneContent.scene);
            // Resetting controller
            sceneContent.controller.reset();
            sceneContent.controller.run();
            // Showing window if it isn't present
            if (!primaryStage.isShowing()) primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns scene's string content (in fact - content of {@code .fxml} file).
     *
     * @param sceneIdentifier name of {@link .fxml} file in which scene is (includes {@link .fxml}).
     * @return string content of selected scene.
     */
    public static SceneContent getSceneContent(String sceneIdentifier) {
        return loadedScenes.get(sceneIdentifier);
    }
}
