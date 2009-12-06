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

package org.fiz;

/**
 * InstantiationError is thrown by various Fiz methods when a dynamically
 * loaded class cannot be instantiated.    It is used in situations where
 * it's unlikely that the caller will be able to handle the problem; most
 * likely, the current request will be aborted.  Using an Error allows us
 * to unwind the call stack easily without every ancestor having to declare
 * an exception.
 */

public class InstantiationError extends Error {
    /**
     * Construct a InstantiationError with a message that contains the
     * className and an additional message.
     * @param className            Name of the class that could not be found.
     * @param message              Additional information about the problem,
     *                             such as "class isn't a subclass of Foo".
     */
    public InstantiationError(String className, String message) {
        super("couldn't create an instance of class \"" + className +
                "\": " + message );
    }
}