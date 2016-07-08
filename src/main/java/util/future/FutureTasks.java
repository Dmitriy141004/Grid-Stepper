package util.future;

import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Utility-class with functions for future tasks.
 */
public class FutureTasks {
    public static void runLaterWithPermissions(Runnable task) {
        runLaterWithPermissions(task, 0);
    }

    public static void runLaterWithPermissions(Runnable task, long delay) {
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                Platform.runLater(task);
            }
        }, delay);
    }
}
