package bin.levels;

/**
 * This exception is wrapper of {@link java.net.URISyntaxException}, but this exception is child of {@link RuntimeException}.
 * So, in some of my file loader you can often see this:
 *
 * <pre><code>
 * try {
 *     return loadFile((new File(srcUrl.toURI())).getAbsolutePath());
 * } catch (URISyntaxException e) {
 *     throw new InvalidPathSyntaxException(path.toString());
 * }
 * </code></pre>
 *
 * That's because I was bored by this non-runtime exception.
 *
 * @author Dmitriy Meleshko
 * @since v. 1.0
 *
 */
public class InvalidPathSyntaxException extends RuntimeException {
    public InvalidPathSyntaxException(String path) {
        super(path);
    }
}
