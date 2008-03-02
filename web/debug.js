// debug.js --
//
// This file contains miscellaneous functions used for debugging
// JavaScript code.
//
// Copyright (c) 2005-2006 Electric Cloud, Inc.
// All rights reserved

//----------------------------------------------------------------------------
// log
//
//      Display diagnostic information in the current document.
//
// Results:
//      None.
//
// Side Effects:
//      "String" gets displayed in a separate "Debug Log" window, which
//      is created during the first call this function.
//----------------------------------------------------------------------------

function log(
    string,                       // HTML text to display.  A line break
                                  // will automatically be generated in
                                  // the log after this text.
    replace,                      // True means delete any existing log
                                  // information before displaying "string".
                                  // False means append "string" to existing
                                  // log info.
    pre)                          // True means display the text in a
                                  // <pre> element; false means display
                                  // it as normal paragraph text.
{
    if (!log.element || log.window.closed) {
        // Create the window.
        // While creating the window, we have to insert delays at a couple
        // of points in order to give the browser enough time to create
        // previous stuff before we use it to create the next stuff.
        // This is done by scheduling additional procedures to run later.
        // Note that there could potentially be several calls to this
        // function before the window is finally initialized; we save all
        // of the arguments for all of the calls in the array log.calls.

        if (!log.createInProgress && (!log.window || log.window.closed)) {
            delete log.element;
            log.calls = new Array;
            
            // It is possible that the window.open call below could cause
            // a recursive call to this method (with log.window not yet
            // set).  The variable log.createInProgress keeps us from
            // creating another window in this case.
            
            log.createInProgress = true;
            log.window = window.open("", "debugLog");
            log.createInProgress = false;
            setTimeout("log.finishCreation();", 50);
        }
        log.calls.push({string:string, replace:replace, pre:pre});
        return;
    }
    if (replace) {
        log.element.innerHTML = "";
    }
    scroller = new AutoScroller(log.window);
    if (pre) {
        var newElement = log.window.document.createElement("pre");
        newElement.appendChild(log.window.document.createTextNode(string));
        log.element.appendChild(newElement);
    } else {
        log.element.appendChild(log.window.document.createTextNode(string));
        log.element.appendChild(log.window.document.createElement("br"));
    }
    scroller.update();
}

//----------------------------------------------------------------------------
// log.finishCreation (private)
// log.finishCreation2 (private)
//
//      These functions are used to complete the initialization of the log
//      window after a suitable delay to allow the window to get created.
//      See comments above for the reason why this function is needed.
//
// Results:
//      None.
//
// Side Effects:
//      Initialization gets finished for the log window, and all of the
//      accumulated log messages are displayed.
//----------------------------------------------------------------------------

log.finishCreation = function()
{
    log.window.document.write(
            "<html>\n" +
                "<head>\n" +
                    "<title>Debug Log</title>\n" +
                "</head>\n" +
                "<body>\n" +
                    "<h1>Debug Log</h1>\n" +
                    "<p id=\"debugLog\"/></p>\n" +
                "</body>\n" +
            "</html>\n");
    log.window.document.close();
    setTimeout("log.finishCreation2();", 50);
}

log.finishCreation2 = function()
{
    log.element = log.window.document.getElementById("debugLog");
    for (var i = 0; i < log.calls.length; i++) {
        log(log.calls[i].string, log.calls[i].replace, log.calls[i].pre);
    }
    delete log.calls;
}
