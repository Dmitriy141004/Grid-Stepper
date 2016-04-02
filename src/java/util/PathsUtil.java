package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Utility-Class for doing some things paths.
 *
 * <center><h2 style="font-family: 'DejaVu Sans'">Available tasks</h2></center>
 * <table style="border-collapse: collapse; color: black">
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
     * Converts varargs or array to {@link Collection}.
     *
     * @param array varargs or array of items
     * @param <C> collection type
     * @param <T> type of items in collection
     * @return converted array to collection.
     */
    @SafeVarargs
    @SuppressWarnings({"varargs", "unchecked"})
    private static <C extends Collection, T> C asCollection(T... array) {
        ArrayList<T> out = new ArrayList<>(array.length);
        Collections.addAll(out, array);
        return (C) out;
    }

    /**
     * Makes real path from relative. To read more description - see {@link PathsUtil documentation for class}.
     *
     * @param relative relative path to convert
     * @return real path from relative.
     */
    public static String realPath(String relative) {
        try {
            // Replacing used FILE separators ('\', '//' etc, but not '/') to '/'
            relative = relative.replaceAll("(/{2}|\\\\)", "/");

            // Parts of relative path
            ArrayList<String> relativePathParts = asCollection(splitPath(relative));

            // Getting caller class location and its parts (caller class is on third stack trace element, on second is
            // this function, and on first - "Thread#getStackTrace()").
            Class<?> callerClass = Class.forName(Thread.currentThread().getStackTrace()[3].getClassName());
            // Is class in JAR?
            boolean classPackedInJar = callerClass.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getFile()
                    // Pattern passes, for example, "package.jar", but won't pass ".jar"
                    .matches("(.+)\\.jar");
            StringBuilder callerClassLocation = new StringBuilder(callerClass.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath()
            );
            if (classPackedInJar) callerClassLocation.append('/');
            callerClassLocation.append(callerClass.getPackage().getName().replaceAll("\\.", "/")).append('/');

            ArrayList<String> callerClasspathParts = asCollection(splitPath(callerClassLocation.toString()));

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
        } catch (ClassNotFoundException e) {
            // Isn't reachable in this case
            throw new RuntimeException(e);
        }
    }
}
