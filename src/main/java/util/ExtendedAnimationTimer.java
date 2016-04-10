package util;

import com.sun.javafx.tk.Toolkit;
import com.sun.scenario.animation.AbstractMasterTimer;
import com.sun.scenario.animation.shared.TimerReceiver;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <i>&#x00AB;My own implementation&#x00BB;</i> of {@link javafx.animation.AnimationTimer} (well, you can see copy-paste).
 * Where is difference? Common {@link javafx.animation.AnimationTimer} doesn't allow you to do action after it stops
 * (not in method {@link AnimationTimer#stop()}). For example, I couldn't show new windows if level were completed. That's
 * why I made <i>&#x00AB;my own implementation&#x00BB;</i>
 *
 * <i><b>Note:</b> you'll see many javadoc from {@link AnimationTimer}!</i>
 *
 */
public abstract class ExtendedAnimationTimer {
    private class AnimationTimerReceiver implements TimerReceiver {
        /**
         * {@inheritDoc}
         */
        public void handle(final long now) {
            if (accessControlContext == null) {
                throw new IllegalStateException("Error: AccessControlContext not captured");
            }

            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                try {
                    ExtendedAnimationTimer.this.handle(now);
                } catch (RuntimeException e) {
                    // If an exception was thrown in #handle(long) method - stopping timer
                    stop();
                    throw e;
                }
                return null;
            }, accessControlContext);
        }
    }

    private final AbstractMasterTimer timer;
    private final AnimationTimerReceiver timerReceiver = new AnimationTimerReceiver();
    private boolean active;
    private Runnable endAction;

    /** Access control context, captured in {@link #start()} */
    private AccessControlContext accessControlContext = null;

    public void setEndAction(Runnable endAction) {
        this.endAction = endAction;
    }

    /**
     * Creates a new timer.
     *
     */
    public ExtendedAnimationTimer() {
        timer = Toolkit.getToolkit().getMasterTimer();
    }

    /**
     * This method needs to be overridden by extending classes. It is going to be called in every frame while the
     * {@code AnimationTimer} is active.
     *
     * @param now The timestamp of the current frame given in nanoseconds. This
     *            value will be the same for all {@code AnimationTimers} called
     *            during one frame.
     */
    public abstract void handle(long now);

    /**
     * Starts timer. Once it is started, the {@link #handle(long)} method of this timer will be called every frame.
     * Timer can be stopped by calling {@link #stop()}.
     */
    public void start() {
        if (!active) {
            // Capture the Access Control Context to be used during the animation pulse
            accessControlContext = AccessController.getContext();
            timer.addAnimationTimer(timerReceiver);
            active = true;
        }
    }

    /**
     * Stops timer. It can be activated again by calling {@link #start()}.
     */
    public void stop() {
        if (active) {
            timer.removeAnimationTimer(timerReceiver);
            active = false;

            // If endAction isn't null, we must run it
            if (endAction != null) {
                Timer timer = new Timer();

                timer.schedule(new TimerTask() {
                    public void run() {
                        Platform.runLater(endAction);
                    }
                }, 0);
            }
        }
    }
}
