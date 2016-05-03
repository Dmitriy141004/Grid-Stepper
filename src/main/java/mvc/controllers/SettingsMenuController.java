package mvc.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import mvc.util.FXController;
import start.Main;
import util.collections.MapUtils;
import util.objects.Wrapper;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for settings menu.
 */
public class SettingsMenuController extends FXController {
    /**
     * Convert table for language tags, where:
     * <ul>
     * <li><b>Key</b> - language tag,</li>
     * <li><b>Value</b> - full language name.</li>
     * </ul>
     */
    private static final Map<String, String> LANG_CONVERT_TABLE = MapUtils.mapFrom2DArray(new String[][] {
            {"en", "English"},
            {"ru", "\u0420\u0443\u0441\u0441\u043a\u0438\u0439"},
            {"ua", "\u0423\u043a\u0440\u0430\u0457\u043d\u0441\u044c\u043a\u0430"},
            {"de", "Deutsch"},
            {"fr", "Fran\u00e7ais"}
    });
    @FXML
    private ComboBox<Language> langSelector;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        Wrapper<Language> currentLang = new Wrapper<>(null);

        ArrayList<Language> outputLocales = Main.getAvailableLocales().stream()
                // Mapping stream to collection of Language objects...
                .map(tag -> {
                    Language lang = new Language(tag);
                    // ...And searching current language between them
                    if (tag.equals(Main.getAppSettings().getSetting("lang")))
                        currentLang.set(lang);
                    return lang;
                })
                .collect(Collectors.toCollection(ArrayList<Language>::new));

        langSelector.setItems(FXCollections.observableArrayList(outputLocales));
        langSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
                Main.getAppSettings().setSetting("lang", newValue.tag);
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

    public void actionButtonPressed(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        switch (clickedButton.getId()) {
            case "backButton":
                Main.changeScene("main.fxml");
                break;
        }
    }

    private class Language {
        private String fullName;
        private String tag;

        Language(String tag) {
            this.fullName = LANG_CONVERT_TABLE.get(tag);
            this.tag = tag;
        }

        @Override
        public String toString() {
            return String.format("%s (%s)", fullName, tag);
        }
    }
}
