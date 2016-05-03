package util.collections;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implements interface {@link LIFOQueue}. It's very simple stack.
 *
 * @param <E> type of stack's items.
 */
public class Stack<E> implements LIFOQueue<E> {
    /**
     * Inner data of stack. It's {@link ArrayList} because of strange Java's error - you can't do this in classes:
     * <pre><code>
     * E[] stackData = new E[];
     * </code></pre>
     * Where {@code E} is generic type.
     */
    private ArrayList<E> stackData = new ArrayList<>(0);

    /**
     * Creates new stack.
     *
     * @param source stack to copy. <i><b>Note:</b> clones another stack's {@link #stackData}, not sets link!</i>
     */
    @SuppressWarnings("unchecked")
    public Stack(Stack<E> source) {
        if (source != null)
            stackData = (ArrayList<E>) source.stackData.clone();
    }

    /**
     * This constructor is used when there's no source stack. So, in {@link #Stack(Stack) first constructor} you can see
     * {@code if-not-null-then-do} expression.
     */
    public Stack() {

    }

    /**
     * Creates new stack and adds all items from source collection
     *
     * @param source collection with source items.
     */
    public Stack(Collection<? extends E> source) {
        stackData.addAll(source);
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
     * <p>
     * <p><i><b>Note:</b> if stack is empty - returns null, not exception.</i></p>
     *
     * @return last removed object.
     */
    @Override
    public synchronized E pop() {
        if (stackData.isEmpty())
            return null;

        E result = stackData.get(stackData.size() - 1);
        stackData.remove(stackData.size() - 1);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void clear() {
        stackData.clear();
    }

    /**
     * {@inheritDoc}
     */
    public int length() {
        return stackData.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return stackData.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E last() {
        if (stackData.isEmpty())
            return null;

        return stackData.get(stackData.size() - 1);
    }
}
