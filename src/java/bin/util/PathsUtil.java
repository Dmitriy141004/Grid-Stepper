package bin.util;

import java.io.File;
import java.util.*;

/**
 * Utility-Class specified on paths. You can see available tasks here:
 *
 * <table style="border-collapse: collapse;">
 *     <tr style="background-color: #4D7A97; color: white; font-family: 'DejaVu Sans';">
 *         <th style="padding: 20px; border-left: 1px solid #BBB; border-top: 1px solid #BBB; border-bottom: 1px solid #BBB;">
 *             Link to function</th>
 *         <th style="padding: 20px; border-right: 1px solid #BBB; border-top: 1px solid #BBB; border-bottom: 1px solid #BBB;">
 *             Task description</th>
 *     </tr>
 *     <tr style="background-color: rgb(230,230,230);">
 *         <td style="padding: 20px; border: 1px solid #BBB;">{@link #realPath(String)}</td>
 *         <td style="padding: 20px; border: 1px solid #BBB;">Make real path from relative. Why I don't want to use
 *         {@link Class#getResource(String)}? If directory/file isn't nested in {@code classpath} - it returns {@code null},
 *         because {@link ClassLoader} can't "get resource". My method doesn't use {@link ClassLoader} to make real path
 *         (only for getting directory of caller class) - so it works pretty well even if directory/file isn't
 *         nested in {@code classpath}.</td>
 *     </tr>
 * </table>
 *
 */
public class PathsUtil {
    /**
     * Splits path fragments. For example, path (randomly selected, needed big nesting)
     * <pre><code>/usr/lib/jvm/java-8-oracle/jre/lib/ext/</code></pre>
     * will be split to array
     * <pre><code>["/", "usr/", "lib/", "jvm/", "java-8-oracle/", "jre/", "lib/", "ext/"]</code></pre>
     *
     * And Windows-Style path
     * <pre><code>C:/gcc/gcc-4.8.0/libjava/javax/xml/parsers</code></pre>
     * will be split to array
     * <pre><code>["C:/", "gcc/", "gcc-4.8.0/", "libjava/", "javax/", "xml/", "parsers"]</code></pre>
     *
     * Function also supports file names and you don't have to add slash at the end of string to say that last element
     * is directory.
     *
     * @param path path to process
     * @return array of path parts.
     */
    private static String[] splitPath(String path) {
        ArrayList<String> out = new ArrayList<>(0);

        // Iterating all chars
        StringBuilder buffer = new StringBuilder(0);
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            buffer.append(c);

            if (c == '/' || i + 1 == path.length()) {
                out.add(buffer.toString());
                buffer = new StringBuilder(0);
            }
        }

        return out.stream().toArray(String[]::new);
    }

    /**
     * Converts varargs or array to {@link Collection} (in fact - {@link ArrayList}).
     *
     * @param array varargs or array of items
     * @param <T> type of items in collection and array
     * @return converted array to collection.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    private static <T> Collection<T> asCollection(T... array) {
        ArrayList<T> out = new ArrayList<>(array.length);
        Collections.addAll(out, array);
        return out;
    }

    /**
     * Returns class that called some method. In fact, it returns first class, whose name doesn't equal current's name.
     *
     * @param current class where this method is being called (e.g. {@code Class.getClass()} or
     *                {@code &lt;ClassName&gt;.class})
     * @return caller class.
     */
    @SuppressWarnings("unchecked")
    private static Class<?> getCallerClass(Class<?> current) {
        try {
            // Getting and iterating stack trace
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            Class<?> caller = null;
            for (int i = 0; i < stackTrace.length; i++) {
                StackTraceElement next = stackTrace[i];

                // Skipping method java.lang.Thread#getStackTrace(), it always appeared in stack trace when I was testing
                if (i == 0 && next.getClassName().equals("java.lang.Thread") && next.getMethodName().equals("getStackTrace"))
                    continue;
                if (!next.getClassName().equals(current.getName())) {
                    caller = Class.forName(next.getClassName());
                    break;
                }
            }

            return caller;
        } catch (ClassNotFoundException e) {
            // Unreachable in this case
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes real path from relative. To read more description - see {@link PathsUtil documentation for class}.
     *
     * @param relative relative path to convert
     * @return real path from relative.
     */
    public static String realPath(String relative) {
        // Replacing used FILE separators ('\', '//' etc, but not '/') to '/'
        relative = relative.replaceAll("(/{2}|\\\\)", "/");

        // Parts of relative path
        ArrayList<String> relativePathParts = (ArrayList<String>) asCollection(splitPath(relative));

        // Getting caller class location and its parts (caller class is on second stack trace element, on first is
        // this function). If class is in JAR - caller class location will be like "build/jar/Application.jarapp/util/",
        // but it is proper location!
        Class<?> callerClass = getCallerClass(PathsUtil.class);
        String callerClassLocation = callerClass.getProtectionDomain().getCodeSource().getLocation().getPath() +
                callerClass.getPackage().getName().replaceAll("\\.", "/") + "/";
        ArrayList<String> callerClasspathParts = (ArrayList<String>) asCollection(splitPath(callerClassLocation));

        // "Counting" real path
        ArrayList<String> out = new ArrayList<>(0);
        for (int i = 0; i < relativePathParts.size(); i++) {
            String part = relativePathParts.get(i);

            if ((part.equals("./") || part.equals(".")) && i > 0) continue;

            if ((part.equals("./") || part.equals(".")) && i == 0) out.addAll(callerClasspathParts);
            else if (part.equals("../") || part.equals("..")) {
                if (out.size() == 0) out.addAll(callerClasspathParts);
                out.remove(out.size() - 1);
            } else out.add(part);
        }
        // Converting result from ArrayList<String> to string
        StringBuilder madeRealPath = new StringBuilder(0);
        out.forEach(madeRealPath::append);

        // Also replacing all slashes to current FILE separators
        return madeRealPath.toString().replaceAll("/", File.separator);
    }
}
