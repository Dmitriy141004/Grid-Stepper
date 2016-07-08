package util.objects;

/**
 * This class is simple wrapper for object stored inside. Created to make such things in anonymous classes and lambdas:
 * <p>
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
 * <p>
 * Without such thing you can't change variables declared in functions and methods.
 *
 * @param <T> type of stored value.
 */
public class Wrapper<T> {
    private T value;
    public void set(T value) {
        this.value = value;
    }
    public T get() {
        return value;
    }

    public Wrapper(T value) {
        this.value = value;
    }
}
