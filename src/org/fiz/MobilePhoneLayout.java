/* Copyright (c) 2010 Stanford University
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
 * This class is used to define device-level properties while creating web
 * applications using Fiz. The caller must call the {@code setup} method with
 * the client request object as an argument along with the target device.
 */

public class MobilePhoneLayout {

    /**
     * Construct a MobilePhoneLayout object.
     */
    public MobilePhoneLayout(){}

    /**
     * Setup various device-specific properties.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param device               A string containing the device name.
     *                             This is used to set the value of "device"
     *                             in the main dataset associated with the
     *                             client request object and can thus be
     *                             accessed in other places.
     */
    public void setup(ClientRequest cr, String device){
        Dataset requestData = cr.getMainDataset();
        requestData.set("device", device);
        Html html = cr.getHtml();

        if (device.equals("IPhone")){
            html.appendToHead(" <meta name=\"viewport\" " +
                    "content=\"width=device-width; " +
                    "initial-scale=1.0; maximum-scale=1.0; minimum-scale=1.0; " +
                    "user-scalable=0;\" />");
            html.includeCssFile("IPhoneLayout.css");
            html.includeJsFile("static/fiz/IPhoneLayout.js");
            html.evalJavascript("new Fiz.IPhoneLayout();\n");
        }

        if (device.equals("Android")){
            html.appendToHead(" <meta name=\"viewport\" " +
                    "content=\"width=device-width; " +
                    "initial-scale=1.0; maximum-scale=1.0; minimum-scale=1.0; " +
                    "user-scalable=0;\" />");
            html.includeCssFile("AndroidLayout.css");
            html.includeJsFile("static/fiz/AndroidLayout.js");
            html.evalJavascript("new Fiz.AndroidLayout();\n");
        }
    }
}