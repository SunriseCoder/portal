var Uploader = {
    checkSumWorkerUrl: undefined,
    chunkUploadWorkerUrl: undefined,
    createFilePlaceHolderUrl: undefined,
    uploadChunkUrl: undefined,
    _checkSumWorker: undefined,
    _chunkUploadWorker: undefined,
    _jobCounter: 0,

    selectFiles: function(input) {
        var fileId = document.getElementById('fileId');
        fileId.value = undefined;

        var file = document.getElementById('file');
        file.multiple = 'multiple';
        file.click();
    },

    uploadFiles: function(input) {
        if (typeof(Worker) !== "undefined") {
            var csrf = document.getElementById("csrf");
            var csrfMessage = {type: 'csrf', name: csrf.name, value: csrf.value};

            if (typeof(this._checkSumWorker) == 'undefined') {
                this._checkSumWorker = new Worker(this.checkSumWorkerUrl);
                this._checkSumWorker.onmessage = this._checkSumWorkerMessage;
                this._checkSumWorker.postMessage(csrfMessage);
                this._checkSumWorker.postMessage({type: 'createFilePlaceHolderUrl', url: this.createFilePlaceholderUrl});
            }

            if (typeof(this._chunkUploadWorker) == 'undefined') {
                this._chunkUploadWorker = new Worker(this.chunkUploadWorkerUrl);
                this._chunkUploadWorker.onmessage = this._chunkUploadWorkerMessage;
                this._chunkUploadWorker.postMessage(csrfMessage);
                this._chunkUploadWorker.postMessage({type: 'uploadChunkUrl', url: this.uploadChunkUrl})
            }

            var table = document.getElementById('uploadTable');
            var files = input.files;
            this._addFilesToTable(table, files);
        } else {
            alert('Sorry, Your browser does not support workers, file upload is impossible');
        }
    },

    _addFilesToTable: function(table, files) {
        for (var i = 0; i < files.length; i++) {
            var file = files[i];
            var jobId = this._jobCounter++;

            var tr = document.createElement("tr");
            table.appendChild(tr);

            var td = document.createElement("td");
            tr.appendChild(td);
            var text = document.createTextNode(file.name);
            td.appendChild(text);

            td = document.createElement("td");
            td.className += " progress";
            tr.appendChild(td);

            var progressBar = this._createProgressBar(jobId);
            td.appendChild(progressBar);

            var job = {};
            job.type = 'job';
            job.id = jobId;
            job.file = file;
            var fileId = document.getElementById('fileId').value;
            if (fileId !== 'undefined') {
                job.fileId = fileId;
            }
            this._checkSumWorker.postMessage(job);
        }
    },

    _createProgressBar(id) {
        var background = document.createElement("div");
        background.className += " progressBackground";

        var label = document.createElement("div");
        label.id = 'label' + id;
        label.className += " label progressLabel";
        background.appendChild(label);

        text = document.createTextNode("0%");
        label.appendChild(text);

        var indicator = document.createElement("div");
        indicator.id = 'bar' + id;
        indicator.className += " bar progressForeground";
        background.appendChild(indicator);

        return background;
    },

    _checkSumWorkerMessage: function(event) {
        var message = event.data;
        var id = message.id;
        if (message.type === 'progress') {
            var percent = message.percent;
            Uploader._setJobProgress(id, percent, 'progressChecksumForeground', 'Checksum: ');
        } else if (message.type === 'failed') {
            Uploader._setJobFailed(id);
        } else if (message.type === 'done') {
            var job = message.job;
            Uploader._chunkUploadWorker.postMessage(job);
            Uploader._setJobCheckSumDone(id);
        }
    },

    _chunkUploadWorkerMessage: function(event) {
        var message = event.data;
        var id = message.id;
        if (message.type === 'progress') {
            var percent = message.percent;
            Uploader._setJobProgress(id, percent, 'progressUploadForeground', 'Upload: ');
        } else if (message.type === 'failed') {
            Uploader._setJobFailed(id);
        } else if (message.type === 'done') {
            Uploader._setJobUploadDone(id);
        }
    },

    _setJobProgress: function(id, percent, barClass, labelText) {
        var bar = document.getElementById('bar' + id);
        bar.classList.add(barClass);
        bar.style = "width: " + percent + "%;";
        var label = document.getElementById('label' + id);
        label.innerText = labelText + percent.toFixed(2) + "%";
    },

    _setJobFailed: function(id) {
        var label = document.getElementById('label' + id);
        label.classList.add("errorLabel");
        label.innerText = "Failed";
    },

    _setJobCheckSumDone: function(id) {
        var label = document.getElementById('label' + id);
        label.innerText = "Waiting for Upload";
    },

    _setJobUploadDone: function(id) {
        var label = document.getElementById('label' + id);
        label.innerText = "Done";
    }
}
