package org.fiz;
import java.io.*;
import java.lang.reflect.*;
import javax.servlet.http.*;

/**
 * The Util class defines miscellaneous methods that are generally useful
 * and don't fit anywhere else.
 */

public final class Util {
    // No constructor: this class only has a static methods.
    private Util() {}

    /**
     * Copy all of the data from one stream to another, stopping when the
     * end of the input stream is reached.
     * @param in                      Read from this stream.
     * @param out                     Write to this stream
     * @throws IOException            Thrown if errors occur while copying
     *                                the data.
     */
    public static void copyStream(Reader in, Writer out) throws IOException {
        while (true) {
            int next = in.read();
            if (next == -1) {
                break;
            }
            out.write(next);
        }
    }

    /**
     * Deletes a given file/directory and, in the case of a directory,
     * all of its descendents.
     * @param name                    Name of the file or directory to delete.
     * @return                        Returns true if the deletion was
     *                                completed successfully, false otherwise.
     */
    public static boolean deleteTree(String name) {
        File file = new File(name);
        if (file.isDirectory()) {
            for (String child : file.list()) {
                deleteTree(name + "/" + child);
            }
        }
        return file.delete();
    }

    /**
     * Find the class corresponding to particular name; if the name
     * doesn't exist and doesn't contain any dot separators, also search
     * in various packages defined by the {@code searchPackages} value
     * in the {@code main} configuration dataset.
     * @param className            Name of the desired class.
     * @return                     The Class object corresponding to
     *                             {@code className}, or null if no such class
     *                             could be found.
     */
    public static Class findClass(String className) {
        Class<?> cl = null;
        try {
            cl = Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            // Just keep going if the class could be found.
        }
        if ((cl == null) && (className.indexOf('.') < 0)) {
            // Couldn't find the given name; try prepending various package
            // names provided by configuration information.
            Dataset config = Config.getDataset("main");
            String path = config.check("searchPackages");
            if (path != null) {
                for (String packageName : StringUtil.split(path, ',')) {
                    try {
                        cl = Class.forName(packageName + "."  + className);
                        break;
                    }
                    catch (ClassNotFoundException e) {
                    }
                }
            }
        }
        return cl;
    }

    /**
     * Given a file name without an extension, this checks to see if there
     * exists a file with this base name and one of several possible
     * extensions.
     * @param base                    The base file name, which should not
     *                                already have an extension.
     * @param extensions              Extensions to try (must contain ".")
     *                                in order.
     * @return                        The name (base plus extension) of the
     *                                first file that exists.  If none of
     *                                extensions exist null is returned.
     */
    public static String findFileWithExtension(String base,
            String... extensions) {
        int baseLength = base.length();
        StringBuilder fullName = new StringBuilder(baseLength + 6);
        fullName.append(base);
        for (String extension : extensions) {
            fullName.setLength(baseLength);
            fullName.append(extension);
            if ((new File(fullName.toString())).exists()) {
                return fullName.toString();
            }
        }
        return null;
    }

    /**
     * This method re-creates the full URL for an incoming request,
     * including the query string.
     * @param request                 Information about the request.
     * @return                        The URL for the request, including
     *                                query string (if there was one).
     */
    public static String getUrlWithQuery(HttpServletRequest request) {
        String url = request.getRequestURI();
        String query = request.getQueryString();
        if (query == null) {
            return url;
        }
        return url + "?" + query;
    }

    /**
     * Given the name of a class and various other information, load the
     * class and construct an instance of it.
     * @param className            Name of the desired class.  If this
     *                             name doesn't exist as a class, and it
     *                             contains no "." characters, then the
     *                             name will also be searched for in a
     *                             list of packages determined by configuration
     *                             information.
     * @param requiredType         If this is non-null, then the class must
     *                             be a subclass of this or implement this
     *                             interface.
     * @param constructorArgs      Arguments to pass to the constructor; the
     *                             class must contain a constructor compatible
     *                             with these arguments.
     * @return                     The return value is a new instance of
     *                             the class.
     */
    public static Object newInstance(String className, String requiredType,
            Object... constructorArgs) {

        // Look up the class.
        Class<?> cl = findClass(className);
        if (cl == null) {
            throw new ClassNotFoundError(className);
        }

        // Make sure that class has the right type.
        if (requiredType != null) {
            Class<?> desiredClass;
            try {
                desiredClass = Class.forName(requiredType);
            }
            catch (ClassNotFoundException e) {
                throw new ClassNotFoundError(requiredType);
            }
            if (!desiredClass.isAssignableFrom(cl)) {
                throw new InstantiationError(className,
                        "class isn't a subclass of " + requiredType);
            }
        }

        // Find a constructor with the right signature and create an instance.
        Constructor constructor;
        try {
            Class<?>[] argClasses = new Class<?>[constructorArgs.length];
            for (int i = 0; i < constructorArgs.length; i++) {
                argClasses[i] = constructorArgs [i].getClass();
            }
            constructor = cl.getConstructor(argClasses);
        }
        catch (Exception e) {
            throw new InstantiationError(className,
                    "couldn't find appropriate constructor ("
                    + e.getMessage() + ")");
        }
        try {
            return constructor.newInstance(constructorArgs);
        }
        catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new InstantiationError(className,
                    "exception in constructor: " +
                    StringUtil.lcFirst(cause.getMessage()));
        }
    }

    /**
     * Read a file and return its contents in a StringBuilder object.
     * @param fileName                Name of the file to read.
     * @return                        Contents of the file.
     * @throws FileNotFoundException  The file could not be opened.
     * @throws IOError                An error happened while reading the
     *                                file.
     */
    public static StringBuilder readFile(String fileName)
            throws FileNotFoundException {
        try {
            FileReader reader = new FileReader(fileName);
            StringBuilder result = new StringBuilder(
                    (int) (new File(fileName)).length());
            char[] buffer = new char[1000];
            while (true) {
                int length = reader.read(buffer, 0, buffer.length);
                if (length < 0) {
                    break;
                }
                result.append(buffer, 0, length);
            }
            reader.close();
            return result;
        }
        catch (FileNotFoundException e) {
            throw e;
        }
        catch (IOException e) {
            throw IOError.newFileInstance(fileName, e.getMessage());
        }
    }

    /**
     * Searches a collection of directories for a file and reads in the first
     * file found.
     * @param fileName                Name of the file to read.
     * @param type                    Type of the file, such as "dataset"
     *                                or "template"; used only to generate a
     *                                better error message if the file can't
     *                                be found.  Null means the file doesn't
     *                                have a meaningful type.
     * @param path                    One or more directories in which to
     *                                search for the file.
     * @return                        Contents of the first file found.
     * @throws FileNotFoundError      None of the directories in
     *                                {@code path} contained the file.
     */
    public static StringBuilder readFileFromPath(String fileName, String type,
            String... path) throws FileNotFoundError {
        for (int i = 0; i < path.length; i++) {
            try {
                return readFile(path[i] + "/" + fileName);
            }
            catch (FileNotFoundException e) {
                // No template in this directory; go on to the next.
                continue;
            }
        }
        throw FileNotFoundError.newPathInstance(fileName, type, path);
    }

    /**
     * Return the contents of a file as the response to an HTTP request.
     * @param input                   Identifies the file to return.
     * @param response                The file is delivered via this object.
     * @throws IOException            Thrown if an error occurs while
     *                                copying the file.
     */
    public static void respondWithFile(File input,
            HttpServletResponse response) throws IOException {
        response.setContentLength((int) input.length());
        FileReader in = new FileReader(input);
        copyStream(in, response.getWriter());
        in.close();
    }
}