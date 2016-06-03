package util.io;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility-Class for doing some things paths.
 */
public class PathsUtil {
    /**
     * Splits path fragments. For example, path (randomly selected, needed big nesting)
     * <pre><code>/usr/lib/jvm/java-8-oracle/jre/lib/ext/</code></pre>
     * will be split to list
     * <pre><code>["/", "usr/", "lib/", "jvm/", "java-8-oracle/", "jre/", "lib/", "ext/"]</code></pre>
     *
     * And Windows-Style path
     * <pre><code>C:/gcc/gcc-4.8.0/libjava/javax/xml/parsers</code></pre>
     * will be split to list
     * <pre><code>["C:/", "gcc/", "gcc-4.8.0/", "libjava/", "javax/", "xml/", "parsers"]</code></pre>
     *
     * Function also supports file names and you don't have to add slash at the end of string to say that last element
     * is directory.
     *
     * @param path path to process
     * @return list of path parts.
     */
    private static List<String> splitPath(String path) {
        List<String> out = new ArrayList<>(0);

        StringBuilder buffer = new StringBuilder(0);
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            buffer.append(c);

            if (c == '/' || i + 1 == path.length()) {
                out.add(buffer.toString());
                buffer = new StringBuilder(0);
            }
        }

        return out;
    }

    /**
     * Makes real path from relative. To read more description - see {@link PathsUtil documentation for class}.
     *
     * @param relative relative path to convert. But,
     *                 <span color="#A52500"><b>YOU HAVE TO USE SLASHES AS FILE SEPARATORS!</b></span>
     * @return real path from relative.
     */
    public static String realPath(String relative) {
        try {
            ArrayList<String> relativePathParts = (ArrayList<String>) splitPath(relative);

            // Caller class is on third stack trace element, on second is this function, and on first -
            // "Thread#getStackTrace()".
            Class<?> callerClass = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());

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
            if (classPackedInJar)
                callerClassLocation.append('/');

            String transformedPackagePath = callerClass.getPackage().getName().replaceAll("\\.", "/");
            callerClassLocation.append(transformedPackagePath).append('/');

            ArrayList<String> callerClasspathParts = (ArrayList<String>) splitPath(callerClassLocation.toString());

            ArrayList<String> out = new ArrayList<>(0);
            for (int i = 0; i < relativePathParts.size(); i++) {
                String part = relativePathParts.get(i);

                if ((part.equals("./") || part.equals(".")) && i > 0)
                    continue;

                if ((part.equals("./") || part.equals(".")) && i == 0)
                    out.addAll(callerClasspathParts);
                else if (part.equals("../") || part.equals("..")) {
                    if (out.size() == 0)
                        out.addAll(callerClasspathParts);
                    out.remove(out.size() - 1);
                } else
                    out.add(part);
            }

            StringBuilder madeRealPath = new StringBuilder(0);
            out.forEach(madeRealPath::append);
            return madeRealPath.toString();
        } catch (ClassNotFoundException e) {
            // Isn't reachable in this case
            throw new RuntimeException(e);
        }
    }
}
