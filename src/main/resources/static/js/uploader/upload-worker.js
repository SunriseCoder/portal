var uploadChunkUrl = undefined;
var chunkSize = undefined;

var jobs = [];
var csrf = undefined;
var reader = new FileReaderSync();

function loop() {
    if (jobs.length == 0) {
        setTimeout("loop()", 500);
        return;
    }

    var job = jobs[0];
    if (job.uploadDone) {
        postMessage({type: 'uploadDone', job: job});
        jobs.shift();
    } else if (job.cancelled) {
        postMessage({type: 'cancelled', job: job});
        jobs.shift();
    } else if (job.failed) {
        postMessage({type: 'failed', job: job});
        jobs.shift();
    } else {
        processJob(job);
    }
    setTimeout("loop()", 0);
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
    } else if (message.type === 'cancelJob') {
        console.log('upload worker: got cancel job');
        var jobId = message.jobId;
        var job = jobs.find(function(job) { return job.id == jobId});
        if (job !== undefined) {
            job.cancelled = true;
        }
    }
};

function processJob(job) {
    if (job.nextChunk == -1) {
        job.uploadDone = true;
        return;
    }

    var xhr = sendChunk(job);
    var nextChunk = getNextChunkNumber(xhr, job);
    if (nextChunk !== undefined) {
        job.nextChunk = nextChunk;
        updateProgress(job);
    } else {
        job.failed = true;
    }

    function sendChunk(job) {
        var offset = job.nextChunk * chunkSize;
        var toRead = job.file.size >= offset + chunkSize ? chunkSize : job.file.size - offset;
        var chunk = job.file.slice(offset, offset + toRead);

        var formData = new FormData();
        formData.append(csrf.name, csrf.value);
        formData.append('filePlaceHolderId', job.filePlaceHolderId);
        formData.append("chunk", chunk);

        var xhr = new XMLHttpRequest();
        xhr.open('POST', uploadChunkUrl, false);
        xhr.send(formData);

        return xhr;
    }

    function getNextChunkNumber(xhr, job) {
        // Retrieving placeholder's id
        var status = xhr.status;
        if (status != 200) {
            console.log("Server response error");
            var message = {type: 'failed', job: job};
            postMessage(message);
            return;
        }

        var result = JSON.parse(xhr.response);
        if (result.status !== 'Ok') {
            console.log("Chunk sending error: " + result.error);
            var message = {type: 'failed', job: job};
            postMessage(message);
            return;
        }

        var nextChunkNumber = result.response;
        return nextChunkNumber;
    }
}

function updateProgress(job) {
    var message = {type: 'progress', job};
    postMessage(message);
}

loop();
