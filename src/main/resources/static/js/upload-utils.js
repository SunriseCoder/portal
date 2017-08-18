var UploadUtils = {
    uploadUrl: undefined,
    _transferFilesWorker: undefined,
    _jobCounter: 0,

    onChange: function(input) {
        if (typeof(Worker) !== "undefined") {
            if (typeof(this._transferFilesWorker) == "undefined") {
                this._transferFilesWorker = new Worker("js/transfer-files-worker.js");

                this._transferFilesWorker.onmessage = this._updateStatus;

                var csrf = document.getElementById("csrf");
                var csrfMessage = {type: 'csrf', name: csrf.name, value: csrf.value};
                this._transferFilesWorker.postMessage(csrfMessage)
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
            this._transferFilesWorker.postMessage(job);
        }

        //this._runJobs(jobs);
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

    _updateStatus: function(event) {
        var message = event.data;
        if (message.type === 'checksum') {
            // TODO update progress bar with checksum update info ("checksum: XX%")
            // And color = maybe blue or so
        } else if (message.type === 'upload') {
            // TODO update progress bar with upload update info ("upload: XX%")
            // And color = maybe green or so
        } else if (message.type === 'failed') {
            UploadUtils._setUploadError(message.id);
        } else if (message.type === 'done') {
            // TODO change status to done via UploadUtils._setUploadDone(message.id);
        }
        console.log(message);
    },

    _setUploadError: function(id) {
        var label = document.getElementById('label' + id);
        label.style = "background-color: #c44; color: #ccc;";
        label.innerText = "Failed";
    },
// TODO cut off rest after it will be completely replaced with new code
    _runJobs: function(jobs) {
        this._processJobs(jobs);
    },

    _processJobs(jobs) {
        while (jobs.length > 0) {
            var job = jobs.shift();
            this._processJob(job);
        }
    },

    _processJob(job) {
        var formData = new FormData();
        var csrf = document.getElementById("csrf");
        formData.append(csrf.name, csrf.value);
        formData.append("file", job.file);
        HttpUtils.upload(this.uploadUrl, formData, job.progressBar, this._fileUploadedSuccess, this._fileUploadError);
    },

    _fileUploadedSuccess: function(progressBar) {
        progressBar.getElementsByClassName("label")[0].innerText = "Done";
    },

    _fileUploadError: function(progressBar) {
        progressBar.getElementsByClassName("label")[0].style = "background-color: #c44; color: #ccc;";
        progressBar.getElementsByClassName("label")[0].innerText = "Failed";
    }
}
