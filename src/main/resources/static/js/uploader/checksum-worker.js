var createFilePlaceHolderUrl = undefined;
var chunkSize = undefined;

var jobs = [];
var csrf = undefined;
var reader = new FileReaderSync();

function loop() {
    console.log("checksum worker: checking for a job"); // TODO cut off logging?
    if (jobs.length == 0) {
        setTimeout("loop()", 500);
        return;
    }

    var job = jobs[0];
    if (job.checkSumDone) {
        postMessage({type: 'checkSumDone', id: job.id, job: job});
        jobs.shift();
    } else if (job.cancelled) {
        postMessage({type: 'cancelled', id: job.id});
        jobs.shift();
    } else if (job.failed) {
        postMessage({type: 'failed', id: job.id});
        jobs.shift();
    } else {
        processJob(job);
    }
    setTimeout("loop()", 0);
}

onmessage = function(event) {
    var message = event.data;
    if (message.type === 'job') {
        console.log('checksum worker: adding job: ' + message.id);
        jobs.push(message);
    } else if (message.type === 'csrf') {
        console.log('checksum worker: got csrf');
        csrf = message;
    } else if (message.type === 'createFilePlaceHolderUrl') {
        console.log('checksum worker: got createFilePlaceHolderUrl');
        createFilePlaceHolderUrl = message.url;
    } else if (message.type === 'chunkSize') {
        console.log('checksum worker: got chunkSize');
        chunkSize = message.value;
    } else if (message.type === 'cancelJob') {
        console.log('checksum worker: got cancel job');
        var jobId = message.jobId;
        var job = jobs.find(function(job) { return job.id == jobId});
        if (job !== undefined) {
            job.cancelled = true;
        }
    }
};

function processJob(job) {
    if (job.md5sums === undefined) {
        job.md5sums = [];
        job.offset = 0;
    }

    if (job.offset < job.file.size) {
        // Hash next chunk
        calculateChunk(job);
        updateProgress(job);
        return;
    }

    // Create file placeholder
    createFilePlaceHolder(job);
    if (job.filePlaceHolderId === undefined) {
        job.failed = true;
    } else {
        job.checkSumDone = true;
    }
}

function calculateChunk(job) {
    var toRead = job.file.size - job.offset;
    if (toRead >= chunkSize) {
        var sum = md5sum(job.file, job.offset, chunkSize);
        job.md5sums = job.md5sums.concat(sum);
        job.offset += chunkSize;
    } else {
        var sum = md5sum(job.file, job.offset, toRead);
        job.md5sums = job.md5sums.concat(sum);
        job.offset += toRead;
    }
    return;

    function md5sum(file, offset, length) {
        var chunk = file.slice(offset, offset + length);
        var arrayBuffer = reader.readAsArrayBuffer(chunk);
        var array = Md5.arrayBufferToArray(arrayBuffer);
        var lengthBits = length * 8;
        var sum = Md5.md5(array, lengthBits);
        return sum;
    }
}

function updateProgress(job) {
    var percent = job.offset * 100 / job.file.size;
    var message = {type: 'progress', id: job.id, percent: percent};
    postMessage(message);
}

function createFilePlaceHolder(job) {
    // Sums of chunks and final digest
    var finalSum = finalMd5sum(job.md5sums);
    job.md5sums = job.md5sums.concat(finalSum);
    var checkSum = Md5.array32ToString(job.md5sums);

    // Creating placeholder on the server side
    var params = csrf.name + '=' + csrf.value;
    if (job.fileId !== undefined) {
        params += '&fileId=' + job.fileId;
    }
    params += '&name=' + job.file.name;
    params += '&size=' + job.file.size;
    params += '&chunkSize=' + chunkSize;
    params += '&checkSum=' + checkSum;
    var xhr = new XMLHttpRequest();
    xhr.open("POST", createFilePlaceHolderUrl, false);
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhr.send(params);

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
        console.log("Create file placeholder error: " + result.error);
        var message = {type: 'failed', id: job.id};
        postMessage(message);
        return;
    }

    job.filePlaceHolderId = result.response.placeHolderId;
    job.nextChunk = result.response.nextChunk;
    return;

    function finalMd5sum(array) {
        var lengthBits = array.length * 32;
        var sum = Md5.md5(array, lengthBits);
        return sum;
    }
}

loop();

var Md5 = {
    arrayBufferToArray: function(arrayBuffer) {
        var size = arrayBuffer.byteLength;
        if (size % 4 != 0) {
            var toFill = 4 - size % 4;
            var array8bit = new Int8Array(arrayBuffer);
            var adjustedArray = new Int8Array(size + toFill);
            for (var i = 0; i < size; i++) {
                adjustedArray[i] = array8bit[i];
            }
            arrayBuffer = adjustedArray.buffer;
        }
        var array32bit = new Int32Array(arrayBuffer);
        // TODO investigate, why Int32Array doesn't work
        var result = [];
        for (var i = 0; i < array32bit.length; i++) {
            result[i] = array32bit[i];
        }
        return result;
    },

    array32ToString: function(input) {
        var hexTab = '0123456789abcdef';
        var output = '';
        var length32 = input.length * 32;
        for (var i = 0; i < length32; i += 8) {
            var charCode = (input[i >> 5] >>> (i % 32)) & 0xFF;
            output += hexTab.charAt((charCode >>> 4) & 0x0F);
            output += hexTab.charAt(charCode & 0x0F);
        }
        return output
    },

    stringToArray32: function(input) {
        var output = [];
        output[(input.length >> 2) - 1] = undefined;
        for (var i = 0; i < output.length; i += 1) {
            output[i] = 0;
        }
        var length8 = input.length * 8;
        for (i = 0; i < length8; i += 8) {
            output[i >> 5] |= (input.charCodeAt(i / 8) & 0xFF) << (i % 32);
        }
        return output;
    },

    md5: function(input, len) {
        x = input.slice();
        x[len >> 5] |= 0x80 << (len % 32);
        x[(((len + 64) >>> 9) << 4) + 14] = len;

        var a = 1732584193;
        var b = -271733879;
        var c = -1732584194;
        var d = 271733878;

        for (var i = 0; i < x.length; i += 16) {
            var olda = a;
            var oldb = b;
            var oldc = c;
            var oldd = d;

            a = md5ff(a, b, c, d, x[i], 7, -680876936);
            d = md5ff(d, a, b, c, x[i + 1], 12, -389564586);
            c = md5ff(c, d, a, b, x[i + 2], 17, 606105819);
            b = md5ff(b, c, d, a, x[i + 3], 22, -1044525330);
            a = md5ff(a, b, c, d, x[i + 4], 7, -176418897);
            d = md5ff(d, a, b, c, x[i + 5], 12, 1200080426);
            c = md5ff(c, d, a, b, x[i + 6], 17, -1473231341);
            b = md5ff(b, c, d, a, x[i + 7], 22, -45705983);
            a = md5ff(a, b, c, d, x[i + 8], 7, 1770035416);
            d = md5ff(d, a, b, c, x[i + 9], 12, -1958414417);
            c = md5ff(c, d, a, b, x[i + 10], 17, -42063);
            b = md5ff(b, c, d, a, x[i + 11], 22, -1990404162);
            a = md5ff(a, b, c, d, x[i + 12], 7, 1804603682);
            d = md5ff(d, a, b, c, x[i + 13], 12, -40341101);
            c = md5ff(c, d, a, b, x[i + 14], 17, -1502002290);
            b = md5ff(b, c, d, a, x[i + 15], 22, 1236535329);

            a = md5gg(a, b, c, d, x[i + 1], 5, -165796510);
            d = md5gg(d, a, b, c, x[i + 6], 9, -1069501632);
            c = md5gg(c, d, a, b, x[i + 11], 14, 643717713);
            b = md5gg(b, c, d, a, x[i], 20, -373897302);
            a = md5gg(a, b, c, d, x[i + 5], 5, -701558691);
            d = md5gg(d, a, b, c, x[i + 10], 9, 38016083);
            c = md5gg(c, d, a, b, x[i + 15], 14, -660478335);
            b = md5gg(b, c, d, a, x[i + 4], 20, -405537848);
            a = md5gg(a, b, c, d, x[i + 9], 5, 568446438);
            d = md5gg(d, a, b, c, x[i + 14], 9, -1019803690);
            c = md5gg(c, d, a, b, x[i + 3], 14, -187363961);
            b = md5gg(b, c, d, a, x[i + 8], 20, 1163531501);
            a = md5gg(a, b, c, d, x[i + 13], 5, -1444681467);
            d = md5gg(d, a, b, c, x[i + 2], 9, -51403784);
            c = md5gg(c, d, a, b, x[i + 7], 14, 1735328473);
            b = md5gg(b, c, d, a, x[i + 12], 20, -1926607734);

            a = md5hh(a, b, c, d, x[i + 5], 4, -378558);
            d = md5hh(d, a, b, c, x[i + 8], 11, -2022574463);
            c = md5hh(c, d, a, b, x[i + 11], 16, 1839030562);
            b = md5hh(b, c, d, a, x[i + 14], 23, -35309556);
            a = md5hh(a, b, c, d, x[i + 1], 4, -1530992060);
            d = md5hh(d, a, b, c, x[i + 4], 11, 1272893353);
            c = md5hh(c, d, a, b, x[i + 7], 16, -155497632);
            b = md5hh(b, c, d, a, x[i + 10], 23, -1094730640);
            a = md5hh(a, b, c, d, x[i + 13], 4, 681279174);
            d = md5hh(d, a, b, c, x[i], 11, -358537222);
            c = md5hh(c, d, a, b, x[i + 3], 16, -722521979);
            b = md5hh(b, c, d, a, x[i + 6], 23, 76029189);
            a = md5hh(a, b, c, d, x[i + 9], 4, -640364487);
            d = md5hh(d, a, b, c, x[i + 12], 11, -421815835);
            c = md5hh(c, d, a, b, x[i + 15], 16, 530742520);
            b = md5hh(b, c, d, a, x[i + 2], 23, -995338651);

            a = md5ii(a, b, c, d, x[i], 6, -198630844);
            d = md5ii(d, a, b, c, x[i + 7], 10, 1126891415);
            c = md5ii(c, d, a, b, x[i + 14], 15, -1416354905);
            b = md5ii(b, c, d, a, x[i + 5], 21, -57434055);
            a = md5ii(a, b, c, d, x[i + 12], 6, 1700485571);
            d = md5ii(d, a, b, c, x[i + 3], 10, -1894986606);
            c = md5ii(c, d, a, b, x[i + 10], 15, -1051523);
            b = md5ii(b, c, d, a, x[i + 1], 21, -2054922799);
            a = md5ii(a, b, c, d, x[i + 8], 6, 1873313359);
            d = md5ii(d, a, b, c, x[i + 15], 10, -30611744);
            c = md5ii(c, d, a, b, x[i + 6], 15, -1560198380);
            b = md5ii(b, c, d, a, x[i + 13], 21, 1309151649);
            a = md5ii(a, b, c, d, x[i + 4], 6, -145523070);
            d = md5ii(d, a, b, c, x[i + 11], 10, -1120210379);
            c = md5ii(c, d, a, b, x[i + 2], 15, 718787259);
            b = md5ii(b, c, d, a, x[i + 9], 21, -343485551);

            a = safeAdd(a, olda);
            b = safeAdd(b, oldb);
            c = safeAdd(c, oldc);
            d = safeAdd(d, oldd);
        }

        return [a, b, c, d];

        function safeAdd (x, y) {
            var lsw = (x & 0xFFFF) + (y & 0xFFFF);
            var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
            return (msw << 16) | (lsw & 0xFFFF);
        }

        function bitRotateLeft (num, cnt) {
            return (num << cnt) | (num >>> (32 - cnt));
        }

        function md5cmn (q, a, b, x, s, t) {
            var aq = safeAdd(a, q);
            var xt = safeAdd(x, t);
            var aqxt = safeAdd(aq, xt);
            var rotated = bitRotateLeft(aqxt, s);
            var result = safeAdd(rotated, b);
            return result;
        }

        function md5ff (a, b, c, d, x, s, t) {
            var q = (b & c) | ((~b) & d);
            var result = md5cmn(q, a, b, x, s, t);
            return result;
        }

        function md5gg (a, b, c, d, x, s, t) {
            var q = (b & d) | (c & (~d));
            var result = md5cmn(q, a, b, x, s, t);
            return result;
        }

        function md5hh (a, b, c, d, x, s, t) {
            var q = b ^ c ^ d;
            var result = md5cmn(q, a, b, x, s, t);
            return result;
        }

        function md5ii (a, b, c, d, x, s, t) {
            var q = c ^ (b | (~d));
            var result = md5cmn(q, a, b, x, s, t);
            return result;
        }
    }
}
