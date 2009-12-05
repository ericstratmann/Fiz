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
 * RawDataManager implements two requests (one normal and one for returning
 * errors) that return precomputed data provided by the caller.
 */

public class RawDataManager {
    /**
     * Create a DataRequest that will return a given dataset as its result.
     * @param result               Use this as the result of the request.
     * @return                     A DataRequest whose response will be
     *                             {@code result}.
     */
    public static DataRequest newRequest(Dataset result) {
        DataRequest request = new DataRequest("raw.normal");
        request.setComplete(result);
        return request;
    }

    /**
     * Create a DataRequest that will return an error.
     * @param errorInfo            The arguments consist of any number
     *                             of datasets, which provide information
     *                             about the "error".
     * @return                     A DataRequest that will return with
     *                             an error.
     */
    public static DataRequest newError(Dataset... errorInfo) {
        DataRequest request = new DataRequest("raw.error");
        request.setError(errorInfo);
        return request;
    }
}
