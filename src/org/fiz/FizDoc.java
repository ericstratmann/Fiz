package org.fiz;
import com.sun.javadoc.*;

/**
 * FizDoc is a Doclet used for creating Javadoc documentation for Fiz.
 * It finds all of the comment text, then invokes DocTranslator to
 * modify the comment text, identifying forms such as "term: definition"
 * that are easy to read in code, and replacing them with forms that look
 * good in HTML, such as {@code <table>}, {@code <ul>}, etc.  See
 * DocTranslator for details on the supported syntax.
 */

public class FizDoc extends com.sun.tools.doclets.standard.Standard {
    // No constructor: this class only has a static methods.
    private FizDoc() {}

    /**
     * This method is our main entry point; it is invoked by Javadoc.
     * It scans all of the accumulated documentation and performs
     * translations on all of the relevant comment text.  Then it invokes
     * the standard doclet to do its normal things.
     * @param root                 Provides access to all of the
     *                             accumulated documentation.
     * @return                     The return value is determined by the
     *                             standard doclet.
     */
    public static boolean start(RootDoc root) {
        for (ClassDoc cl : root.classes()) {
            handleClass(cl);
        }
        return com.sun.tools.doclets.standard.Standard.start(root);
    }

    protected static void handleClass(ClassDoc cl) {
        cl.setRawCommentText((new DocTranslator(
                cl.getRawCommentText())).translate());
//        System.out.printf("\n-------------- %s\n%s", cl.name(),
//                cl.getRawCommentText());
        for (Doc doc : cl.constructors()) {
            doc.setRawCommentText((new DocTranslator(
                    doc.getRawCommentText())).translate());
        }
        for (Doc doc : cl.fields()) {
            doc.setRawCommentText((new DocTranslator(
                    doc.getRawCommentText())).translate());
        }
        for (Doc doc : cl.methods()) {
            doc.setRawCommentText((new DocTranslator(
                    doc.getRawCommentText())).translate());
        }
        for (ClassDoc inner : cl.innerClasses()) {
            handleClass(inner);
        }
    }
}
