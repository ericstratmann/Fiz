/* IPhoneTouchScrollableSection.js --
 *
 * This file provides Javascript functions needed to implement the
 * TouchScrollableSection class for an iPhone.
 * One Fiz.IPhoneTouchScrollableSection Javascript object gets created for each
 * TouchScrollableSection Java object created on an iPhone.
 * Methods on the Javascript object are invoked for handling the scrolling of
 * of the section in order to mimic a scrollable list of a native application.
 *
 * Copyright (c) 2010 Stanford University
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
// The following lines are used by Fiz to manage Javascript dependencies.
// Fiz:include static/fiz/Fiz.js

/**
 * Creates an IPhoneTouchScrollableSection object.
 * @param idOut                    Id for the {@code <div>} element
 *                                 that represents the outer container for this
 *                                 section.
 * @param idIn                     Id for the {@code <div>} element
 *                                 that represents the scrollable inner
 *                                 container.
 */
Fiz.IPhoneTouchScrollableSection = function(idOut, idIn) {
    this.idOut = idOut;
    this.idIn = idIn;

    this.startY = null;         // Y co-ordinate of the touchstart event
                                // Units: pixels
                                // 'null' value means it is uninitialized
    this.endY = null;           // Y co-ordinate of the touchend event
                                // Units: pixels
                                // 'null' value means it is uninitialized
    this.startTime = null;      // Time Stamp of the touchstart event
                                // 'null' value means it is uninitialized
    this.endTime = null;        // Time Stamp of the touchend event
                                // 'null' value means it is uninitialized
    this.oldY = null;           // Y co-ordinate of the last touchmove event
                                // Units: pixels
                                // 'null' value means it is uninitialized
    this.oldTime = null;        // Time Stamp of the last touchmove event
                                // 'null' value means it is uninitialized
    this.speed = 0;             // Current speed of the scroll
                                // Units: pixels/ms
    this.acceleration = null;   // Current acceleration of the scroll
                                // Units: pixels/ms (This is interpreted as a
                                // value added or subtracted from the speed
                                // in a given time interval.)
                                // 'null' value means it is uninitialized
    this.timer = null;          // Stores the timer function that produces scrolling
                                // 'null' value means it is uninitialized
    this.timerLastTick = null;  // Stores the time of occurrence of the last
                                // timer function run
                                // 'null' value means it is uninitialized
    this.cancel = false;        // Is a 'click' event on a link possible?
                                // 'false' => 'click' event allowed
                                // 'true' => 'click' event not allowed. This is
                                // set whenever the page is scrolled.

    this.enableScrollOnContent();
    this.disableLinksOnScroll();
}

/**
 * Handler for the {@code touchstart} event on an IPhoneTouchScrollableSection
 * object. The handler records the starting position and time of the touch.
 */
Fiz.IPhoneTouchScrollableSection.prototype.touchStartHandler = function(event){
    var content = document.getElementById(this.idIn);

    // Interrupt scroller if it is already moving
    this.speed = 0;
    this.acceleration = null;
    clearInterval(this.timer);
    this.timerLastTick = null;

    this.startY = event.touches[0].clientY;
    this.startTime = event.timeStamp;
    if (!content.style.top) {
        content.style.top = 0 + "px";
    }
}

/**
 * Handler for the {@code touchmove} event on an IPhoneTouchScrollableSection
 * object. The {@code top} attribute of the scrollable section is adjusted
 * based on finger movement to produce an effect of scrolling with finger drag.
 */
Fiz.IPhoneTouchScrollableSection.prototype.touchMoveHandler = function(event){
    var content = document.getElementById(this.idIn);

    var newY = event.touches[0].clientY;
    var newTime = event.timeStamp;
    this.oldY = this.oldY || this.startY;
    this.oldTime = this.oldTime || this.startTime;

    this.speed = (newY - this.oldY)/(newTime - this.oldTime);

    /*
     * The local variable 'boundary' defines the most negative possible value
     * for the top attribute of the scrollable section. This minimum is
     * attained when the scrollable section is moved all the way up, so that
     * the bottom of the content is visible.
     */
    var boundary = (document.getElementById(this.idOut).offsetHeight -
            content.offsetHeight);

    /*
     * If the finger moves downwards, then the new position of the event
     * is greater than the old one. Since the top of the scrollable section
     * cannot come below the top of its container, we must ensure that
     * 'top' is no greater than zero.
     * If the finger moves upwards, then the new position of the event
     * is smaller than the old one. Since the value is decreasing, we must
     * ensure that it doesn't go below 'boundary'.
     */
    var newTop = parseFloat(content.style.top) + (newY - this.oldY);
    newTop = (newTop > 0) ? 0 : (newTop < boundary) ? boundary : newTop;
    content.style.top = newTop + "px";

    this.oldY = newY;
    this.oldTime = newTime;
    event.preventDefault();
}

/**
 * Handler for the {@code touchend} event on an IPhoneTouchScrollableSection
 * object.
 */
Fiz.IPhoneTouchScrollableSection.prototype.touchEndHandler = function(event){
    var $i = this;
    var content = document.getElementById(this.idIn);

    this.endY = event.changedTouches[0].clientY;
    this.endTime = event.timeStamp;
    var oldTop = parseFloat(content.style.top);

    /*
     * The local variable 'boundary' defines the minimum possible value
     * for the top attribute of the scrollable section. This minimum is
     * attained when the scrollable section is moved all the way up.
     */
    var boundary = (document.getElementById(this.idOut).offsetHeight -
            content.offsetHeight);

    /*
     * We must restrict the speed to a range of values otherwise the
     * scroller will shoot off suddenly if the instantaneous speed of dragging
     * was high just before the finger was lifted.
     */
    var speed = 0 - this.speed;
    this.speed = (speed > 2) ? 2 : (speed < -2) ? -2 : speed;
    if (speed != 0){
        this.acceleration = 0.1 * this.speed;
        this.timerLastTick = new Date().getTime();
        this.timer = setInterval(function(){
            $i.coastToStop(boundary);}, 10);
    }
    this.oldY = null;
    this.oldTime = null;
}

/**
 * This function assigns handlers required to implement the scrolling action
 * of the inner container.
 */
Fiz.IPhoneTouchScrollableSection.prototype.enableScrollOnContent = function() {
    var $i = this;
    var content = document.getElementById(this.idIn);
    content.addEventListener("touchstart", function(e) {
        $i.touchStartHandler(e);
    },false);
    content.addEventListener("touchmove", function(e) {
        $i.touchMoveHandler(e);
    },false);
    content.addEventListener("touchend", function(e) {
        $i.touchEndHandler(e);
    },false);
}

/**
 * Helps to bring the scrollable section to a gradual stop. This is invoked
 * after the finger is flicked an lifted. The caller can use this as a
 * setInterval handler.
 * @param boundary                 Defines a lower bound for the {@top}
 *                                 attribute of the scrollable section.
 *                                 It cannot scroll beyond this. The upper
 *                                 bound is zero.
 */
Fiz.IPhoneTouchScrollableSection.prototype.coastToStop = function(boundary){
    var curTick = new Date().getTime();
    var elapsedTicks = curTick - this.timerLastTick;
    this.timerLastTick = curTick;

    var element = document.getElementById(this.idIn);
    var speed = this.speed;
    var acceleration = this.acceleration;
    var currentTop = parseFloat(element.style.top);

    var newTop = currentTop - speed * elapsedTicks;
    newTop = (newTop <= boundary) ? boundary : (newTop > 0) ? 0 : newTop ;
    element.style.top = newTop + "px";

    /*
     * If the speed becomes lower than the acceleration, or the section reaches
     * its scrollable limit, then the function cleans up and exits.
     */
    if (Math.abs(acceleration) > Math.abs(speed) ||
            newTop == 0 || newTop == boundary) {
        this.speed = 0;
        this.acceleration = null;
        this.timerLastTick = null;
        clearInterval(this.timer);
    }

    /*
     * Otherwise, lower the speed and the acceleration, to achieve a gradual
     * slow down over subsequent runs of this function.
     */
    else{
        this.speed -= this.acceleration;
        this.acceleration -= 0.01 * this.speed;
    }
}

/**
 * This function helps to prevent annoying clicks that may occur on link
 * elements within the page as the user tries to scroll. If a {@code touchmove}
 * event is followed by a {@code click} event, then the default action of
 * the click event is prevented. Otherwise, the action associated with the
 * {@code click} event takes place as usual.
 */
Fiz.IPhoneTouchScrollableSection.prototype.disableLinksOnScroll = function() {
    var content = document.getElementById(this.idIn);
    content.addEventListener("click", function(e) {
        if (this.cancel) {
            e.stopPropagation();
            e.preventDefault();
        }
        this.cancel = false;
    }, true);

    content.addEventListener("touchmove", function(e) {
        this.cancel = true;
    }, false);
}