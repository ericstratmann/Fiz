// AutoScroller.js --
//
// This file implements a JavaScript class that is used when appending
// information to a window; this class will automatically scroll the
// window as new information is added, to keep the end of the window
// in view (but only if it was in view before the new information was
// added.
//
// Copyright (c) 2006-2007 Electric Cloud, Inc.
// All rights reserved

//----------------------------------------------------------------------------
// AutoScroller
//
//      Constructor for AutoScroller objects.  The object starts off
//      empty; properties get filled in externally to give the object
//      its behavior.
//----------------------------------------------------------------------------

function AutoScroller(
    scrollWindow)                // Window element that will be auto-scrolled.
                                 // Defaults to "window".

{
    this.endVisible = null;      // True means that the end of the
                                 // information contained in this window
                                 // was visible the last time "check" was
                                 // called.  In other words, when "update"
                                 // is called, we should scroll the window
                                 // if necessary to keep the new endpoint
                                 // visible.

    this.scrollWindow = scrollWindow;
    if (!this.scrollWindow) {
        this.scrollWindow = window;
    }
    this.check();
}

//----------------------------------------------------------------------------
// check
//
//      This function is invoked automatically by the constructor, and
//      can also be invoked by clients prior to appending data to the
//      window.  It records whether the window is currently scrolled
//      to make the bottom of the information in the window visible.
//----------------------------------------------------------------------------

AutoScroller.prototype.check = function()
{
    var win = this.scrollWindow;
    var doc = win.document;
    if (window.innerHeight) {
        // Firefox

        this.endVisible = (win.scrollY + win.innerHeight)
                >= doc.body.scrollHeight;
    } else {
        // IE

        this.endVisible = (doc.documentElement.scrollTop
                + doc.documentElement.clientHeight) >= doc.body.scrollHeight;
    }
}

//----------------------------------------------------------------------------
// update
//
//      If the last call to "check" determined that the end of the content
//      was visible in the window, then this function will scroll the
//      window to keep the end of the content visible.
//----------------------------------------------------------------------------

AutoScroller.prototype.update = function()
{
    var win = this.scrollWindow;
    var doc = win.document;
    if (!this.endVisible) {
        return;
    }
    if (win.innerHeight) {
        // Firefox

        if ((win.scrollY + win.innerHeight) < doc.body.scrollHeight) {
            win.scrollTo(win.scrollX,
                    doc.body.scrollHeight - win.innerHeight);
        }
    } else {
        // IE

        if ((doc.documentElement.scrollTop + doc.documentElement.clientHeight)
                < doc.body.scrollHeight) {
            win.scrollTo(doc.documentElement.scrollLeft,
                    doc.body.scrollHeight - doc.documentElement.clientHeight);
        }
    }
}
