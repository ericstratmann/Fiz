package org.fiz;

/**
 * This interface contains no methods.  If a class declares itself to
 * implement this interface, the class is indicating that it is prepared
 * to handle Ajax requests that come directly to it, bypassing the usual
 * Interactor entry points; any static method in the class that takes a single
 * ClientRequest argument and has a name starting with {@code ajax}
 * can be invoked directly using URLs.  For example, if the class name is
 * {@code FooSection} and the method name is {@code ajaxMethod} than an
 * HTTP request for URL {@code fiz/FooSection/ajaxMethod} will be dispatched
 * to {@code ajaxMethod}.
 */
public interface DirectAjax {
}
