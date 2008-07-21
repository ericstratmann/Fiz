package org.fiz;
import java.io.*;

/**
 * Junit tests for the Util class.
 */

public class UtilTest extends junit.framework.TestCase {

    public void test_copyStream() throws IOException {
        StringReader in = new StringReader("01234567890abcdefg");
        StringWriter out = new StringWriter();
        in.read(); in.read();
        out.write('x');
        Util.copyStream (in, out);
        assertEquals("destination stream", "x234567890abcdefg",
                out.toString());
        out.getBuffer().setLength(0);
        out.write('q');
        Util.copyStream (in, out);
        assertEquals("input stream already at end-of-file", "q",
                out.toString());
    }

    public void test_deleteTree_success() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/first", "data for first file");
        TestUtil.writeFile("_test1_/child/second", "data for second file");
        assertEquals("successful return value", true,
                Util.deleteTree("_test1_"));
        assertEquals("directory is gone", false,
                (new File("_test1_")).exists());
    }
    public void test_deleteTree_failure() throws IOException {
        // This test only works on Windows, where a file cannot be
        // deleted if it is open.
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return;
        }
        TestUtil.writeFile("_test", "data");
        FileReader reader = new FileReader("_test");
        assertEquals("can't delete open file", false,
                Util.deleteTree("_test"));
        reader.close();
        assertEquals("can delete file once it's closed", true,
                Util.deleteTree("_test"));
    }

    public void test_findClass_classNameWorksImmediately() {
        Class result = Util.findClass("org.fiz.Dataset");
        assertEquals("name of result class", "org.fiz.Dataset",
                result.getName());
    }
    public void test_findClass_noSearchPackagesConfig() {
        Config.setDataset("main", new Dataset());
        Class result = Util.findClass("Dataset");
        assertEquals("null result", null, result);
    }
    public void test_findClass_classInPackage() {
        Config.setDataset("main", new Dataset("searchPackages",
                "foo.bar, org.fiz, bogus.moreBogus"));
        Class result = Util.findClass("Dataset");
        assertEquals("name of result class", "org.fiz.Dataset",
                result.getName());
    }
    public void test_findClass_notInSearchPackages() {
        Config.setDataset("main", new Dataset("searchPackages",
                "foo.bar, bogus.moreBogus"));
        Class result = Util.findClass("gorp");
        assertEquals("null result", null, result);
    }

     public void test_findFileWithExtension() {
        TestUtil.writeFile("_test_.abc", "abc");
        TestUtil.writeFile("_test_.x", "abc");
        assertEquals("found file", "_test_.x",
                Util.findFileWithExtension("_test_", ".def", ".x", ".abc"));
        assertEquals("couldn't find file", null,
                Util.findFileWithExtension("_test_", ".q", ".y"));
        TestUtil.deleteTree("_test_.abc");
        TestUtil.deleteTree("_test_.x");
    }

    public void test_getUriAndQuery() {
        ServletRequestFixture request = new ServletRequestFixture();
        request.uri = "/a/b";
        request.queryString = null;
        assertEquals("no query data", "/a/b",
                Util.getUrlWithQuery(request));
        request.queryString = "x=24&y=13";
        assertEquals("query data", "/a/b?x=24&y=13",
                Util.getUrlWithQuery(request));
    }

    public void test_newInstance_classNotFound() {
        Config.setDataset("main", new Dataset());
        boolean gotException = false;
        try {
            Util.newInstance("Dataset", null);
        }
        catch (ClassNotFoundError e) {
            assertEquals("exception message",
                    "couldn't find class \"Dataset\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_newInstance_requiredTypeClassNotFound() {
        boolean gotException = false;
        try {
            Util.newInstance("org.fiz.Dataset", "NonexistentType");
        }
        catch (ClassNotFoundError e) {
            assertEquals("exception message",
                    "couldn't find class \"NonexistentType\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_newInstance_notRequiredType() {
        boolean gotException = false;
        try {
            Util.newInstance("org.fiz.Dataset", "java.lang.String");
        }
        catch (InstantiationError e) {
            assertEquals("exception message",
                    "couldn't create an instance of class " +
                    "\"org.fiz.Dataset\": class isn't " +
                    "a subclass of java.lang.String",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_newInstance_cantFindMatchingConstructor() {
        boolean gotException = false;
        try {
            Util.newInstance("org.fiz.Dataset", null, new int[]{3,4});
        }
        catch (InstantiationError e) {
            assertEquals("exception message",
                    "couldn't create an instance of class " +
                    "\"org.fiz.Dataset\": couldn't " +
                    "find appropriate constructor " +
                    "(org.fiz.Dataset.<init>([I))",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_newInstance_argumentsForConstructor() {
        Object result = Util.newInstance("org.fiz.UtilTest1", null,
                new Dataset("xyz", "abc"), "value1");
        assertEquals ("class of result", "org.fiz.UtilTest1",
                result.getClass().getName());
        assertEquals ("first argument", "abc",
                ((UtilTest1) result).dataset.get("xyz"));
        assertEquals ("second argument", "value1",
                ((UtilTest1) result).string);
    }
    public void test_newInstance_exceptionInConstructor() {
        boolean gotException = false;
        try {
            Util.newInstance("org.fiz.UtilTest1", null, new Dataset(),
                    "error");
        }
        catch (InstantiationError e) {
            assertEquals("exception message",
                    "couldn't create an instance of class " +
                    "\"org.fiz.UtilTest1\": exception " +
                    "in constructor: test exception message",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_readFile_shortFile() throws FileNotFoundException {
        String contents = "Line 1\nLine 2\n";
        TestUtil.writeFile("test.foo", contents);
        StringBuilder s = Util.readFile("test.foo");
        assertEquals("file length", 14, s.length());
        assertEquals("file contents", contents, s.toString());
        TestUtil.deleteTree("test.foo");
    }
    public void test_readFile_longFile() throws FileNotFoundException {
        StringBuilder contents = new StringBuilder();
        for (int i = 0; i <1000; i++) {
            contents.append(String.format("This is line #%04d\n", i));
        }
        TestUtil.writeFile("test.foo", contents.toString());
        StringBuilder s = Util.readFile("test.foo");
        assertEquals("file length", 19000, s.length());
        assertEquals("check one line", "This is line #0010",
                s.substring(190, 208));
        assertEquals("full file contents", contents.toString(), s.toString());
        TestUtil.deleteTree("test.foo");
    }
    public void test_readFile_FileNotFoundException() {
        boolean gotException = false;
        try {
            Util.readFile("bogus/nonexistent");
        }
        catch (FileNotFoundException e) {
            assertEquals("exception message",
                    "bogus\\nonexistent (The system cannot find the "
                    + "path specified)", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_readFileFromPath() throws FileNotFoundError {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/foo.txt", "_test1_/foo.txt");
        TestUtil.writeFile("_test1_/child/foo.txt", "_test1_/child/foo.txt");
        StringBuilder contents = Util.readFileFromPath("foo.txt", "test",
                ".", "bogus/xyz", "_test1_", "_test1_/child");
        assertEquals("full file contents", "_test1_/foo.txt",
                contents.toString());
        TestUtil.deleteTree("_test1_");
    }
    public void test_readFileFromPath_notFound() {
        boolean gotException = false;
        try {
            Util.readFileFromPath("src", "test", ".", "bogus1/a",
                    "bogus2/x/y");
        }
        catch (FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't find test file \"src\" in path "
                    + "(\".\", \"bogus1/a\", \"bogus2/x/y\")",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_respondWithFile() throws IOException {
        TestUtil.writeFile("test.html", "0123456789abcde");
        ServletResponseFixture response = new ServletResponseFixture();
        Util.respondWithFile(new File("test.html"), response);
        assertEquals("content length", 15, response.contentLength);
        assertEquals("returned data", "0123456789abcde",
                response.out.toString());
        TestUtil.deleteTree("test.html");
    }

    public void test_urlComplete() {
        assertEquals("url /a/b/c", true,
                Util.urlComplete(new StringBuffer("/a/b/c")));
        assertEquals("url a/b/c", false,
                Util.urlComplete("a/b/c"));
        assertEquals("url http://www.xyz/com/foo", true,
                Util.urlComplete("http://www.xyz/com/foo"));
        assertEquals("url ftp://cs.berkeley.edu/foo", true,
                Util.urlComplete("ftp://cs.berkeley.edu/foo"));
        assertEquals("url noSlashes", false,
                Util.urlComplete("noSlashes"));
    }
}
