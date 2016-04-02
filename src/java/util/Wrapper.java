package util;

/**
 * This class is simple wrapper for object stored inside. Created to make such things in anonymous classes and lambdas:
 *
 * <pre><code>
 * Wrapper&lt;String&gt; specialString = new Wrapper&lt;&gt;();
 *
 * availableStrings.stream()
 *         .forEach(str -&gt; {
 *             if (SpecialStringValidator.isSpecial(str)) specialString.set(str);
 *         });
 *
 * System.out.println(specialString);
 * </code></pre>
 *
 * Without such thing you can't change variables declared in functions and methods.
 *
 * @param <T> type of stored value.
 *
 */
public class Wrapper<T> {
    /** Inner storage for value of wrapper. */
    private T value;

    /**
     * Creates new wrapper.
     *
     * @param value initial value of {@link #value storage}
     */
    public Wrapper(T value) {
        this.value = value;
    }

    /**
     * Sets new value to {@link #value storage}.
     *
     * @param value new value to set.
     */
    public void set(T value) {
        this.value = value;
    }

    /**
     * Getter for {@link #value storage}.
     *
     * @return current value of {@link #value storage}.
     */
    public T get() {
        return value;
    }
}
