var UploadUtils = {
    uploadUrl: undefined,
    _name: "",

    onNameChange: function(input) {
        this._name = input.value;
    },

    onChange: function(input) {
        var table = document.getElementById('uploadTable');
        var files = input.files;
        this._addFilesToTable(table, files);
    },

    _addFilesToTable: function(table, files) {
        var jobs = [];
        for (var i = 0; i < files.length; i++) {
            var file = files[i];

            var tr = document.createElement("tr");
            table.appendChild(tr);

            var td = document.createElement("td");
            tr.appendChild(td);
            var text = document.createTextNode(file.name);
            td.appendChild(text);

            td = document.createElement("td");
            td.className += " progress";
            tr.appendChild(td);

            var progressBar = this._createProgressBar(file);
            td.appendChild(progressBar);

            var job = {};
            job.file = file;
            job.progressBar = progressBar;
            jobs.push(job);
        }
        this._runJobs(jobs);
    },

    _createProgressBar() {
        var background = document.createElement("div");
        background.className += " progressBackground";

        var label = document.createElement("div");
        label.className += " label progressLabel";
        background.appendChild(label);

        text = document.createTextNode("0%");
        label.appendChild(text);

        var indicator = document.createElement("div");
        indicator.className += " bar progressForeground";
        background.appendChild(indicator);

        return background;
    },

    _runJobs: function(jobs) {
        var that = this;
        var promise = new Promise(function(resolve, reject) {
            that._processJobs(that, jobs);
            resolve();
        });
    },

    _processJobs(that, jobs) {
        while (jobs.length > 0) {
            var job = jobs.shift();
            that._processJob(that, job);
        }
    },

    _processJob(that, job) {
        var formData = new FormData();
        formData.append("name", that._name);
        formData.append("file", job.file);
        Ajax.upload(that.uploadUrl, formData, job.progressBar, that._fileUploadedSuccess, that._fileUploadError);
    },

    _fileUploadedSuccess: function(progressBar) {
        progressBar.getElementsByClassName("label")[0].innerText = "Done";
    },

    _fileUploadError: function(progressBar) {
        progressBar.getElementsByClassName("label")[0].style = "background-color: #c44; color: #ccc;";
        progressBar.getElementsByClassName("label")[0].innerText = "Failed";
    }
}
