package org.fiz;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * JUnit tests for TableLayout.
 */
public class TableLayoutTest extends junit.framework.TestCase {
    protected ClientRequestFixture cr;
    protected TableLayoutContainer container;

    public void setUp() {
        cr = new ClientRequestFixture();
        container = new CompoundSection(null,
                new TemplateSection(new Dataset("id", "1",
                        "template", "<h1>section 1</h1>\n")),
                new TemplateSection(new Dataset("id", "2",
                        "template", "<h1>section 2</h1>\n")),
                new TemplateSection(new Dataset("id", "3",
                        "template", "<h1>section 3</h1>\n")),
                new TemplateSection(new Dataset("id", "4",
                        "template", "<h1>section 4</h1>\n")),
                new TemplateSection(new Dataset("id", "5",
                        "template", "<h1>section 5</h1>\n")),
                new TemplateSection(new Dataset("id", "6",
                        "template", "<h1>section 6</h1>\n")),
                new TemplateSection(new Dataset("id", "7",
                        "template", "<h1>section 7</h1>\n")),
                new TemplateSection(new Dataset("id", "8",
                        "template", "<h1>section 8</h1>\n")),
                new TemplateSection(new Dataset("id", "9",
                        "template", "<h1>section 9</h1>\n")),
                new TemplateSection(new Dataset("id", "10",
                        "template", "<h1>section 10</h1>\n")),
                new TemplateSection(new Dataset("id", "11",
                        "template", "<h1>section 11</h1>\n")),
                new TemplateSection(new Dataset("id", "12",
                        "template", "<h1>section 12</h1>\n"))
            );
        Config.setDataset("tableLayout", new Dataset("useCache", "false"));
    }

    public void test_parseError_constructorMessage() {
        String msg = "a message";
        TableLayout.ParseError error =
                new TableLayout.ParseError(msg);
        assertEquals("exception message", msg, error.getMessage());
    }

    public void test_parseError_costructorCause() {
        Throwable cause = new Exception();
        TableLayout.ParseError error =
                new TableLayout.ParseError(cause);
        assertEquals("exception cause", cause, error.getCause());
    }

    public void test_clearCache() {
        TableLayout.layoutCache.clear();
        TableLayout.layoutCache.put("123",
                new ArrayList<TableLayout.HtmlFragment>());
        assertEquals("layoutCache size before clearCache",
                1, TableLayout.layoutCache.size());
        TableLayout.clearCache();
        assertEquals("layoutCache size after clearCache", 0,
                TableLayout.layoutCache.size());
    }

    public void test_render_noLayout() {
        boolean gotException = false;
        try {
            TableLayout.render(new Dataset(), container, cr);
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"layout\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_render_cache() {
        String layout = "+----+\n" +
                        "| 1  |\n" +
                        "+----+\n";

        TableLayout.clearCache();
        TableLayout.render(new Dataset("layout", layout), container, cr);
        assertEquals("layoutCache size after html", 1,
                TableLayout.layoutCache.size());
    }

    public void test_render_cacheUse() {
        String layout = "+----+\n" +
                        "| 1  |\n" +
                        "+----+\n";
        TableLayout.clearCache();
        TableLayout.render(new Dataset("layout", layout), container, cr);
        int cacheUseCountBefore = TableLayout.cacheUseCount;
        TableLayout.render(new Dataset("layout", layout), container, cr);
        assertEquals("Cache use count", cacheUseCountBefore + 1,
                TableLayout.cacheUseCount);
    }

    public void test_render_useIdAndClass() {
        String layout = "+----+\n" +
                        "| 1  |\n" +
                        "+----+\n";
        TableLayout.clearCache();
        TableLayout.render(new Dataset("id", "abc",
                "class", "someStyle",
                "layout", layout), container, cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML",
                "<table id=\"abc\" class=\"someStyle\" cellspacing=\"0\" >\n" +
                "  <tr>\n" +
                "    <td colspan=\"1\" rowspan=\"1\">\n" +
                "<h1>section 1</h1>\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "</table>\n",
                html);
    }

    public void test_parse_basic() {
        String layout = "+----+\n" +
                        "| 1  |\n" +
                        "+----+\n";
        TableLayout.render(new Dataset("layout", layout), container, cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML",
                "<table cellspacing=\"0\" >\n" +
                        "  <tr>\n" +
                        "    <td colspan=\"1\" rowspan=\"1\">\n" +
                        "<h1>section 1</h1>\n" +
                        "    </td>\n" +
                        "  </tr>\n" +
                        "</table>\n",
                html);
    }

    /**
     * Tests all paths in parse and preprocessLayout using one complex layout.
     */
    public void test_parse_complex() {
        String layout = "+-----+-----+\n" +
                        "|  1  |  2  |\n" +
                        "+--+--+-----+\n" +
                        "|  |4 |  5  |\n" +
                        "|  |  +--+--+\n" +
                        "|3 +--+6 |7 |\n" +
                        "|  |8 +--+--+\n" +
                        "|  |  |  9  |\n" +
                        "+--+-+++----+\n" +
                        "| 10 |1| 12 |\n" +
                        "|    |1|    |\n" +
                        "+----+-+----+";
        TableLayout.render(new Dataset("layout", layout), container, cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML","<table cellspacing=\"0\" >\n" +
                "  <tr>\n" +
                "    <td colspan=\"3\" rowspan=\"1\">\n" +
                "<h1>section 1</h1>\n" +
                "    </td>\n" +
                "    <td colspan=\"3\" rowspan=\"1\">\n" +
                "<h1>section 2</h1>\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td colspan=\"1\" rowspan=\"4\">\n" +
                "<h1>section 3</h1>\n" +
                "    </td>\n" +
                "    <td colspan=\"2\" rowspan=\"2\">\n" +
                "<h1>section 4</h1>\n" +
                "    </td>\n" +
                "    <td colspan=\"3\" rowspan=\"1\">\n" +
                "<h1>section 5</h1>\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td colspan=\"2\" rowspan=\"2\">\n" +
                "<h1>section 6</h1>\n" +
                "    </td>\n" +
                "    <td colspan=\"1\" rowspan=\"2\">\n" +
                "<h1>section 7</h1>\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td colspan=\"2\" rowspan=\"2\">\n" +
                "<h1>section 8</h1>\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td colspan=\"3\" rowspan=\"1\">\n" +
                "<h1>section 9</h1>\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td colspan=\"2\" rowspan=\"1\">\n" +
                "<h1>section 10</h1>\n" +
                "    </td>\n" +
                "    <td colspan=\"2\" rowspan=\"1\">\n" +
                "<h1>section 11</h1>\n" +
                "    </td>\n" +
                "    <td colspan=\"2\" rowspan=\"1\">\n" +
                "<h1>section 12</h1>\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "</table>\n",
                html);
    }

    public void test_createGridFromLayout_emptyString() {
        boolean gotException = false;
        try {
            TableLayout.createGridFromLayout("");
        }
        catch (TableLayout.ParseError e) {
            assertEquals("exception message",
                    "layout description cannot be empty",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_createGridFromLayout_emptyNewLines() {
        boolean gotException = false;
        try {
            TableLayout.createGridFromLayout("\n\n");
        }
        catch (TableLayout.ParseError e) {
            assertEquals("exception message",
                    "layout description cannot be empty",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_createGridFromLayout_incorrectLength() {
        String layout = "+----+\n" +
                        "|     |\n" +
                        "+----+";
        boolean gotException = false;
        try {
            TableLayout.createGridFromLayout(layout);
        }
        catch (TableLayout.ParseError e) {
            assertEquals("exception message",
                    "incorrectly formatted layout: line 1 has incorrect length",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_createGridFromLayout() {
        String layout = "abcd\n" +
                        "efgh\n" +
                        "ijkl\n" +
                        "mnop";
        char grid[][] = TableLayout.createGridFromLayout(layout);
        assertEquals("row 0 of grid", true,
                Arrays.equals(new char[]{'a', 'b', 'c', 'd'}, grid[0]));
        assertEquals("row 1 of grid", true,
                Arrays.equals(new char[]{'e', 'f', 'g', 'h'}, grid[1]));
        assertEquals("row 2 of grid", true,
                Arrays.equals(new char[]{'i', 'j', 'k', 'l'}, grid[2]));
        assertEquals("row 3 of grid", true,
                Arrays.equals(new char[]{'m', 'n', 'o', 'p'}, grid[3]));
    }

    public void test_validate_invalidCorner() {
        String[] layouts = new String[] {
                // Invalid character at corner of the grid.
                "a---+\n" +
                "|   |\n" +
                "+---+\n",

                "----+\n" +
                "|   |\n" +
                "+---+\n",

                "|---+\n" +
                "|   |\n" +
                "+---+\n",

                "+--- \n" +
                "|   |\n" +
                "+---+\n",

                "+----\n" +
                "|   |\n" +
                "+---+\n",

                "+---|\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "x---+\n",

                "+---+\n" +
                "|   |\n" +
                "----+\n",

                "+---+\n" +
                "|   |\n" +
                "|---+\n",

                "+---+\n" +
                "|   |\n" +
                "+---x\n",

                "+---+\n" +
                "|   |\n" +
                "+----\n",

                "+---+\n" +
                "|   |\n" +
                "+---|\n",

                // Invalid characters adjacent to the corner position.
                "++--+\n" +
                "|   |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "a   |\n" +
                "|   |\n" +
                "+---+\n",

                "+--|+\n" +
                "|   |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|    \n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "a   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "|   |\n" +
                "++--+\n",

                "+---+\n" +
                "|   |\n" +
                "|   |\n" +
                "+--++\n",

                "+---+\n" +
                "|   |\n" +
                "|   a\n" +
                "+---+\n"
        };
        for (String l : layouts) {
            boolean gotException = false;
            try {
                TableLayout.render(new Dataset("layout", l), container, cr);
            }
            catch (TableLayout.ParseError e) {
                gotException = true;
            }
            assertEquals("exception happened", true, gotException);
        }
    }

    public void test_validate_invalidIChar() {
        String[] layouts = new String[]{
                "+---+\n" +
                "|   |\n" +
                "| + |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "| + |\n" +
                "| | |\n" +
                "+-+-+\n",

                "+-+-+\n" +
                "| | |\n" +
                "+-+ |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "| +||\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "| + |\n" +
                "| - |\n" +
                "+---+\n",

                "+-+-+\n" +
                "|   |\n" +
                "|   |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "|   +\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "+   |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "|   |\n" +
                "|   |\n" +
                "+-+-+\n",

        };
        test_validate_helper(layouts);
    }

    public void test_validate_invalidHChar() {
        String[] layouts = new String[]{
                "+---+\n" +
                "| - |\n" +
                "|   |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "| | |\n" +
                "|   |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "| + |\n" +
                "|   |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "+---+\n" +
                "| - |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "+---+\n" +
                "| | |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "+---+\n" +
                "| + |\n" +
                "+---+\n",

                "+-d-+\n" +
                "|   |\n" +
                "|   |\n" +
                "|   |\n" +
                "+---+\n",

                "+-|-+\n" +
                "|   |\n" +
                "|   |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "| --+\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "+-a |\n" +
                "|   |\n" +
                "+---+\n"
        };
        test_validate_helper(layouts);
    }

    public void test_validate_invalidVChar() {
        String[] layouts = new String[]{
                "+---+\n" +
                "|   |\n" +
                "|-  |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "||  |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "|+  |\n" +
                "|   |\n" +
                "+---+\n",

                "+-+--+\n" +
                "| |  |\n" +
                "| |- |\n" +
                "| |  |\n" +
                "+-+--+\n",

                "+-+--+\n" +
                "| |  |\n" +
                "| || |\n" +
                "| |  |\n" +
                "+-+--+\n",

                "+-+--+\n" +
                "| |  |\n" +
                "| |+ |\n" +
                "| |  |\n" +
                "+-+--+\n",

                "+---+\n" +
                "|   |\n" +
                "-   |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "    |\n" +
                "|   |\n" +
                "+---+\n",

                "+-+-+\n" +
                "| | |\n" +
                "|   |\n" +
                "|   |\n" +
                "+---+\n",

                "+---+\n" +
                "|   |\n" +
                "| a |\n" +
                "| | |\n" +
                "+-+-+\n"
        };
        test_validate_helper(layouts);
    }

    private void test_validate_helper(String[] layouts) {
        for (String l : layouts) {
            boolean gotException = false;
            try {
                TableLayout.render(new Dataset("layout", l), container, cr);
            }
            catch (TableLayout.ParseError e) {
                gotException = true;
            }
            assertEquals("exception happened", true, gotException);
        }
    }

    public void test_invalidCharException_topLeft() {
        String layout = "abcdefg\n" +
                        "hijklmn\n" +
                        "opqrstu\n" +
                        "vwxyzAB\n" +
                        "CDEFGHI\n" +
                        "JKLMNOP\n" +
                        "QRSTUVW";
        char[][] grid = TableLayout.createGridFromLayout(layout);
        boolean gotException = false;
        try {
            TableLayout.invalidCharException(grid, 0, 0);
        } catch (TableLayout.ParseError e) {
            assertEquals("exception message",
                    "invalid characters around position (0,0):\n" +
                            "    abc\n" +
                            "    hij\n" +
                            "    opq",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_invalidCharException_bottomRight() {
        String layout = "abcdefg\n" +
                        "hijklmn\n" +
                        "opqrstu\n" +
                        "vwxyzAB\n" +
                        "CDEFGHI\n" +
                        "JKLMNOP\n" +
                        "QRSTUVW";
        char[][] grid = TableLayout.createGridFromLayout(layout);
        boolean gotException = false;
        try {
            TableLayout.invalidCharException(grid, 6, 6);
        } catch (TableLayout.ParseError e) {
            assertEquals("exception message",
                    "invalid characters around position (6,6):\n" +
                            "    GHI\n" +
                            "    NOP\n" +
                            "    UVW",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_invalidCharException_middle() {
        String layout = "abcdefg\n" +
                        "hijklmn\n" +
                        "opqrstu\n" +
                        "vwxyzAB\n" +
                        "CDEFGHI\n" +
                        "JKLMNOP\n" +
                        "QRSTUVW";
        char[][] grid = TableLayout.createGridFromLayout(layout);
        boolean gotException = false;
        try {
            TableLayout.invalidCharException(grid, 2, 2);
        } catch (TableLayout.ParseError e) {
            assertEquals("exception message",
                    "invalid characters around position (2,2):\n" +
                            "    abcde\n" +
                            "    hijkl\n" +
                            "    opqrs\n" +
                            "    vwxyz\n" +
                            "    CDEFG",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_findCellName_basic() {
        String layout = "+-----+\n" +
                        "| abc |\n" +
                        "+-----+";
        char[][] grid = TableLayout.createGridFromLayout(layout);
        String cellName = TableLayout.findCellName(grid, 0, 0, 2, 6);
        assertEquals("cell name", "abc", cellName);
    }

    public void test_findCellName_multiLine() {
        String layout = "+-----+\n" +
                        "| abc |\n" +
                        "|  d  |\n" +
                        "+-----+";
        char[][] grid = TableLayout.createGridFromLayout(layout);
        String cellName = TableLayout.findCellName(grid, 0, 0, 3, 6);
        assertEquals("cell name", "abcd", cellName);
    }

    public void test_findCellName_empty() {
        String layout = "+-----+\n" +
                        "|     |\n" +
                        "|     |\n" +
                        "+-----+";
        char[][] grid = TableLayout.createGridFromLayout(layout);
        String cellName = TableLayout.findCellName(grid, 0, 0, 3, 6);
        assertEquals("cell name", "", cellName);
    }
}
