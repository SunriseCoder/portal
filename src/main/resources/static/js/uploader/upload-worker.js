var uploadChunkUrl = undefined;
var chunkSize = undefined;

var jobs = [];
var csrf = undefined;
var reader = new FileReaderSync();

function loop() {
    console.log("upload worker: checking job"); // TODO cut off
    var job = jobs.shift();
    if (job !== undefined) {
        processJob(job);
        setTimeout("loop()", 0);
    } else {
        setTimeout("loop()", 500);
    }
}

onmessage = function(event) {
    var message = event.data;
    if (message.type === 'job') {
        console.log('upload worker: adding job: ' + message.id);
        jobs.push(message);
    } else if (message.type === 'csrf') {
        console.log('upload worker: got csrf');
        csrf = message;
    } else if (message.type === 'uploadChunkUrl') {
        console.log('upload worker: got uploadChunkUrl');
        uploadChunkUrl = message.url;
    } else if (message.type === 'chunkSize') {
        console.log('upload worker: got chunkSize');
        chunkSize = message.value;
    }
};

function processJob(job) {
    var file = job.file;
    var filePlaceHolderId = job.filePlaceHolderId;
    var nextChunk = job.nextChunk;
    while (nextChunk != -1) {
        var xhr = sendChunk(file, nextChunk, filePlaceHolderId);
        nextChunk = getNextChunkNumber(xhr, job);
        if (nextChunk !== undefined) {
            job.nextChunk = nextChunk;
            var percentDone = nextChunk != -1 ? nextChunk * chunkSize * 100 / file.size : 100;
            var message = {type: 'progress', id: job.id, percent: percentDone};
            postMessage(message);
        } else {
            return;
        }
    }

    // Reporting that the job is done
    var message = {type: 'done', id: job.id};
    postMessage(message);
    return;

    function sendChunk(file, nextChunk, filePlaceHolderId) {
        var offset = nextChunk * chunkSize;
        var toRead = file.size >= offset + chunkSize ? chunkSize : file.size - offset;
        var chunk = file.slice(offset, offset + toRead);

        var formData = new FormData();
        formData.append(csrf.name, csrf.value);
        formData.append('filePlaceHolderId', filePlaceHolderId);
        formData.append("chunk", chunk);

        var xhr = new XMLHttpRequest();
        xhr.open('POST', uploadChunkUrl, false);
        xhr.send(formData);

        return xhr;
    }

    function getNextChunkNumber(xhr, job) {
        // And retrieving that placeholder's id
        var status = xhr.status;
        if (status != 200) {
            console.log("Server response error");
            var message = {type: 'failed', id: job.id};
            postMessage(message);
            return;
        }

        var result = JSON.parse(xhr.response);
        if (result.status !== 'Ok') {
            console.log("Chunk sending error: " + result.error);
            var message = {type: 'failed', id: job.id};
            postMessage(message);
            return;
        }

        var nextChunkNumber = result.response;
        return nextChunkNumber;
    }
}

loop();
