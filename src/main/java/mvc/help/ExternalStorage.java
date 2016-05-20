package mvc.help;

import levels.Level;
import levels.LevelPack;
import mvc.controllers.GamePlayController;

/**
 * This class is storage for really global values, and values that unstably (I don't know why) work in
 * {@link GamePlayController}.
 *
 * <p>Class uses Singleton pattern to match OOP dependencies.</p>
 *
 * @see #currentLevel
 * @see #instance
 */
public class ExternalStorage {
    private static ExternalStorage instance;

    /** @see Level
     * @see levels.XMLLevelLoader#loadLevelPack(String) */
    public volatile Level currentLevel;
    /** @see LevelPack
     * @see levels.XMLLevelLoader#loadLevelPack(String) */
    public volatile LevelPack selectedCampaign;

    private ExternalStorage() {

    }

    public static ExternalStorage getInstance() {
        if (instance == null)
            instance = new ExternalStorage();
        return instance;
    }
}
