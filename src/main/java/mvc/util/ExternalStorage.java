package mvc.util;

import levels.Level;
import mvc.controllers.GamePlayController;

/**
 * External storage for values that will be used in {@link GamePlayController}. This values can unstably work in
 * {@link GamePlayController controller}.
 *
 * <p>Class uses Singleton pattern to match OOP dependencies.</p>
 *
 * @see #currentLevel
 * @see #instance
 */
public class ExternalStorage {
    /** Instance of class. */
    private static ExternalStorage instance;

    /** Object for current level.
      * @see levels.XMLLevelLoader#loadLevelPack(String) */
    public volatile Level currentLevel;

    /**
     * Returns instance of class. If instance is {@code null}, it will be set to <code>new ExternalStorage();</code>.
     *
     * @return instance of class.
     */
    public static ExternalStorage getInstance() {
        return (instance != null) ? instance : (instance = new ExternalStorage());
    }

    private ExternalStorage() {

    }
}
