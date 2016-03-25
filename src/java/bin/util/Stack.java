package bin.util;

import java.util.ArrayList;

/**
 * Implements interface {@link LIFOQueue}. It's very simple stack.
 *
 * @param <E> type of stack's items.
 *
 * @author Dmitriy Meleshko
 * @since v. 1.0
 */
public class Stack<E> implements LIFOQueue<E> {
    /**
     * Inner data of stack. It's {@link ArrayList} because of strange Java's error - you can't do this in classes:
     * <pre><code>
     * E[] stackData = new E[];
     * </code></pre>
     * Where {@code E} is generic type.
     *
     */
    private ArrayList<E> stackData = new ArrayList<>(0);

    /**
     * Standard constructor for class.
     *
     * @param source stack to copy. <i><b>Note:</b> clones another stack's {@link #stackData}, not sets link!</i>
     */
    @SuppressWarnings("unchecked")
    public Stack(Stack<E> source) {
        if (source != null) stackData = (ArrayList<E>) source.stackData.clone();
    }

    /**
     * This constructor is used when there's no source stack. So, in {@link #Stack(Stack) first constructor} you can see
     * {@code if-not-null-then-do} expression.
     *
     */
    public Stack() {
        this(null);
    }

    /**
     * Implements {@link LIFOQueue#push(Object)} method from superclass}. This method is synchronized because JavaFX
     * creates many threads, and I don't need exceptions caused by stacks.
     *
     * @param obj object to add
     */
    @Override
    public synchronized void push(E obj) {
        stackData.add(obj);
    }

    /**
     * Implements {@link LIFOQueue#pop() method from superclass}. This method is synchronized because JavaFX
     * creates many threads, and I don't need exceptions caused by stacks.
     *
     * <p><i><b>Note:</b> if stack is empty - returns null, not exception.</i></p>
     *
     * @return last removed object.
     */
    @Override
    public synchronized E pop() {
        if (stackData.isEmpty()) return null;

        E result = stackData.get(stackData.size() - 1);
        stackData.remove(stackData.size() - 1);
        return result;
    }

    /**
     * Clears {@link #stackData}.
     *
     */
    @Override
    public synchronized void clear() {
        stackData.clear();
    }

    /**
     *
     * @return length of {@link #stackData}
     */
    @Override
    public int length() {
        return stackData.size();
    }

    @Override
    public String toString() {
        return stackData.toString();
    }

    /**
     * Just overrides {@link LIFOQueue#last()}.
     *
     * @return last element.
     */
    @Override
    public E last() {
        if (stackData.isEmpty()) return null;

        return stackData.get(stackData.size() - 1);
    }
}
