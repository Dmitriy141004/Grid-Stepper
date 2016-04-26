package mvc.controllers;

import mvc.util.FXController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import settings.XMLSettingsLoader;
import start.Main;
import util.Wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Controller for settings menu.
 *
 */
public class SettingsMenuController extends FXController {
    /** Link to "language selector" combo-box. */
    @FXML
    private ComboBox<Language> langSelector;

    /** Convert table for language tags, where:
     * <ul>
     *     <li><b>Key</b> - language tag,</li>
     *     <li><b>Value</b> - full language name.</li>
     * </ul> */
    private static final HashMap<String, String> LANG_CONVERT_TABLE = mapFrom2DArray(new String[][] {
            {"en", "English"},
            {"ru", "\u0420\u0443\u0441\u0441\u043a\u0438\u0439"},
            {"ua", "\u0423\u043a\u0440\u0430\u0457\u043d\u0441\u044c\u043a\u0430"},
            {"de", "Deutsch"},
            {"fr", "Fran\u00e7ais"}
    });

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        // Creating list of languages
        Wrapper<Language> currentLang = new Wrapper<>(null);          // Current language to select in combo-box

        // Converting locale tags ("ru", "en", "ua", "uk", "de", "fr", etc) to ArrayList with Language objects
        ArrayList<Language> outputLocales = Main.getAvailableLocales().stream()
                // Mapping stream to collection of Language objects...
                .map(tag -> {
                    Language lang = new Language(tag);
                    // And searching current language between them
                    if (tag.equals(XMLSettingsLoader.getSetting("lang"))) currentLang.set(lang);
                    return lang;
                })
                .collect(Collectors.toCollection(ArrayList<Language>::new));

        // Setting items of language combo-box
        langSelector.setItems(FXCollections.observableArrayList(outputLocales));
        langSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) XMLSettingsLoader.setSetting("lang", newValue.tag);
        });
        langSelector.getSelectionModel().select(currentLang.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {

    }

    /**
     * Event handler for all menu buttons.
     *
     * @param event event from button.
     */
    public void actionButtonPressed(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        switch (clickedButton.getId()) {
            case "backButton":
                Main.changeScene("main.fxml");
                break;
        }
    }

    /**
     * Class for storing tag and full name of language.
     *
     */
    private class Language {
        /** Full name of language. */
        String fullName;
        /** Language tag. */
        String tag;

        /**
         * Creates new instance of class.
         *
         * @param tag received tag of language. Using {@link #LANG_CONVERT_TABLE} and this argument constructor generates
         *            value for {@link #fullName}.
         */
        Language(String tag) {
            this.fullName = LANG_CONVERT_TABLE.get(tag);
            this.tag = tag;
        }

        /**
         *
         * @return string with this format: {@code fullName (tag)}.
         */
        @Override
        public String toString() {
            return String.format("%s (%s)", LANG_CONVERT_TABLE.get(tag), tag);
        }
    }

    /**
     * Makes {@link HashMap} from 2D array.
     *
     * @param array source 2D array, where each "column" is pair, and each pair is 2-item array.
     * @param <K> type of keys of new {@link HashMap}.
     * @param <V> type of values of new {@link HashMap}
     * @return constructed {@link HashMap}.
     * @throws RuntimeException if sub array doesn't contain enough elements to make pair ({@code subArray.length != 2}).
     */
    @SuppressWarnings("unchecked")
    private static <K, V> HashMap<K, V> mapFrom2DArray(Object[][] array) {
        HashMap<K, V> out = new HashMap<>(array.length);

        for (Object[] subArray : array) {
            if (subArray.length != 2) throw new RuntimeException("Sub array " + Arrays.toString(subArray) + " doesn\'t " +
                    "contain enough elements to make key-value pair.");

            out.put((K) subArray[0], (V) subArray[1]);
        }

        return out;
    }
}
