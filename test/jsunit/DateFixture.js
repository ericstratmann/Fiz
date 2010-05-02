// DateFixture.js --
//
// This file provides a dummy partial implementation of the Date
// DOM class, for use in tests.  There's just enough functionality
// here to test existing Javascript code.
//
// Copyright (c) 2010 Stanford University
// Permission to use, copy, modify, and distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

/**
 * Constructor for Date objects.
 */

function Date(timeValue){
    if (timeValue){
        this.time = timeValue;
    }
    else{
        this.time = 20;
    }
}

/**
 * Simply returns the time and increments it so that a different value is
 * returned on the next call.
 * @return                         The present value of the time property of
 *                                 this date object.
 */
Date.prototype.getTime = function() {
    var presentTime = this.time;
    this.time = this.time + 10;
    return presentTime;
}