package org.fiz;

/**
 * An object that contains child elements that can each be uniquely
 * identified by an id. The {@code childHtml} method is invoked to generate
 * the HTML for a child element.
 * Objects of this type are invoked by the {@link org.fiz.TableLayout} class
 * to generate HTML.
 */
public interface TableLayoutContainer {

    /**
     * Generate HTML for a child element of this container and append it to
     * the {@link org.fiz.Html} object associated with {@code cr}. If a child
     * element named {@code id} does not exist, this method just returns without
     * modifying {@code cr}.
     *
     * @param id        The id of the child element.
     * @param cr        Overall information about the client
     *                  request being serviced; HTML should get appended to
     *                  {@code cr.getHtml()}.
     */
    public void renderChild(String id, ClientRequest cr); 
}
