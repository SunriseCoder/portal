var Uploader = {
    chunkSize: 4194304, // 4Mb
    checkSumWorkerUrl: undefined,
    chunkUploadWorkerUrl: undefined,
    createFilePlaceHolderUrl: undefined,
    uploadChunkUrl: undefined,
    deleteFileUrl: undefined,

    _uploadTable: undefined,
    _checkSumWorker: undefined,
    _chunkUploadWorker: undefined,
    _jobCounter: 0,
    _failedJobs: [],

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
                this._checkSumWorker.postMessage({type: 'chunkSize', value: this.chunkSize});
            }

            if (typeof(this._chunkUploadWorker) == 'undefined') {
                this._chunkUploadWorker = new Worker(this.chunkUploadWorkerUrl);
                this._chunkUploadWorker.onmessage = this._chunkUploadWorkerMessage;
                this._chunkUploadWorker.postMessage(csrfMessage);
                this._chunkUploadWorker.postMessage({type: 'uploadChunkUrl', url: this.uploadChunkUrl})
                this._chunkUploadWorker.postMessage({type: 'chunkSize', value: this.chunkSize});
            }

            if (this._uploadTable == undefined) {
                var uploadTableDiv = document.getElementById('uploadTable');
                this._uploadTable = new UploadTable();
                uploadTableDiv.appendChild(this._uploadTable);
            }

            var files = input.files;
            this._addFilesToTable(files);
        } else {
            alert('Sorry, Your browser does not support workers, file upload is impossible');
        }
    },

    _addFilesToTable: function(files) {
        for (var i = 0; i < files.length; i++) {
            var file = files[i];
            var jobId = this._jobCounter++;

            var uploadElement = new UploadElement();
            uploadElement.jobId = jobId;
            uploadElement.filename = file.name;
            this._uploadTable.appendChild(uploadElement);

            // Creating job object
            var job = {};
            job.type = 'job';
            job.id = jobId;
            job.file = file;
            var fileId = document.getElementById('fileId').value;
            if (fileId !== 'undefined') {
                job.fileId = fileId;
            }
            uploadElement.job = job;
            this._checkSumWorker.postMessage(job);
        }
    },

    cancelJob: function(jobId) {
        var message = {type: 'cancelJob', jobId: jobId};
        Uploader._checkSumWorker.postMessage(message);
        Uploader._chunkUploadWorker.postMessage(message);
    },

    retryJob: function(jobId) {
        var finder = function(element) { return element.id == jobId; };
        var job = Uploader._failedJobs.findFirstAndPop(finder);
        if (job === undefined) {
            return false;
        }

        if (job.cancelled && job.filePlaceHolderId !== undefined) {
            delete job.filePlaceHolderId;
            job.checkSumDone = false;
        }

        delete job.cancelled;
        delete job.failed;

        if (job.filePlaceHolderId === undefined) {
            Uploader._checkSumWorker.postMessage(job);
        } else {
            Uploader._chunkUploadWorker.postMessage(job);
        }

        return true;
    },

    _checkSumWorkerMessage: function(event) {
        var message = event.data;
        var job = message.job;
        var id = job.id;
        if (message.type === 'progress') {
            var percent = job.offset * 100 / job.file.size;
            Uploader._uploadTable.setJobProgress(id, 'checksum', percent);
        } else if (message.type === 'cancelled') {
            Uploader._uploadTable.setJobCancelled(id);
        } else if (message.type === 'failed') {
            Uploader._failedJobs.push(job);
            Uploader._uploadTable.setJobFailed(id);
        } else if (message.type === 'checkSumDone') {
            Uploader._uploadTable.setJobCheckSumDone(job);
            Uploader._chunkUploadWorker.postMessage(job);
        }
    },

    _chunkUploadWorkerMessage: function(event) {
        var message = event.data;
        var job = message.job;
        var id = job.id;
        if (message.type === 'progress') {
            var percent = job.nextChunk != -1 ? job.nextChunk * Uploader.chunkSize * 100 / job.file.size : 100;
            Uploader._uploadTable.setJobProgress(id, 'upload', percent);
        } else if (message.type === 'cancelled') {
            Uploader._uploadTable.setJobCancelled(id);
            Uploader._deleteFileFromServer(message.job.filePlaceHolderId);
        } else if (message.type === 'failed') {
            Uploader._failedJobs.push(job);
            Uploader._uploadTable.setJobFailed(id);
        } else if (message.type === 'uploadDone') {
            Uploader._uploadTable.setJobUploadDone(id);
        }
    },

    _deleteFileFromServer(filePlaceHolderId) {
        var csrf = document.getElementById("csrf");
        var params = csrf.name + '=' + csrf.value;
        params += '&ids=' + filePlaceHolderId;
        HttpUtils.post(Uploader.deleteFileUrl, params, true);
    }
}
