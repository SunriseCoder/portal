var sessionHeartbeatUrl = undefined;
var timeout = undefined; // In milliseconds

function loop() {
    console.log(new Date() + 'Session heartbeat');
    var xhr = new XMLHttpRequest();
    xhr.open("GET", sessionHeartbeatUrl, true);
    xhr.send();

    setTimeout("loop()", timeout);
}

onmessage = function(event) {
    var message = event.data;
    if (message.type === 'sessionHeartbeatUrl') {
        sessionHeartbeatUrl = message.value;
    } else if (message.type === 'timeout') {
        timeout = message.value;
    } else if (message.type === 'start') {
        setTimeout("loop()", 0);
    }
}
