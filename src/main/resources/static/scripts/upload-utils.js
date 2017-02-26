var UploadUtils = {
    uploadUrl: undefined,
    _name: "",

    onNameChange: function(input) {
        var name = input.value;
        this._name = name;
        CookieUtils.put("upload-name", name);
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
        formData.append("name", this._name);
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
