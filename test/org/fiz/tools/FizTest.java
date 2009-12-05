/* Copyright (c) 2009 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.fiz.tools;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * JUnit tests for the Fiz command line tool.
 * Note: This set of tests does not completely test the tool. This is because
 * of the difficulty of creating stubs for methods that interact with the
 * Fiz website.
 */
public class FizTest extends junit.framework.TestCase {
    Fiz fizTool;

    public void setUp() {
        fizTool = new Fiz();
    }

    public void test_ToolError() {
        String msg = "a message";
        Fiz.ToolError error = new Fiz.ToolError(Fiz.Command.createExt, msg);
        assertEquals("exception command", Fiz.Command.createExt,
                error.getCommand());
        assertEquals("exception message", msg, error.getMessage());
    }
    public void test_parseGlobalOptions() {
        String serverUrl = "http://abc.xyz";
        String[] args = {
                "x",
                "--v",
                "-y",
                "--s",
                serverUrl,
                "z"
        };
        ArrayList<String> result = fizTool.parseGlobalOptions(args);
        assertEquals("server url", serverUrl, fizTool.serverUrl);
        assertEquals("log level", Fiz.LogLevel.verbose, fizTool.logLevel);
        assertTrue("contains other args", result.contains("x") &&
                result.contains("-y") && result.contains("z"));
    }
    public void test_parseGlobalOptions_logLevelQuiet() {
        String[] args = {
                "--q"
        };
        ArrayList<String> result = fizTool.parseGlobalOptions(args);
        assertEquals("log level", Fiz.LogLevel.quiet, fizTool.logLevel);
        assertEquals("empty result", 0, result.size());
    }
    public void test_parseGlobalOptions_errorInvalidOption() {
        String[] args = {
                "--x"
        };
        boolean gotException = false;
        try {
            fizTool.parseGlobalOptions(args);
        }
        catch(Fiz.ToolError e) {
            gotException = true;
        }
        assertTrue("exception happened", gotException);
    }
    public void test_parseGlobalOptions_errorMissingArg() {
        String[] args = {
                "--s"
        };
        boolean gotException = false;
        try {
            fizTool.parseGlobalOptions(args);
        }
        catch(Fiz.ToolError e) {
            gotException = true;
        }
        assertTrue("exception happened", gotException);
    }
    public void test_parseCommand_help() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("help");
        Fiz.Command result = fizTool.parseCommand(args);
        assertEquals("help command", Fiz.Command.help, result);
        assertTrue("removed command", !args.contains("help"));

        args.clear();
        args.add("h");
        result = fizTool.parseCommand(args);
        assertEquals("shortcut", Fiz.Command.help, result);
        assertTrue("removed command", !args.contains("h"));
    }
    public void test_parseCommand_checkCore() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("check");
        args.add("core");
        Fiz.Command result = fizTool.parseCommand(args);
        assertEquals("check core command", Fiz.Command.checkCore, result);
        assertTrue("removed command", !(args.contains("check") ||
                args.contains("core")));

        args.clear();
        args.add("chc");
        result = fizTool.parseCommand(args);
        assertEquals("command shortcut",
                Fiz.Command.checkCore, result);
        assertTrue("removed command", !args.contains("chc"));
    }
    public void test_parseCommand_checkExt() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("check");
        args.add("ext");
        Fiz.Command result = fizTool.parseCommand(args);
        assertEquals("check ext command", Fiz.Command.checkExt, result);
        assertTrue("removed command", !(args.contains("check") ||
                args.contains("ext")));

        args.clear();
        args.add("che");
        result = fizTool.parseCommand(args);
        assertEquals("command shortcut",
                Fiz.Command.checkExt, result);
        assertTrue("removed command", !args.contains("che"));
    }
    public void test_parseCommand_createApp() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("create");
        args.add("app");
        Fiz.Command result = fizTool.parseCommand(args);
        assertEquals("create app command", Fiz.Command.createApp, result);
        assertTrue("removed command", !(args.contains("check") ||
                args.contains("app")));

        args.clear();
        args.add("cra");
        result = fizTool.parseCommand(args);
        assertEquals("command shortcut",
                Fiz.Command.createApp, result);
        assertTrue("removed command", !args.contains("cra"));
    }
    public void test_parseCommand_createExt() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("create");
        args.add("ext");
        Fiz.Command result = fizTool.parseCommand(args);
        assertEquals("create ext command", Fiz.Command.createExt, result);
        assertTrue("removed command", !(args.contains("check") ||
                args.contains("ext")));

        args.clear();
        args.add("cre");
        result = fizTool.parseCommand(args);
        assertEquals("command shortcut",
                Fiz.Command.createExt, result);
        assertTrue("removed command", !args.contains("cre"));
    }
    public void test_parseCommand_installCore() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("install");
        args.add("core");
        Fiz.Command result = fizTool.parseCommand(args);
        assertEquals("install core command", Fiz.Command.installCore, result);
        assertTrue("removed command", !(args.contains("install") ||
                args.contains("core")));

        args.clear();
        args.add("ic");
        result = fizTool.parseCommand(args);
        assertEquals("command shortcut",
                Fiz.Command.installCore, result);
        assertTrue("removed command", !args.contains("ic"));
    }
    public void test_parseCommand_installExt() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("install");
        args.add("ext");
        Fiz.Command result = fizTool.parseCommand(args);
        assertEquals("install ext command", Fiz.Command.installExt, result);
        assertTrue("removed command", !(args.contains("install") ||
                args.contains("ext")));

        args.clear();
        args.add("ie");
        result = fizTool.parseCommand(args);
        assertEquals("command shortcut",
                Fiz.Command.installExt, result);
        assertTrue("removed command", !args.contains("ie"));
    }
    public void test_parseCommand_upgrade() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("upgrade");
        Fiz.Command result = fizTool.parseCommand(args);
        assertEquals("upgrade command", Fiz.Command.upgrade, result);
        assertTrue("removed command", !args.contains("upgrade"));

        args.clear();
        args.add("u");
        result = fizTool.parseCommand(args);
        assertEquals("command shortcut",
                Fiz.Command.upgrade, result);
        assertTrue("removed command", !args.contains("u"));
    }
    public void test_parseCommand_version() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("version");
        Fiz.Command result = fizTool.parseCommand(args);
        assertEquals("version command", Fiz.Command.version, result);
        assertTrue("removed command", !args.contains("version"));

        args.clear();
        args.add("v");
        result = fizTool.parseCommand(args);
        assertEquals("command shortcut",
                Fiz.Command.version, result);
        assertTrue("removed command", !args.contains("v"));
    }
    public void test_parseCommand_error() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("xyz");
        boolean gotException = false;
        try {
            fizTool.parseCommand(args);
        }
        catch(Fiz.ToolError e) {
            gotException = true;
        }
        assertTrue("exception happened", gotException);
    }
    public void test_parseOptions() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("a1");
        args.add("-a");
        args.add("-b");
        args.add("a2");
        args.add("-c");
        args.add("carg");
        args.add("-de");
        HashMap<String, String> result =
                fizTool.parseOptions(Fiz.Command.unknown, args, "c");
        assertTrue("contains options", result.containsKey("a") &&
                result.containsKey("b") &&
                result.containsKey("c") &&
                result.containsKey("d") &&
                result.containsKey("e"));
        assertEquals("option with parameter", "carg", result.get("c"));
        assertTrue("contains other args", args.contains("a1") &&
                args.contains("a1"));
        assertTrue("does not contain options and parameters",
                !(args.contains("-a") || args.contains("-b") || args.contains("-c") ||
                args.contains("carg") || args.contains("-de")));
    }
    public void test_parseOptions_errorStrayHyphen() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("-");
        boolean gotException = false;
        try{
            fizTool.parseOptions(Fiz.Command.checkCore, args, "");
        }
        catch (Fiz.ToolError e) {
            gotException = true;
            assertEquals("exception command", Fiz.Command.checkCore,
                    e.getCommand());
        }
        assertTrue("exception happened", gotException);
    }
    public void test_parseOptions_errorDuplicateOption() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("-ab");
        args.add("-b");
        boolean gotException = false;
        try {
            fizTool.parseOptions(Fiz.Command.checkCore, args, "");
        }
        catch (Fiz.ToolError e) {
            gotException = true;
            assertEquals("exception command", Fiz.Command.checkCore,
                    e.getCommand());
        }
        assertTrue("exception happened", gotException);
    }
    public void test_parseOptions_errorOptionWithoutParam() {
        ArrayList<String> args = new ArrayList<String>();
        args.add("-a");
        args.add("-bc");
        args.add("x");
        boolean gotException = false;
        try {
            fizTool.parseOptions(Fiz.Command.checkCore, args, "b");
        }
        catch (Fiz.ToolError e) {
            gotException = true;
            assertEquals("exception command", Fiz.Command.checkCore,
                    e.getCommand());
        }
        assertTrue("exception happened", gotException);
    }
}
