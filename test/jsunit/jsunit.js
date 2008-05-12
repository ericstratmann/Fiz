// jsunit.js --
//
// This file acts as the main driver for Javascript unit tests for Fiz.
// It is invoked using the Rhino Javascript interpreter with a command line
// like the following:
//
// java <javaArgs> org.mozilla.javascript.tools.shell.Main jsunit.js <options> file file ...
//
// <javaArgs> consists of argument to the Java runtime to make available
// all the relevant classes, including the Rhino engine and the Fiz class
// {@code JsunitHelper), which provides utility functions needed by the
// test framework.
//
// For information about the other command-line options, read the help text
// in the printHelp method below, or invoke jsunit with the -help option.

// All of jsunit's global data is kept in the following object:
var jsunit = {
    // Values for command-line options.
    files:             new Array(),
    path:              ["javascript", "test/jsunit"],
    quiet:             0,

    // Name of the file whose tests are currently executing:
    currentFile:       null,

    // Name of the test currently being executed:
    currentTestName:   null,

    // Has an error occurred in the current test?
    currentTestError:  false,

    // Various statistics:
    numTests:          0,
    numErrors:         0,
    numFiles:          0,

    // Object used to access additional facilities provided by the Java
    // class JsunitHelper.
    helper:            null,

    // The following variable is used by dummy methods (stand-ins for
    // official browser features such as {@code alert}) to log the fact
    // that they were called.  Tests can check this variable to verify
    // that certain calls were made.  The variable is cleared before
    // each test.
    log:               ""
}

// Create the global {@code window} object, which is a container for
// all of the interesting browser objects.

window = new Object();

/**
 * This is the main function for jsunit.  It processes command-line
 * options, executes all of the tests that were requested, then press
 * summary information.
 * @param options                  Array of command-line options.
 */
function main(options) {
    defineClass("org.fiz.JsunitHelper");
    jsunit.helper = new JsunitHelper();
    parseArguments(options);

    // Load standard test fixtures.
    include("ElementFixture.js");
    include("DocumentFixture.js");

    // Run the tests.
    while (jsunit.files.length > 0) {
        file = jsunit.files.shift();
        jsunit.currentFile = file;
        jsunit.numFiles++;
        if (!jsunit.quiet) {
            print(file + ":");
        }

        // Run this test file.  There doesn't seem to be any way to find out
        // if the file could be found; Rhino just prints an error message
        // and returns normally.
        load(file);
    }

    // Print final statistics.
    print("");
    print("Test files:   " + jsunit.numFiles);
    print("Total tests:  " + jsunit.numTests);
    print("Total errors: " + jsunit.numErrors);
}

/**
 * Process a collection of command-line options and set values in
 * {@code jsunit}.
 * @param options                  Array containing any number of command-line
 *                                 options.
 */
function parseArguments(options) {
    while (options.length > 0) {
        if (options[0].charAt(0) != "-") {
            // All the remaining arguments are names of either files or
            // directories.  Expand the directory names into lists of
            // test files.
            while (options.length > 0) {
                var expanded = jsunit.helper.getTestFiles(options.shift());
                var files = expanded.split(",");
                while (files.length > 0) {
                    jsunit.files.push(files.shift());
                }
        }
            return;
        }

        // Process the next argument, removing the argument name (and value,
        // if present) from the argument array.
        var option = options.shift();
        if (option == "-help") {
            printHelp();
            quit();
        } else if (option == "-path") {
            checkOptionValue(options, option);
            jsunit.path = options.shift().split(",");
        } else if (option == "-quiet") {
            jsunit.quiet = 1;
        } else {
            // Unknown option.
            print("Error: unknown option \"" + option + "\"");
            printHelp();
            quit();
        }
    }
}

/**
 * Make sure that the options array contains a value for the current option;
 * if not, print an error message and exit.
 * @param args                     Current contents of the command-line
 *                                 options array (contains all options after
 *                                 the name of the current option).
 * @param option                   Name of the current option; used to
 *                                 generate an error message.
 */
function checkOptionValue(args, option) {
    if (args.length == 0) {
        print("Error: no value specified for \"" + option + "\" option");
        quit();
    }
}

/**
 * Print help text describing the command-line options for this program.
 */
function printHelp() {
    print("Usage: jsunit [options] file/dir file/dir ...");
    print("");
    print("Options:");
    print("-help             Print this message and exit without running");
    print("                  any tests.");
    print("-path path        Comma-separated list of directories in which");
    print("                  to search for Javascript files included");
    print("                  by tests.");
    print("-quiet            Don't print non-essential output (such as");
    print("                  messages indicating test success).");
}

/**
 * Generate a banner line of the form "text ......." were the number of
 * dots is adjusted so that the total line length is a fixed value.  If
 * the text is so long that there would be no dots, the banner is extended
 * beyond the normal length so there are always at least four dots (this
 * helps postprocessors find error messages).
 * @param text                     Text to appear at the left side of the
 *                                 banner.
 */
function banner(text) {
    var result = text + " ....";
    for (var i = result.length; i < 65; i++) {
        result += ".";
    }
    return result;
}

/**
 * This function runs a single test: it is is invoked by test files.
 * It invokes {@code func}, which implements the actual test, catches
 * exceptions that happen inside {@code func}, collects statistics, and
 * prints log information.
 * @param name                     Name of the test case.
 * @param func                     Function containing the body of the test.
 */
function test(name, func) {
    jsunit.numTests++;
    jsunit.currentTestName = name;
    jsunit.currentTestError = false;
    jsunit.log = "";
    try {
        func();
    } catch (e) {
        var where = "";
        if (e.fileName && e.lineNumber) {
            where = " (" + e.fileName + ":" + e.lineNumber + ")";
        }
        error("Exception in test \"" + name + "\"" + where + ":\n" + e);
    }
    if (!jsunit.currentTestError && !jsunit.quiet) {
        print(banner(name) + " PASSED");
    }
}

/**
 * This function is invoked when an error occurs during a test.  It prints
 * information about the error and updates statistics.
 * @param msg                      Message describing the error.  Need not
 *                                 and in newline (a newline will be added
 *                                 when the message is printed)
 */
function error(msg) {
    // If we are in the noisy mode, make sure the test banner has been
    // printed.
    if (!jsunit.currentTestError) {
        print(banner(jsunit.currentTestName) + " FAILED");
    }
    jsunit.currentTestError = true;
    jsunit.numErrors++;
    print(msg);
    print("------------------------------");
}

/**
 * This function is invoked by individual tests to compare a test output
 * within expected value and generate an error if they are different.
 * @param expected                 Value that the test should have produced.
 * @param actual                   Value produced by the test.
 * @param description              (optional) Information about the test being
 *                                 perform, for use in error messages.  It
 *                                 typically describes the value being checked
 *                                 or the sub-case within a particular test.
 */
function assertEqual(expected, actual, description) {
    if (expected == actual) {
        return;
    }
    var message = "Error in test \"" + jsunit.currentTestName + "\"";
    if (description != null) {
        message += ": " + description;
    }
    message += "\nExpected value: " + expected.toString() +
            "\nActual value:   " + actual.toString();
    error(message);
}

/**
 * Load a Javascript file by searching all of the directories in the
 * path and picking the first one that exists.  The contents of the file
 * are then evaluated using the Rhino {@code load} function.
 * @param fileName                 Name of the desired Javascript file.
 */
function include(fileName) {
    for (var i = 0, length = jsunit.path.length; i <length; i++) {
        var fullName = jsunit.path[i] + "/" + fileName;
        if (jsunit.helper.fileExists(fullName)) {
            load(fullName);
            return;
        }
    }
    throw "couldn't find file \"" + fileName + "\" in path (" +
          jsunit.path.join(",") + ")";
}

/**
 * This method is a stand-in for the browser method of the same name.
 * It just logs information about the call to {@code jsunit.log}.
 * @param message                  Message that would normally appear in a
 *                                 popup window.
 */
function alert(message) {
    jsunit.log += "alert(message: " + message + ")\n";
}

main(arguments);