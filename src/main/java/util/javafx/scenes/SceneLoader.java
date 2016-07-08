package util.javafx.scenes;

import javafx.fxml.FXMLLoader;
import start.Main;
import util.io.InvalidPathSyntaxException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for productive loading scenes. What I mean using word <b>&#x00AB;productive&#x00BB;</b>? You must load scene
 * only one time, without such constructions:
 *
 * <pre><code>
 * Parent root = FXMLLoader.load(new ByteArrayInputStream("path/to/fxml/file").getBytes()));
 * </code></pre>
 */
public class SceneLoader {
    /**
     * Loads scene from single file.
     *
     * @param location location of {@code .fxml} file
     * @return loaded {@link SceneContent}.
     */
    private static SceneContent loadScene(String location) {
        try {
            SceneContent loadedScene;

            File fxmlFile = new File(location);

            if (!fxmlFile.exists())
                throw new IllegalArgumentException("Invalid value of argument " +
                        "\"location\": file \"" + location + "\" doesn\'t exist!");
            if (!fxmlFile.isFile())
                throw new IllegalArgumentException("Invalid value of argument " +
                        "\"location\": file \"" + location + "\" isn\'t file!");
            if (!location.endsWith(".fxml"))
                throw new IllegalArgumentException("Invalid value of argument " +
                        "\"location\": file \"" + location + "\" isn\'t an \".fxml\" file!");

            FXMLLoader currentLoader;
            try {
                currentLoader = new FXMLLoader(fxmlFile.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new InvalidPathSyntaxException(fxmlFile.getAbsolutePath());
            }
            currentLoader.setResources(Main.resourceBundle);
            currentLoader.load();
            loadedScene = new SceneContent(currentLoader);

            return loadedScene;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads all {@code .fxml} files in specified directory.
     *
     * @param location location of directory with files
     * @return map, where key is name of file and value is {@link SceneContent} object.
     * @throws IllegalArgumentException if location doesn't end with current file separator (UNIX (Mac OS and Linux - "/",
     *                                  Windows - "//").
     */
    public static Map<String, SceneContent> loadAllFromDirectory(String location) {
        if (!location.endsWith(File.separator))
            throw new IllegalArgumentException("Invalid value of argument " +
                    "\"location\": path doesn\'t end with file separator (\"" + File.separator + "\")!");

        Map<String, SceneContent> loadedScenes = new HashMap<>();
        File file = new File(location);

        for (String fileName : file.list())
            loadedScenes.put(fileName, loadScene(location + fileName));

        return loadedScenes;
    }
}
