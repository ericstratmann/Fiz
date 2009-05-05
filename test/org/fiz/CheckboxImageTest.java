package org.fiz;

import java.util.*;

/**
 * Junit tests for the CheckboxImage class
 */

public class CheckboxImageTest extends junit.framework.TestCase {
	protected ClientRequest cr;
	protected StringBuilder out;
	protected Dataset testRow;
	protected CheckboxImage cbs;
	protected ArrayList<String> falseVals;
	protected Dataset data;

	public void setUp() {
		cr = new ClientRequestFixture();
		out = new StringBuilder();
		falseVals = new ArrayList<String>(Arrays.asList(
			new String[] { "0", "false"}));
		data = new Dataset("id", "foo", "family", "cb");
	}

	public void test_constructor_emptyProperties() {
		try {
			cbs = new CheckboxImage(new Dataset(), falseVals);
		} catch (Dataset.MissingValueError e) {
			return;
		}
		fail("Expected exception not thrown");
	}

	public void test_constructor_propertiesAndArrayList() {
		cbs = new CheckboxImage(data, falseVals);
		assertEquals("foo", cbs.id);
		assertEquals("cb", cbs.family);
		assertEquals("false", cbs.falseValues.get(1));
	}

	public void test_constructor_defaultFamily() {
		cbs = new CheckboxImage(new Dataset("id", "foo"));
		assertEquals(cbs.family, "checkbox");
	}

	public void test_constructor_propertiesAndArray() {
		cbs = new CheckboxImage(data, new String[] {"0", "false"});
		assertEquals("foo", cbs.id);
		assertEquals("cb", cbs.family);
		assertEquals("false", cbs.falseValues.get(1));
	}

	public void test_constructor_propertiesAndVarArgs() {
		cbs = new CheckboxImage(data, "0", "false");
		assertEquals("foo", cbs.id);
		assertEquals("cb", cbs.family);
	}

	public void test_constructor_defaultFalseVals() {
		cbs = new CheckboxImage(data);
		assertEquals(4, cbs.falseValues.size());
		assertEquals(true, cbs.falseValues.contains("false"));
	}

	public void test_render_keyNotFoundInDataset() {
		cbs = new CheckboxImage(data);
		testRow = new Dataset("bar", "null");
		try {
			cbs.render(cr, testRow, out);
		} catch (Dataset.MissingValueError e) {
			return;
		}
		fail("Expected exception not thrown");
	}

	public void test_render_useFalseImg() {
		cbs = new CheckboxImage(data, falseVals);
		testRow = new Dataset("foo", "0");
		cbs.render(cr, testRow, out);
		assertEquals("<img src=\"cb-false.png\" alt=\"unchecked\" />",
			out.toString());

	}

	public void test_render_useTrueImg() {
		cbs = new CheckboxImage(data, falseVals);
		testRow = new Dataset("foo", "1");
		cbs.render(cr, testRow, out);
		assertEquals("<img src=\"cb-true.png\" alt=\"checked\" />",
			out.toString());
	}
}
