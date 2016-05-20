package mvc.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import mvc.help.FXController;
import start.Main;
import util.collections.MapUtils;
import util.javafx.ColorUtils;

import java.util.ArrayList;
import java.util.List;
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
    @FXML
    private ColorPicker gpColorPicker;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        setupLanguageSelector();
        setupGPColorPicker();
    }

    private void setupLanguageSelector() {
        Language currentLang = null;
        ArrayList<Language> outputLocales = new ArrayList<>();

        for (String tag : Main.getAvailableLocales()) {
            Language language = new Language(tag);
            if (tag.equals(Main.getAppSettings().getSetting("lang")))
                currentLang = language;
        }

        langSelector.setItems(FXCollections.observableArrayList(outputLocales));
        langSelector.getSelectionModel().select(currentLang);
        langSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Main.getAppSettings().setSetting("lang", newValue.tag);
        });
    }

    private void setupGPColorPicker() {
        gpColorPicker.getCustomColors().addAll(Main.getCustomColorPickerColors());
        gpColorPicker.setValue(Color.web(Main.getAppSettings().getSettingOrElse("gp-color", "#439D1C")));
        gpColorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            Main.getAppSettings().setSetting("gp-color", ColorUtils.toWebString(newValue));
        });
        gpColorPicker.getCustomColors().addListener((ListChangeListener<Color>) change -> {
            List<Color> customColors = Main.getCustomColorPickerColors();
            if (change.wasAdded())
                customColors.addAll(change.getAddedSubList());
            else if (change.wasRemoved())
                customColors.addAll(change.getRemoved());

            List<String> hexStringColors = customColors.stream()
                    .map(ColorUtils::toWebString)
                    .collect(Collectors.toList());
            Main.getAppSettings().setSetting("custom-color-picker-color", String.join(", ", hexStringColors));
        });
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
