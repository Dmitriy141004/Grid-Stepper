package bin.util;

/**
 * Interface for <b>L</b>irst-<b>i</b>n-<b>F</b>irst-<b>O</b>ut queues.
 *
 * @param <E> type of queue's items.
 *
 * @author Dmitriy Meleshko
 * @since v. 1.0
 */
public interface LIFOQueue<E> {
    /**
     * Adds object in the end.
     *
     * @param obj object to add
     */
    void push(E obj);

    /**
     * Removes last object from the end.
     *
     * @return last removed object.
     */
    E pop();

    /**
     * Clears all items of queue. Used to make code
     * <pre><code>
     * for ( ; stack.length() &gt; 0; stack.pop());
     * </code></pre>
     * more compact. Now, it will be
     * <pre><code>
     * stack.clear();
     * </code></pre>
     *
     */
    void clear();

    /**
     * Used for getting size of stack.
     *
     * @return size of stack.
     */
    int length();

    /**
     * Works like {@link #pop()}, but this method returns last element without removing it.
     *
     * @return last element.
     */
    E last();
}
