/* IPhoneTouchScrollableSectionTest.js --
 *
 * Jsunit tests for IPhoneTouchScrollableSection.js, organized in the standard 
 * fashion.
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
 * ACTION OF CONTRACT, NEGLIGENCE OR otherFields TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

include("static/fiz/Fiz.js");
include("static/fiz/IPhoneTouchScrollableSection.js");
include("DateFixture.js");


IPhoneTouchScrollableSectionTest = {};

IPhoneTouchScrollableSectionTest.test_constructor = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    
    assertEqual("content", scrollSection.idIn, "Id of the inner scroller");
    assertEqual("container", scrollSection.idOut, "Id of the container");
    assertEqual(null, scrollSection.startY, 
            "Y co-ordinate of the touchstart event");
    assertEqual(null, scrollSection.endY, 
            "Y co-ordinate of the touchend event");
    assertEqual(null, scrollSection.startTime, 
            "Time Stamp of the touchstart event");
    assertEqual(null, scrollSection.endTime, 
            "Time Stamp of the touchend event");
    assertEqual(null, scrollSection.oldY, 
            "Y co-ordinate of the last touchmove event");
    assertEqual(null, scrollSection.oldTime, 
            "Time Stamp of the last touchmove event");
    assertEqual(0, scrollSection.speed, "Current speed of the scroll");
    assertEqual(null, scrollSection.acceleration, 
            "Current acceleration of the scroll");
    assertEqual(null, scrollSection.timer, 
            "Timer function that produces scrolling");
    assertEqual(null, scrollSection.timerLastTick, 
            "Time of occurrence of the last timer function run");
}

IPhoneTouchScrollableSectionTest.test_touchStartHandler = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    
    var myEvent = new Object();
    myEvent.touches = new Array();
    var touchEvent = new Object();
    touchEvent.clientY = 10;
    myEvent.touches.push(touchEvent);
    myEvent.timeStamp = 20;
    scrollSection.timer = setInterval(function(){alert("hello world");}, 11);
    jsunit.log = "";
    scrollSection.touchStartHandler(myEvent);
    
    assertEqual(0, scrollSection.speed, "Current speed of the scroll");
    assertEqual(null, scrollSection.acceleration, 
            "Current acceleration of the scroll");
    assertEqual("clearInterval(\nfunction () {\n" +
            "    alert(\"hello world\");\n" +
            "}\n, 11)\n", jsunit.log, "Clear Interval");
    assertEqual(null, scrollSection.timerLastTick, 
            "Time of occurrence of the last timer function run");
    assertEqual("0px", content.style.top, "Top of the scroller")
    assertEqual(10, scrollSection.startY, "Touch Start position");
    assertEqual(20, scrollSection.startTime, "Touch Start time");
}

IPhoneTouchScrollableSectionTest.test_touchMoveHandler_basics = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    content.offsetHeight = 500;
    container.offsetHeight = 100;
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    scrollSection.oldY = 10;
    scrollSection.oldTime = 20;
    content.style.top = "-50px";
    
    var moveEvent = new Object();
    moveEvent.preventDefault = function(){}
    moveEvent.touches = new Array();
    var touchMoveEvent = new Object();
    touchMoveEvent.clientY = 30;
    moveEvent.touches.push(touchMoveEvent);
    moveEvent.timeStamp = 40;
    var speed = (touchMoveEvent.clientY - scrollSection.oldY) / 
       (moveEvent.timeStamp - scrollSection.oldTime);
   
    scrollSection.touchMoveHandler(moveEvent);
    
    assertEqual(40, scrollSection.oldTime, "oldTime value");
    assertEqual(30, scrollSection.oldY, "oldY value");
    assertEqual(speed, scrollSection.speed, "Scrolling speed");
    assertEqual("-30px", content.style.top, "New Top Value");
}

IPhoneTouchScrollableSectionTest.test_touchMoveHandler_oldYoldTimeNotDefined = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    content.offsetHeight = 500;
    container.offsetHeight = 100;
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    scrollSection.startY = 10;
    scrollSection.startTime = 20;

    content.style.top = "-50px";
    
    var moveEvent = new Object();
    moveEvent.preventDefault = function(){}
    moveEvent.touches = new Array();
    var touchMoveEvent = new Object();
    touchMoveEvent.clientY = 30;
    moveEvent.touches.push(touchMoveEvent);
    moveEvent.timeStamp = 40;
   
    var speed = (touchMoveEvent.clientY - scrollSection.startY) / 
       (moveEvent.timeStamp - scrollSection.startTime);
    scrollSection.touchMoveHandler(moveEvent);
    
    assertEqual(40, scrollSection.oldTime, "oldTime value");
    assertEqual(30, scrollSection.oldY, "oldY value");
    assertEqual(speed, scrollSection.speed, "Scrolling speed");
    assertEqual("-30px", content.style.top, "New Top Value");
}

IPhoneTouchScrollableSectionTest.test_touchMoveHandler_newTopAboveLimit = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    content.offsetHeight = 500;
    container.offsetHeight = 100;
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    scrollSection.oldY = 10;
    scrollSection.oldTime = 20;
    content.style.top = "-10px";
    
    var moveEvent = new Object();
    moveEvent.preventDefault = function(){}
    moveEvent.touches = new Array();
    var touchMoveEvent = new Object();
    touchMoveEvent.clientY = 30;
    moveEvent.touches.push(touchMoveEvent);
    moveEvent.timeStamp = 40;
    scrollSection.touchMoveHandler(moveEvent);
    var speed = (touchMoveEvent.clientY - scrollSection.startY) / 
        (moveEvent.timeStamp - scrollSection.startTime);
    
    assertEqual("0px", content.style.top, "New Top Value");
}

IPhoneTouchScrollableSectionTest.test_touchMoveHandler_newTopBelowBoundary = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    content.offsetHeight = 500;
    container.offsetHeight = 100;
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    scrollSection.oldY = 30;
    scrollSection.oldTime = 20;
    content.style.top = "-390px";
    
    var moveEvent = new Object();
    moveEvent.preventDefault = function(){}
    moveEvent.touches = new Array();
    var touchMoveEvent = new Object();
    touchMoveEvent.clientY = 10;
    moveEvent.touches.push(touchMoveEvent);
    moveEvent.timeStamp = 40;
    scrollSection.touchMoveHandler(moveEvent);
    var speed = (touchMoveEvent.clientY - scrollSection.startY) / 
        (moveEvent.timeStamp - scrollSection.startTime);
    
    assertEqual("-400px", content.style.top, "New Top Value");
}

IPhoneTouchScrollableSectionTest.test_touchEndHandler_basics = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.offsetHeight = 500;
    container.offsetHeight = 100;
    content.listeners = new Array();
    content.style.top = "-20px";
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    
    var endEvent = new Object();
    endEvent.changedTouches = new Array();
    var touchEndEvent = new Object();
    touchEndEvent.clientY = 10;
    endEvent.changedTouches.push(touchEndEvent);
    endEvent.timeStamp = 20;
    
    scrollSection.speed = 1;
    scrollSection.touchEndHandler(endEvent);
 
    assertEqual(10, scrollSection.endY, "Touch end position");
    assertEqual(20, scrollSection.endTime, "Touch end time");
    assertEqual(-0.1, scrollSection.acceleration, "Acceleration");
    assertEqual("setInterval(\nfunction () {\n" +
            "    $i.coastToStop(boundary);\n" +
            "}\n, 10)\n", jsunit.log, "Timer function");
    assertEqual(null, scrollSection.oldY, "Clear up last touch-move position");
    assertEqual(null, scrollSection.oldTime, "Clear up last touch-move time");
}

IPhoneTouchScrollableSectionTest.test_touchEndHandler_speedGreaterThanTwo = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.offsetHeight = 500;
    container.offsetHeight = 100;
    content.listeners = new Array();
    content.style.top = "-20px";
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    
    var endEvent = new Object();
    endEvent.changedTouches = new Array();
    var touchEndEvent = new Object();
    touchEndEvent.clientY = 10;
    endEvent.changedTouches.push(touchEndEvent);
    endEvent.timeStamp = 20;
    
    scrollSection.speed = 10;
    scrollSection.touchEndHandler(endEvent);
 
    assertEqual(-0.2, scrollSection.acceleration, "Acceleration");
    assertEqual("setInterval(\nfunction () {\n" +
            "    $i.coastToStop(boundary);\n" +
            "}\n, 10)\n", jsunit.log, "Timer function");
}

IPhoneTouchScrollableSectionTest.test_touchEndHandler_speedLessThanMinusTwo = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.offsetHeight = 500;
    container.offsetHeight = 100;
    content.listeners = new Array();
    content.style.top = "-20px";
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    
    var endEvent = new Object();
    endEvent.changedTouches = new Array();
    var touchEndEvent = new Object();
    touchEndEvent.clientY = 10;
    endEvent.changedTouches.push(touchEndEvent);
    endEvent.timeStamp = 20;
    
    scrollSection.speed = -11;
    scrollSection.touchEndHandler(endEvent);
 
    assertEqual(0.2, scrollSection.acceleration, "Acceleration");
    assertEqual("setInterval(\nfunction () {\n" +
            "    $i.coastToStop(boundary);\n" +
            "}\n, 10)\n", jsunit.log, "Timer function");
}

IPhoneTouchScrollableSectionTest.test_touchEndHandler_zeroSpeed = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.offsetHeight = 500;
    container.offsetHeight = 100;
    content.listeners = new Array();
    content.style.top = "-20px";
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    
    var endEvent = new Object();
    endEvent.changedTouches = new Array();
    var touchEndEvent = new Object();
    touchEndEvent.clientY = 10;
    endEvent.changedTouches.push(touchEndEvent);
    endEvent.timeStamp = 20;
    
    scrollSection.speed = 0;
    scrollSection.touchEndHandler(endEvent);
 
    assertEqual(null, scrollSection.acceleration, "Acceleration");
    assertEqual("", jsunit.log, "Timer function");
}

IPhoneTouchScrollableSectionTest.test_enableScrollOnContent = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    content.style.top = "-10px";
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    scrollSection.speed = 1;
    
    assertEqual("\nfunction (e) {\n" +
            "    $i.touchStartHandler(e);\n" +
            "}\n", content.listeners["touchstart"][0].toString(), 
            "Touch Start Handler");

    assertEqual("\nfunction (e) {\n" +
            "    $i.touchMoveHandler(e);\n" +
            "}\n", content.listeners["touchmove"][0].toString(), 
            "Touch Move Handler");

    assertEqual("\nfunction (e) {\n" +
            "    $i.touchEndHandler(e);\n" +
            "}\n", content.listeners["touchend"][0].toString(), 
            "Touch End Handler");
}

IPhoneTouchScrollableSectionTest.test_coastToStop_basics = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    content.style.top = "-10px";
    
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    
    scrollSection.speed = 1;
    scrollSection.acceleration = 0.1;
    scrollSection.timerLastTick = 10;
    
    scrollSection.coastToStop(-100);
    
    assertEqual(0.9, scrollSection.speed, "Present Speed");
    assertEqual(0.091, scrollSection.acceleration, "Present Acceleration");
    assertEqual(20, scrollSection.timerLastTick, "Last Tick");
    assertEqual("-20px", content.style.top, "New Top Value");
}

IPhoneTouchScrollableSectionTest.test_coastToStop_newTopAboveLimit = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    content.style.top = "-10px";
    
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    
    scrollSection.speed = -1;
    scrollSection.acceleration = -0.1;
    scrollSection.timerLastTick = 0;
    scrollSection.timer = setInterval(function(){alert("hello world");}, 11);
    jsunit.log = "";
    
    scrollSection.coastToStop(-100);
    
    assertEqual(0, scrollSection.speed, "Present Speed");
    assertEqual(null, scrollSection.acceleration, "Present Acceleration");
    assertEqual(null, scrollSection.timerLastTick, "Last Tick");
    assertEqual("clearInterval(\nfunction () {\n" +
            "    alert(\"hello world\");\n" +
            "}\n, 11)\n", jsunit.log, "Clear Interval");
    assertEqual("0px", content.style.top, "New Top Value");
}

IPhoneTouchScrollableSectionTest.test_coastToStop_newTopBelowBoundary = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    content.style.top = "-95px";
    
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    
    scrollSection.speed = 1;
    scrollSection.acceleration = 0.1;
    scrollSection.timerLastTick = 0;
    scrollSection.timer = setInterval(function(){alert("hello world");}, 11);
    jsunit.log = "";
    scrollSection.coastToStop(-100);
    
    assertEqual(0, scrollSection.speed, "Present Speed");
    assertEqual(null, scrollSection.acceleration, "Present Acceleration");
    assertEqual(null, scrollSection.timerLastTick, "Last Tick");
    assertEqual("clearInterval(\nfunction () {\n" +
            "    alert(\"hello world\");\n" +
            "}\n, 11)\n", jsunit.log, "Clear Interval");
    assertEqual("-100px", content.style.top, "New Top Value");
}

IPhoneTouchScrollableSectionTest.test_coastToStop_accelerationGreaterThanSpeed = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    content.style.top = "-30px";
    
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    
    scrollSection.speed = 0.09;
    scrollSection.acceleration = 0.1;
    scrollSection.timerLastTick = 0;
    scrollSection.timer = setInterval(function(){alert("hello world");}, 11);
    jsunit.log = "";
    scrollSection.coastToStop(-100);
    
    assertEqual(0, scrollSection.speed, "Present Speed");
    assertEqual(null, scrollSection.acceleration, "Present Acceleration");
    assertEqual(null, scrollSection.timerLastTick, "Last Tick");
    assertEqual("clearInterval(\nfunction () {\n" +
            "    alert(\"hello world\");\n" +
            "}\n, 11)\n", jsunit.log, "Clear Interval");
    assertEqual("-31.8px", content.style.top, "New Top Value");
}

IPhoneTouchScrollableSectionTest.test_disableLinksOnScroll = function(){
    var container = document.addElementWithId("container");
    var content = document.addElementWithId("content");
    content.listeners = new Array();
    scrollSection = new Fiz.IPhoneTouchScrollableSection("container", "content");
    
    assertEqual("\nfunction (e) {\n" +
            "    if (this.cancel) {\n" +
            "        e.stopPropagation();\n" +
            "        e.preventDefault();\n" +
            "    }\n" +
            "    this.cancel = false;\n" +
            "}\n", content.listeners["click"][0].toString(), 
            "Click Handler");

    assertEqual("\nfunction (e) {\n" +
            "    this.cancel = true;\n" +
            "}\n", content.listeners["touchmove"][1].toString(), 
            "Touch Move Handler to disable links");
}