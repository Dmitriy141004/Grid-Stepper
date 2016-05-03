package start;

import javafx.application.Application;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import levels.LevelPack;
import mvc.controllers.MainMenuController;
import settings.Settings;
import util.io.FileIO;
import util.io.PathsUtil;
import util.io.UTF8Control;
import util.javafx.nodes.JFXNodes;
import util.javafx.scenes.SceneContent;
import util.javafx.scenes.SceneLoader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class for Plates game.
 */
public class Main extends Application {
    /**
     * Indent for serializing {@link LevelPack#toXML() level packs} and {@link Settings#toXML() settings} to XML.
     */
    public static final String XML_SERIALIZE_INDENT = "    ";

    public static final String APP_FONT_NAME = "Arial";

    private static final int DEFAULT_WINDOW_WIDTH = 800;
    private static final int DEFAULT_WINDOW_HEIGHT = 557;
    private static final int MIN_WINDOW_WIDTH = 767;
    private static final int MIN_WINDOW_HEIGHT = 556;
    public static Stage primaryStage;
    /**
     * Regexp for searching locale properties files (with extension {@code .properties}). For example, text
     * <p>{@code Locale_ch.properties}</p>
     * will be found, but text
     * <p>{@code some-file.xml}</p>
     * won't be found.
     */
    private static final Pattern LOCALE_RB_SEARCH_PATTERN = Pattern.compile("Locale_(\\w{2})\\.properties");

    public static ResourceBundle resourceBundle;

    /* See ResourceBundle#getString(String) */
    public static String getLocaleStr(String key) {
        return resourceBundle.getString(key);
    }

    private static ArrayList<String> availableLocales;
    @SuppressWarnings("unchecked")
    public static ArrayList<String> getAvailableLocales() {
        return (ArrayList<String>) availableLocales.clone();
    }

    public static LevelPack classicCampaign;
    public static LevelPack extendedCampaign;
    /**
     * Loaded {@code .fxml} files from scene directory ({@code fxml}).
     * {@code key} - name of file
     * {@code value} - {@link SceneContent} object
     */
    private static HashMap<String, SceneContent> loadedScenes = new HashMap<>(0);

    private static String resourcesRoot;
    /**
     * Field for build version. It looks like this:
     * <p>
     * <pre><code>1.23.40</code></pre>
     *
     * Description for numbers:
     * <ol>
     * <li><b>GLOBAL VERSION.</b> It's number of realize with many differences.</li>
     * <li><b>COMMIT VERSION.</b> It's number of "git commit"s.</li>
     * <li><b>BUILD VERSION.</b> After each launch of Ant build script this number increments by one. </li>
     * </ol>
     */
    private static String productVersion;
    public static String getProductVersion() {
        return productVersion;
    }

    private static Settings appSettings;
    public static Settings getAppSettings() {
        return appSettings;
    }

    /**
     * Returns relative path to resource. At start adds constant {@link #resourcesRoot}, and at the end - resource name.
     *
     * @param pathSuffix name of resource. It will be added at the end.
     * @return real path to resource.
     */
    public static String getResourcePath(String pathSuffix) {
        return PathsUtil.realPath(Main.resourcesRoot + pathSuffix);
    }

    /**
     * Works like {@link #getResourcePath(String)}, but returns {@link URL} with path to resource.
     *
     * <p style="font-size: 13pt; font-weight: bold;">From {@link #getResourcePath(String)}:</p>
     *
     * @param pathSuffix name of resource. It will be added at the end.
     * @return real path to resource.
     */
    public static URL getResourceURL(String pathSuffix) {
        try {
            return new URL("file", null, PathsUtil.realPath(Main.resourcesRoot + pathSuffix));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    /**
     * Changes primary stage to given scene.
     *
     * @param sceneIdentifier name of scene's {@code .fxml} file
     * @throws RuntimeException when loading fails.
     */
    public static void changeScene(String sceneIdentifier) {
        changeScene(sceneIdentifier, "");
    }

    /**
     * Changes primary stage to given scene.
     *
     * @param sceneIdentifier name of scene's {@code .fxml} file
     * @param windowTitle window's title
     * @throws RuntimeException when loading fails.
     */
    public static void changeScene(String sceneIdentifier, String windowTitle) {
        try {
            SceneContent sceneContent = loadedScenes.get(sceneIdentifier);

            primaryStage.setTitle(getLocaleStr("header.base") + windowTitle);
            primaryStage.setMinWidth(Main.MIN_WINDOW_WIDTH);
            primaryStage.setMinHeight(Main.MIN_WINDOW_HEIGHT);
            primaryStage.setScene(sceneContent.scene);

            if (sceneContent.controller != null) {
                sceneContent.controller.reset();
                sceneContent.controller.run();
            }

            if (!primaryStage.isShowing())
                primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns scene's string content (in fact - content of {@code .fxml} file).
     *
     * @param sceneIdentifier name of {@code .fxml} file in which scene is (includes {@code .fxml}).
     * @return string content of selected scene.
     */
    public static SceneContent getSceneContent(String sceneIdentifier) {
        return loadedScenes.get(sceneIdentifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        setupResourcesRoot();
        loadAppSettings();
        loadCampaigns();
        loadProductVersion();
        registerShutdownHooks();
        loadResourceBundles();
        setupPrimaryStage(primaryStage);
        loadScenes();

        changeScene("main.fxml");
    }

    private void setupResourcesRoot() {
        String thisClassFile = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        StringBuilder builder = new StringBuilder();
        if (thisClassFile.endsWith(".jar"))
            builder.append("../");
        builder.append("../../resources/");
        resourcesRoot = builder.toString();
    }

    private void loadAppSettings() {
        appSettings = Settings.fromXML(Main.getResourcePath("settings/settings.xml"));
    }

    private void loadCampaigns() {
        classicCampaign = LevelPack.fromXML(Main.getResourcePath("levels/classic.xml"));
        extendedCampaign = LevelPack.fromXML(Main.getResourcePath("levels/extended.xml"));
    }

    private void loadProductVersion() {
        try {
            productVersion = "v. " + FileIO.load(Main.getResourcePath(".build_version"));
        } catch (RuntimeException e) {
            e.printStackTrace();
            productVersion = "Unknown version";
        }
    }

    private void registerShutdownHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FileIO.write(appSettings.toXML(), Main.getResourcePath("settings/settings.xml"));
            FileIO.write(classicCampaign.toXML(), Main.getResourcePath("levels/classic.xml"));
            FileIO.write(extendedCampaign.toXML(), Main.getResourcePath("levels/extended.xml"));
        }));
    }

    private void loadResourceBundles() throws MalformedURLException {
        getLocaleRB();
    }

    private void getLocaleRB() throws MalformedURLException {
        ClassLoader bundleClassLoader = new URLClassLoader(new URL[] {
                new File(getResourcePath("bundles/")).toURI().toURL()
        });
        resourceBundle = ResourceBundle.getBundle("Locale",
                new Locale(appSettings.getSetting("lang")), bundleClassLoader, new UTF8Control());
        registerAvailableLocales();
    }

    private void registerAvailableLocales() {
        availableLocales = new ArrayList<>(0);
        File localeRBDir = new File(getResourcePath("bundles/"));
        for (String fileName : localeRBDir.list()) {
            Matcher fileMatcher = LOCALE_RB_SEARCH_PATTERN.matcher(fileName);
            if (fileMatcher.find()) {
                availableLocales.add(fileMatcher.group(1));
            }
        }
    }

    private void setupPrimaryStage(Stage primaryStage) {
        Main.primaryStage = primaryStage;
        primaryStage.setWidth(DEFAULT_WINDOW_WIDTH);
        primaryStage.setHeight(DEFAULT_WINDOW_HEIGHT);

        primaryStage.setOnCloseRequest(event -> {
            MainMenuController controller = (MainMenuController) loadedScenes.get("main.fxml").controller;

            Optional result = controller.getExitDialog().showAndWait();

            if (result.isPresent() && result.get() == controller.getExitOption()) {
                System.exit(0);
            } else if (result.isPresent() && result.get() == controller.getCancelOption()) {
                controller.getExitDialog().hide();
                event.consume();
            }
        });
    }

    private void loadScenes() throws IOException {
        loadedScenes = (HashMap<String, SceneContent>) SceneLoader.loadAllFromDirectory(Main.getResourcePath("fxml/"));
        // Resetting fonts
        for (SceneContent sceneContent : loadedScenes.values()) {
            JFXNodes.forAllChildren(sceneContent.root, node -> {
                if (node instanceof Labeled) {
                    Labeled labeled = (Labeled) node;
                    Font newFont = new Font(APP_FONT_NAME, labeled.getFont().getSize());
                    // Restoring font styles using CSS
                    if (labeled.getFont().getStyle().contains("Bold"))
                        labeled.setStyle(labeled.getStyle() + "\n" + "-fx-font-weight: bold;");
                    if (labeled.getFont().getStyle().contains("Italic"))
                        labeled.setStyle(labeled.getStyle() + "\n" + "-fx-font-style: italic;");
                    labeled.setFont(newFont);
                }
            });
        }
    }
}
