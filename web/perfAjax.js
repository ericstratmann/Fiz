// testsPing.js --
//
// This file is used to measure the round-trip time for Ajax requests.

pingCount = 0;
var request = null;

function ping(data)
{
    if (!data) {
        data = "";

    }
    // Browser-specific: create the XMLHttpRequest request object.

    if (window.XMLHttpRequest) {
        request = new XMLHttpRequest();
    } else {
        try {
            request = new ActiveXObject("Msxml2.XMLHTTP");
        } catch(e) {
            try {
                request = new ActiveXObject("Microsoft.XMLHTTP");
            } catch(e) {
                return;
            }
        }
    }

    // Initialize the request and send it out.

    request.onreadystatechange = stateChange;
    request.open("POST", "/fiz/perfAjax/ping");
    request.setRequestHeader("Content-type", "text/plain");
    request.send(data);
}

function stateChange()
{
    if (request.readyState != 4) {
        // Request is still in progress.

        return;
    }
    request.onreadystatechange = null;
    pingCount++;
    if (pingCount < 10) {
        var sum = 0;
        for (var i = 0; i < 0; i++) {
            for (var j = 0; j < 10000; j++) {
               sum += j;
            }
        }
        ping(request.responseText);
    } else {
        var times = request.responseText.split("\n");
        var message = "";
        if (times.length < 12) {
            message ="Raw times:\n" + request.responseText
                + "\nRound-trip times:\n";
            for (var i = 1; i < (times.length-1); i++) {
                message += ((times[i] - times[i-1])/1e6).toFixed(1) + "ms\n";
            }
        }
        var average = (times[times.length-2] - times[0])
                /(1e6*(times.length-2));
        message += "\nAverage time: " + average.toFixed(1) + "ms\n";
        document.getElementById("results").innerHTML
                = message.replace(/\r?\n/g, "<br/>");
    }
}

setTimeout("ping(\"\");", 1000);
