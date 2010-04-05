/* Copyright (c) 2008-2010 Stanford University
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

package org.fiz.section;

import org.fiz.*;
import java.util.*;

/**
 * A TableLayout allows a layout to be specified using an ASCII picture of a
 * table. For example, the following layout specifies how to display variables
 * with ids {@code heading, img1, img2, img3}:
 * <pre>
 * +-------------+
 * |   heading   |
 * +------+------+
 * | img1 |      |
 * +------+ img3 |
 * | img2 |      |
 * +------+------+
 * </pre>
 * Tables consists of the characters {@code |}, {@code -}, and {@code +}, plus
 * the ids in the table.
 *
 * Unlike some layouts, TableLayouts do not use @-signs in front of identifiers.
 *
 * TableLayouts support the same properties as Layouts do (though the
 * {@code format} string is interpreted differently), along with following two:
 * id:                   (optional)  Used as the {@code id} attribute for
 *                       the HTML table. Used to find the table in
 *                       Javascript, e.g. to make it visible or invisible.
 *                       Must be unique among all id's for the page.
 * class:                (optional) Used as the {@code class} attribute for
 *                       the HTML table.
 */
public class TableLayout extends Layout {

    /**
     * A {@code ParseError} is thrown in the {@code html} method if the ASCII
     * layout description contains errors.
     */
    public static class ParseError extends Error {
        /**
         * Construct a ParseError with a given message.
         * @param message          Message describing the problem.
         */
        public ParseError(String message) {
            super(message);
        }

        /**
         * Construct a ParseError with the specified cause.
         * @param cause          The cause of this error.
         */
        public ParseError(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Describes an HTML fragment. A layout is parsed into a list of
     * {@code HtmlFragment} objects which can later be expanded as appropriate.
     */
    protected static class HtmlFragment {
        enum FragmentType {
            // Expand as a Fiz template.
            TemplateHtml,
            // Expand by looking up the Id in data passed to the layout
            Id,
            // Expand by copying contents verbatim.
            PlainHtml
        }

        /**
         * Indicates how this HTML fragment must be expanded.
         */
        FragmentType type;

        /**
         * The value of this Html fragment. Depending on the value of
         * {@code type}, this is either a template, the id of value to be
         * substituted in, or plain HTML.
         */
        String value;

        /**
         * Construct a new HtmlFragment.
         *
         * @param value     The value of this HtmlFragment.
         * @param type      The type of HtmlFragment.
         */
        HtmlFragment(String value, FragmentType type) {
            this.value = value;
            this.type = type;
        }
    }

    // Character used to indicate a horizontal separator.
    protected final static char H = '-';

    // Character used to indicate a vertical separator.
    protected final static char V = '|';

    // Character used to indicate an intersection of a horizontal and vertical
    // separator.
    protected final static char I = '+';

    // Maintains a cache of parsed layout descriptions. This cache lasts for
    // the lifetime of the application.
    // The cache is implemented as a mapping of layout strings to parsed
    // intermediate representations of the layout which can later be efficiently
    // expanded.
    // Note: In the current implementation, entries in the cache are never
    // removed once they are inserted.
    protected static final HashMap<String, ArrayList<HtmlFragment>>
            layoutCache = new HashMap<String, ArrayList<HtmlFragment>>();

    // Records the number of times a cached layout description was used. This
    // is used only for testing the caching functionality.
    protected static int cacheUseCount = 0;

    /**
     * Construct a TableLayout.
     * @param properties      A collection of values describing the
     *                        configuration of the section; see above for
     *                        supported values.
     */
    public TableLayout(Dataset properties) {
        super(properties);
    }

    /**
     * Discard all cached information (such as previously parsed layout
     * descriptions).
     * Typically invoked during debugging sessions to flush caches on every
     * request.
     */
    public static void clearCache() {
        synchronized (layoutCache) {
            layoutCache.clear();
        }
    }

    /**
     * Generates HTML for a table layout.
     * @param cr                Overall information about the client
     *                          request being serviced.
     * @throws ParseError       Thrown if there was an error parsing the
     *                          format specified in {@code properties}.
     */
    public void render(ClientRequest cr) {
        String format = findFormat(cr);

        // Check if the format has already been parsed.
        // Note: The layoutCache is shared application-wide, so access to it
        // must be synchronized.
        ArrayList<HtmlFragment> parsedLayout;
        synchronized (layoutCache) {
            parsedLayout = layoutCache.get(format);
            if (parsedLayout == null) {
                // Create a new instance of a TableLayout object
                // and cache it for later use.
                parsedLayout = parse(format);
                layoutCache.put(format, parsedLayout);
            } else {
                // Record the fact that a cached value is being used.
                cacheUseCount++;
            }
        }

        // Expand the parsed layout description.
        StringBuilder out = cr.getHtml().getBody();
        Dataset data = getData(cr);
        for (HtmlFragment fragment : parsedLayout) {
            switch (fragment.type) {
                case TemplateHtml:
                    Template.appendHtml(out, fragment.value, properties);
                    break;
                case Id:
                    cr.renderVariable(data.get(fragment.value));
                    break;
                case PlainHtml:
                    out.append(fragment.value);
                    break;
            }
        }
    }

    /**
     * Validates and parses a multi-line string describing the layout and
     * creates an intermediate representation of the layout.
     *
     * @param layout        A multi-line string describing the layout.

     * @return              An intermediate representation of the parsed layout
     *                      description.
     * @throws ParseError   Thrown in case of any parse errors.
     */
    protected static ArrayList<HtmlFragment> parse(CharSequence layout)
            throws ParseError {
        // Example:
        // This following layout:
        // +---+---+---+
        // | a | b |   |
        // +---+---+ d |
        // |   c   |   |
        // +-------+---+
        // would generate an intermediate representation that is equivalent to
        // the following markup:
        //
        // <table cellspacing="0" >
        //   <tr>
        //     <td colspan="1" rowspan="1" >a</td>
        //     <td colspan="1" rowspan="1" >b</td>
        //     <td colspan="1" rowspan="2" >d</td>
        //   </tr>
        //   <tr>
        //     <td colspan="2" rowspan="1" >c</td>
        //   </tr>
        // </table>

        // Convert the layout description in "lines" into a char[][]. Using
        // array indexing rather than String.charAt() makes the code more
        // compact and readable.
        char[][] grid = createGridFromLayout(layout);

        // Two passes are used to expand the layout description:
        // In the first pass, row and column span vectors are constructed. The
        // column span and row span of a table cell can be calculated using
        // these vectors. e.g. For a table cell whose top left corner and bottom
        // right corners are at positions (r1, c1) and (r2, c2), the column span
        // is (cspan[c2] - cspan[c1]) and the row span is
        // (rspan[r2] - rspan[r1]) where cspan and rspan are the column span and
        // row span vectors respectively. Pictorially:
        //
        //     11112223334
        //   1 +---+-----+
        //   1 |   |     |
        //   2 |   +--+--+
        //   2 |   |  |  |
        //   3 +---+--+--+
        //
        // In the second pass, the table cells are identified and HTML is
        // generated using the row span and column span vectors from the first
        // pass to calculate the row span and column span for each cell.

        int[] rspan = new int[grid.length];
        int[] cspan = new int[grid[0].length];

        // First pass: cspan and rspan will be filled in with column and row
        // span vectors by preprocessLayout().
        preprocessLayout(grid, rspan, cspan);

        ArrayList<HtmlFragment> parsedLayout = new ArrayList<HtmlFragment>();

        // Add the template HTML fragment to the parsed layout description.
        parsedLayout.add(new HtmlFragment("<table {{id=\"@id\"}} " +
                "{{class=\"@class\"}} cellspacing=\"0\" >\n",
                HtmlFragment.FragmentType.TemplateHtml));

        // Second pass: identify each table cell and generate HTML.
        StringBuilder htmlFragment = new StringBuilder();
        for (int r = 0; r < grid.length - 2; r++) {
            boolean newTableRow = false;
            if ((r == 0) || (rspan[r] != rspan[r-1])) {
                newTableRow = true;
                htmlFragment.append("  <tr>\n");
            }
            for (int c = 0; c < grid[r].length - 2; c++) {
                // Find the top left corner of a cell, i.e. any of the following
                // forms:
                // +-  ++  +-  ++
                // | , | , + , +
                if ((grid[r][c] != I) ||
                        !(((grid[r][c+1] == H) || (grid[r][c+1] == I)) &&
                                ((grid[r+1][c] == V) || (grid[r+1][c] == I)))) {
                    continue;
                }

                // Find the bottom right corner of the cell.
                int height = 1;
                int width = 1;
                while (!((grid[r][c+width] == I) &&
                        ((grid[r+1][c+width] == V) ||
                                (grid[r+1][c+width] == I)))) {
                    width++;
                }
                while (!((grid[r+height][c] == I) &&
                        ((grid[r+height][c+1] == H) ||
                                (grid[r+height][c+1] == I)))) {
                    height++;
                }

                // Compute the row span and column span for the table cell.
                int colspan = cspan[c+width] - cspan[c];
                int rowspan = rspan[r+height] - rspan[r];

                // Generate markup for the table cell.
                htmlFragment.append("    <td colspan=\"").append(colspan)
                        .append("\" rowspan=\"").append(rowspan)
                        .append("\">\n");

                // Add this fragment to the parsed layout description and
                // reset htmlFragment.
                parsedLayout.add(new HtmlFragment(htmlFragment.toString(),
                        HtmlFragment.FragmentType.PlainHtml));
                htmlFragment.setLength(0);

                // Parse the cell name.
                String cellName =
                        findCellName(grid, r, c, r + height, c + width);

                // Add the child id to the parsed layout description.
                parsedLayout.add(new HtmlFragment(cellName,
                        HtmlFragment.FragmentType.Id));
                htmlFragment.append("    </td>\n");
            }
            if (newTableRow) {
                htmlFragment.append("  </tr>\n");
            }
        }
        htmlFragment.append("</table>\n");

        // Add the last HTML fragment.
        parsedLayout.add(new HtmlFragment(htmlFragment.toString(),
                HtmlFragment.FragmentType.PlainHtml));

        return parsedLayout;
    }

    /**
     * Converts a layout description into a 2-D array of chars. This method also
     * does some basic validation.
     *
     * @param layout            The layout description to convert.
     * @return                  A 2-D grid of chars that is equivalent to
     *                          {@code layout}.
     * @throws ParseError       Thrown if the basic validation failed.
     */
    protected static char[][] createGridFromLayout(CharSequence layout)
            throws ParseError{
        String[] lines = layout.toString().split("\r\n|\r|\n");
        if (layout.equals("") || (lines.length == 0)) {
            throw new ParseError("layout description cannot be empty");
        }

        int rows = lines.length;
        int cols = lines[0].length();
        char[][] grid = new char[rows][cols];

        for (int r = 0; r < lines.length; r++) {
            String line = lines[r];

            // Every line of the layout description must have the same number
            // of characters.
            if (line.length() != cols) {
                throw new ParseError("incorrectly formatted layout: line " +
                        r + " has incorrect length");
            }
            line.getChars(0, line.length(), grid[r], 0);
        }

        return grid;
    }

    /**
     * Makes one pass through the grid validating each position and finally
     * constructing a column span and row span vector.
     *
     * @param grid              The grid containing the layout description.
     * @param rspan             An output parameter that will contain the
     *                          row span vector. This parameter is assumed to
     *                          be intialized by the caller to an integer array
     *                          of all 0's, whose length is the number of rows
     *                          in {@code grid}.
     * @param cspan             An output parameter that will contain the
     *                          column span vector. This parameter is
     *                          assumed to be initialized by the caller to an
     *                          integer array of all 0's, whose length is
     *                          the number of columns in {@code grid}.
     * @throws ParseError       Thrown if {@code grid} does not contain a valid
     *                          layout description.
     */
    protected static void preprocessLayout(char[][] grid, int[] rspan,
                                           int[] cspan)
            throws ParseError {
        int rInc = 0;
        for (int r = 0; r < grid.length; r++) {
            // Indicates whether this row of the grid contains the start of
            // a new table row.
            boolean newTableRow = false;
            int prevCspan = 0;
            int cInc = 0;
            for (int c = 0; c < grid[r].length; c++) {
                validate(grid, r , c);
                if (grid[r][c] == I && cspan[c] == prevCspan) {
                    // This is the first time a '+' has been seen in this
                    // column of the grid.
                    cInc++;
                }

                // Save the old value of cspan[j] for use in the next
                // iteration of this loop.
                prevCspan = cspan[c];
                cspan[c] += cInc;
                if (grid[r][c] == I) {
                    newTableRow = true;
                }
            }
            if (newTableRow) {
                rInc++;
            }
            rspan[r] = rInc;
        }
    }

    /**
     * Validate the character at position {@code (row, col)}.
     *
     * @param grid          The grid of characters.
     * @param row           The row index of the character.
     * @param col           The column index of the character.
     * @throws ParseError   Thrown if the character at the given position
     *                      causes the grid to have an invalid layout
     *                      description.
     */
    protected static void validate(char[][] grid, int row, int col)
            throws ParseError {
        // The characters to the left, right, top and bottom of the current
        // position, respectively.
        int l = (col > 0) ? grid[row][col-1] : -1;
        int r = (col < grid[row].length-1) ? grid[row][col+1] : -1;
        int t = (row > 0) ? grid[row-1][col] : -1;
        int b = (row < grid.length-1) ? grid[row+1][col] : -1;

        // Indicates whether the current position is one of the 4 corners.
        boolean corner = ((row == 0) || (row == grid.length-1)) &&
                ((col == 0) || (col == grid[row].length-1));

        if (corner) {
            // A corner position must have one of the following forms:
            // +-  -+  |    |
            // | ,  |, +-, -+
            if ((grid[row][col] == I) && (((b == V) && (r == H)) ||
                    ((b == V) && (l == H)) ||
                    ((t == V) && (r == H)) ||
                    ((t == V) && (l == H)))) {
                return;
            } else {
                invalidCharException(grid, row, col);
            }
        }
        switch (grid[row][col]) {
            case I:
                if ((t == H) || (b == H) || (l == V) || (r == V)) {
                    invalidCharException(grid, row, col);
                }
                // At least 3 of the adjoining 4 characters must be valid
                // "special" characters, i.e. '+', '-' or '|'.
                int neighbors = 0;
                if ((t == V) || (t == I)) {
                    neighbors++;
                }
                if ((b == V) || (b == I)) {
                    neighbors++;
                }
                if ((l == H) || (l == I)) {
                    neighbors++;
                }
                if ((r == H) || (r == I)) {
                    neighbors++;
                }
                if (neighbors < 3) {
                    invalidCharException(grid, row, col);
                }
                break;
            case H:
                if ((t == H) || (t == V) || (t == I) ||
                        (b == H) || (b == V) || (b == I) ||
                        !((l == H) || (l == I)) ||
                        !((r == H) || (r == I))) {
                    if (!(
                            ((b == H) && isInteriorDash(grid, row + 1, col)) ||
                            ((t == H) && isInteriorDash(grid, row - 1, col)) ||
                            (isInteriorDash(grid, row, col))
                        )){
                        invalidCharException(grid, row, col);
                    }
                }
                break;
            case V:
                if ((l == H) || (l == V) || (l == I) ||
                        (r == H) || (r == V) || (r == I) ||
                        !((t == V) || (t == I)) ||
                        !((b == V) || (b == I))) {
                    invalidCharException(grid, row, col);
                }
                break;
            default:
                // Do nothing: the other cases are sufficient to correctly
                // validate the layout.
        }
    }

    /**
     * Checks if a '-' character at a given position is in the interior of a
     * section. {If it is, then the caller can choose not to treat it as a
     * special character defining the section boundary.}
     *
     * @param grid          The 2-D char array containing the layout
     *                      description.
     * @param row           The row index of the '-' character in question.
     * @param col           The column index of the '-' character in question.
     * @return              {@code true} if '-' is part of the identifier of a
     *                      cell, {@code false} if it is part of the boundary.
     */
    protected static boolean isInteriorDash(char[][] grid, int row, int col){
        int colLeft = col;
        int colRight = col;
        /*
         * Check if a '|' appears to the left and right of the character in
         * question before a '+'. If both the above conditions are met, then
         * classify the character in question as being part of the identifier
         * of a section.
         *
         * For example:
         *     +----+
         *     |    |
         *     | x  |
         *     +--y-+
         *
         * This will return true for '-' at location 'x', because it has
         * a '|' on both sides. However, it will return false for a '-' at
         * location 'y', because there is a '+' sign on wither side of it.
         */
        while ((colLeft >= 0) && (grid[row][colLeft] != I) &&
                (grid[row][colLeft] != V)){
            colLeft--;
        }
        while ((colRight < grid[0].length) && (grid[row][colRight] != I) &&
                (grid[row][colRight] != V)){
            colRight++;
        }
        return ((colLeft >=0) && (grid[row][colLeft] == V) &&
                (colRight < grid[0].length) && (grid[row][colRight] == V));
    }

    /**
     * Utility method that constructs and throws a ParseError.
     *
     * @param grid          The 2-D char array containing the layout
     *                      description.
     * @param row           The row index of the offending character.
     * @param col           The column index of the offending character.
     * @throws ParseError   Always throws a ParseError.
     */
    protected static void invalidCharException(char[][] grid, final int row,
                                        final int col)
            throws ParseError {
        // Provide an excerpt from the layout description. This may be useful
        // when trying to figure out why the ParseError occurred.
        String excerpt = "";
        for (int r = row-2; r <= row+2; r++) {
            if (r >= 0 && r < grid.length) {
                for (int c = col-2; c <= col+2; c++) {
                    if (c >= 0 && c < grid[r].length) {
                        excerpt += grid[r][c];
                    }
                }

                // Do not add a new-line for the last row.
                if (r != row+2 && r != grid.length-1) {
                    excerpt += "\n    ";
                }
            }
        }

        throw new ParseError("invalid characters around position " +
                "(" + row + "," + col + "):\n    " + excerpt);
    }

    /**
     * Finds the cell name given a grid and the coordinates of the top-left and
     * bottom-right corners of the cell. This method simply concatenates all
     * the non-space characters in the given cell and returns the resulting
     * string. If there are no non-space characters in the cell then the empty
     * string is returned.
     * <pre>
     * (r1,c1)
     *        +---+
     *        |   |
     *        +---+
     *             (r2,c2)
     * </pre>
     *
     * @param grid      The 2-D char array containing the layout description.
     * @param r1        The column number of the top left corner.
     * @param c1        The row number of the top left corner.
     * @param r2        The column number of the bottom right corner.
     * @param c2        The row number of the bottom right corner.
     *
     * @return String   The cell name.
     */
    protected static String findCellName(char[][] grid, int r1, int c1,
                                         int r2, int c2) {
        StringBuilder name = new StringBuilder();
        for (int r = r1 + 1; r < r2; r++) {
            for (int j = c1 + 1; j < c2; j++) {
                if (grid[r][j] != ' ') {
                    name.append(grid[r][j]);
                }
            }
        }
        return name.toString();
    }
}
