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
        for (String fileName : file.list()) {
            File currentFile = new File(location + fileName);

            // If current file-object is real file, and ends with ".fxml"...
            if (currentFile.isFile() && fileName.endsWith(".fxml")) {
                // It is "scene storage" and we must add its data
                FXMLLoader currentLoader = new FXMLLoader(currentFile.toURI().toURL());
                currentLoader.setResources(Main.resourceBundle);
                currentLoader.load();
                loadedScenes.put(fileName, new SceneContent(currentLoader));
            }
        }

        return loadedScenes;
    }
}
