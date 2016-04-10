package util;

import javafx.fxml.FXMLLoader;
import start.Main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for productive loading scenes. What I mean using word <b>&#x00AB;productive&#x00BB;</b>? You must load scene
 * only one time, without such constructions:
 *
 * <pre><code>
 * Parent root = FXMLLoader.load(
 * new ByteArrayInputStream("path/to/fxml/file").getBytes()));
 * </code></pre>
 *
 */
public class SceneLoader {
    /**
     * Loads scene from single file.
     *
     * @param location location of {@code .fxml} file
     * @return loaded {@link SceneContent}.
     * @throws IOException if loading fails.
     */
    private static SceneContent loadScene(String location) throws IOException {
        // Output
        SceneContent loadedScene = null;

        // File-object
        File fxmlFile = new File(location);

        if (!fxmlFile.exists()) throw new IllegalArgumentException("Invalid value of argument " +
                "\"location\": file \"" + location + "\" doesn\'t exist!");
        if (!fxmlFile.isFile()) throw new IllegalArgumentException("Invalid value of argument " +
                "\"location\": file \"" + location + "\" isn\' file!");

        // If file-object is real file, and ends with ".fxml"...
        if (fxmlFile.isFile() && location.endsWith(".fxml")) {
            // It is "scene storage" and we must add its data
            FXMLLoader currentLoader = new FXMLLoader(fxmlFile.toURI().toURL());
            currentLoader.setResources(Main.resourceBundle);
            currentLoader.load();
            loadedScene = new SceneContent(currentLoader);
        }

        return loadedScene;
    }

    /**
     * Loads all {@code .fxml} files in specified directory.
     *
     * @param location location of directory with files
     * @return map, where key is name of file and value is {@link SceneContent} object.
     * @throws IOException if loading fails.
     * @throws IllegalArgumentException if location doesn't end with current file separator (UNIX (Mac OS and Linux - "/",
     *                                  Windows - "//").
     */
    public static Map<String, SceneContent> loadPackage(String location) throws IOException {
        if (!location.endsWith(File.separator)) throw new IllegalArgumentException("Invalid value of argument " +
                "\"location\": path doesn\'t end with file separator (\"" + File.separator + "\")!");

        // Output map
        Map<String, SceneContent> loadedScenes = new HashMap<>();

        // Directory file-object
        File file = new File(location);

        // Iterating all files in directory
        for (String fileName : file.list()) loadedScenes.put(fileName, loadScene(location + fileName));

        return loadedScenes;
    }
}
