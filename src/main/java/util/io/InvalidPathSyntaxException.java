package util.io;

/**
 * {@link RuntimeException}-Wrapper for {@link java.net.URISyntaxException} (again this boring exception-checking).
 */
public class InvalidPathSyntaxException extends RuntimeException {
    public InvalidPathSyntaxException(String path) {
        super(path);
    }
}
