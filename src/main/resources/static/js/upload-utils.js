var UploadUtils = {
    uploadUrl: undefined,
    _transferFilesWorker: undefined,
    _jobCounter: 0,

    onClick: function(input) {
        var fileId = document.getElementById('fileId');
        fileId.value = undefined;

        var file = document.getElementById('file');
        file.multiple = 'multiple';
        file.click();
    },

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
            // TODO try to use old loader in this case
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
        var id = message.id;
        if (message.type === 'checksum') {
            var percent = message.percent;
            var bar = document.getElementById('bar' + id);
            bar.classList.add('progressChecksumForeground');
            bar.style = "width: " + percent + "%;";
            var label = document.getElementById('label' + id);
            label.innerText = 'Checksum: ' + percent.toFixed(2) + "%";
            if (percent > 100) {
                console.log(1);
            }
        } else if (message.type === 'upload') {
            var percent = message.percent;
            var bar = document.getElementById('bar' + id);
            bar.classList.add('progressUploadForeground');
            bar.style = "width: " + percent + "%;";
            var label = document.getElementById('label' + id);
            label.innerText = 'Upload: ' + percent.toFixed(2) + "%";
        } else if (message.type === 'failed') {
            var label = document.getElementById('label' + id);
            label.classList.add("errorLabel");
            label.innerText = "Failed";
        } else if (message.type === 'done') {
            var label = document.getElementById('label' + id);
            label.classList.add("doneLabel");
            label.innerText = "Done";
        }
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
