/**
 * FileNotFoundError is thrown by various Fiz methods when a requested
 * file doesn't exist or can't be opened, and the situation is such that
 * the caller unlikely to handle the problem: the most likely outcome is
 * that we want to abort the request being processed.  Using an Error for
 * this means that the ancestor methods don't need to declare an exception.
 */

package org.fiz;
public class FileNotFoundError extends Error {
    /**
     * Constructor for FileNotFoundError objects.
     * @param fileName             Name of the file that couldn't be opened.
     * @param type                 Type of the file such as "dataset";
     *                             will appear in the error message.  Null
     *                             means type isn't known.
     * @param message              Information about the problem; typically
     *                             the getMessage() value from an exception.
     */
    public FileNotFoundError(String fileName, String type, String message) {
        super("couldn't open " + ((type != null) ? (type + " ") : "")
                + "file \"" + fileName + "\": "
                + Util.extractInnerMessage(message));
    }

    /**
     * Constructor for FileNotFoundError objects.  This constructor is
     * intended primarily for internal use by newPathInstance.
     * @param message              Information about the problem.
     */
    public FileNotFoundError(String message) {
        super(message);
    }

    /**
     * This method creates an FileNotFoundError object in a path lookup
     * has failed; it includes the path in the error message.
     * @param fileName             Name of the file that couldn't be opened
     * @param type                 Type of the file such as "dataset";
     *                             will appear in the error message.  Null
     *                             means type isn't known.
     * @param path                 Directories that were searched for the file..
     * @return                     An Error object ready to throw.
     */
    public static FileNotFoundError newPathInstance(String fileName,
            String type, String[] path) {
        return new FileNotFoundError("couldn't find "
                + ((type != null) ? (type + " ") : "")
                + "file \"" + fileName + "\" in path (\""
                + Util.join(path, "\", \"") + "\")");
    }
}