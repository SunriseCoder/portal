// UploadTable

var UploadTableProto = Object.create(HTMLElement.prototype);
var UploadTable = document.registerElement('upload-table', { prototype: UploadTableProto, extends: 'table' });

UploadTable.prototype.setJobProgress = function(jobId, stage, percent) {
    var element = this._findByJobId(jobId);
    if (element !== undefined) {
        element._setJobProgress(stage, percent);
    }
};

UploadTable.prototype.setJobCheckSumDone = function(job) {
    var jobId = job.id;
    var element = this._findByJobId(jobId);
    if (element !== undefined) {
        element.job = job;
        element._setJobCheckSumDone();
    }
}

UploadTable.prototype.setJobCancelled = function(jobId) {
    var element = this._findByJobId(jobId);
    if (element !== undefined) {
        element._setJobCancelled();
    }
}

UploadTable.prototype.setJobFailed = function(jobId) {
    var element = this._findByJobId(jobId);
    if (element !== undefined) {
        element._setJobFailed();
    }
}

UploadTable.prototype.setJobUploadDone = function(jobId) {
    var element = this._findByJobId(jobId);
    if (element !== undefined) {
        element._setJobUploadDone();
    }
}

UploadTable.prototype._findByJobId = function(jobId) {
    for (var i = 0; i < this.children.length; i++) {
        var element = this.children[i];
        if (element.jobId == jobId) {
            return element;
        }
    }
};

// UploadElement

var UploadElementProto = Object.create(HTMLElement.prototype);

UploadElementProto.createdCallback = function() {
    this.innerHTML =
        '<tr><td>' +
            '<div class="uploadBlock">' +
                '<div class="filename"></div>' +

                '<div class="uploadLine">' +
                    '<div class="progressBackground">' +
                        '<div class="progressLabel">0%</div>' +
                        '<div class="progressBar progressForeground"></div>' +
                    '</div>' +
                    '<div class="cancelButtonDiv">' +
                        '<button class="cancelButton">Cancel</button>' +
                        '<button class="retryButton">Retry</button>' +
                    '</div>' +
                '</div>' +

                '<div class="infoLine">' +
                    '<div class="titleGroup">Title: <input class="titleInput" /></div>' +
                '</div>' +
                '<div class="infoLine">' +
                    '<div class="dateGroup">' +
                        'Date: <input class="dateInput" size="10" pattern="[0-9?]{2}\.[0-9?]{2}\.[0-9?]{4}" placeholder="E.g. 25.??.1998" />' +
                    '</div>' +
                    '<div class="positionGroup">' +
                        'Position: <input class="positionInput" size="3" pattern="[0-9?]+" placeholder="E.g. 1" />' +
                    '</div>' +
                '</div>' +
                '<div class="infoLine">' +
                    '<div class="saveButtonDiv">' +
                        '<button class="saveButton">Save</button>' +
                    '</div>' +
                    '<div class="publishButtonDiv">' +
                        '<button class="publishButton">Publish</button>' +
                    '</div>' +
                '</div>' +
            '</div>' +
        '</td></tr>';

    UploadElementProto._cancelButton = $(this).find('button.cancelButton')[0];
    UploadElementProto._cancelButton.onclick = function(event) {
        var confirmed = confirm('Are You sure to cancel upload?');
        if (!confirmed) {
            return;
        }

        var uploadElement = UploadElement.findUploadElement(this);
        var jobId = uploadElement.jobId;
        Uploader.cancelJob(jobId);

        uploadElement._setJobCancelled();
    }

    UploadElementProto._retryButton = $(this).find('button.retryButton')[0];
    UploadElementProto._retryButton.onclick = function(event) {
        var uploadElement = UploadElement.findUploadElement(this);
        var jobId = uploadElement.jobId;
        var resumed = Uploader.retryJob(jobId);

        if (resumed) {
            uploadElement._restoreCancelButton();
            this.remove();
        }
    }
    UploadElementProto._retryButton.remove();

    UploadElementProto._saveButton = $(this).find('button.saveButton')[0];
    UploadElementProto._saveButton.onclick = function(event) {
        var uploadElement = UploadElement.findUploadElement(this);
        var csrf = document.getElementById("csrf");
        var job = uploadElement.job;
        var params = csrf.name + '=' + csrf.value +
                '&id=' + job.filePlaceHolderId +
                '&title=' + $(uploadElement).find('input.titleInput')[0].value +
                '&date=' + $(uploadElement).find('input.dateInput')[0].value +
                '&position=' + $(uploadElement).find('input.positionInput')[0].value;
        var url = Uploader.saveFileInfoUrl;
        HttpUtils.post(url, params, true, saveOk, saveError, this);

        function saveOk(event) {
            console.log(1);
            // TODO make circle - save -> saved -> changed -> save button is active again
        }

        function saveError(event) {
            console.log(2);
            // TODO sign somehow that the error has been occurred
        }
    }
};

var UploadElement = document.registerElement('upload-element', { prototype: UploadElementProto, extends: 'tr' });

UploadElement.findUploadElement = function(element) {
    while (element !== undefined) {
        if (element.constructor.name === 'upload-element') {
            return element;
        }
        element = element.parentElement;
    }
}

UploadElement.prototype.job = undefined;
UploadElement.prototype.jobId = undefined;
UploadElement.prototype._filename = undefined;

Object.defineProperty(UploadElement.prototype, 'filename', {
    get: function() {
        return this._filename;
    },

    set: function(filename) {
        this._filename = filename;
        var element = $(this).find('div.filename')[0];
        element.innerText = this._filename;

        var filenameWithoutExt = filename.substr(0, filename.lastIndexOf('.'));
        var re = /^([0-9]{8})[_]*([0-9]*)[\s\-\_]*(.*)$/;
        var groups = filenameWithoutExt.match(re);
        if (groups !== null) {
            var date = groups[1];
            date = date.substr(6, 2) + '.' + date.substr(4, 2) + '.' + date.substr(0, 4);
            $(this).find('input.dateInput')[0].value = date;
            var position = groups[2];
            if (position != '') {
                while (position.startsWith('0') && position.length > 1) {
                    position = position.substr(1, position.length - 1);
                }
                $(this).find('input.positionInput')[0].value = position;
            }
            var title = groups[3];
            $(this).find('input.titleInput')[0].value = title;
        }
    }
});

UploadElement.prototype._setJobProgress = function(stage, percent) {
    var bar = $(this).find('div.progressBar')[0];
    var label = $(this).find('div.progressLabel')[0];

    Array.removeIfExists(label.classList, 'errorLabel');

    if (stage === 'checksum') {
        Array.removeIfExists(bar.classList, 'progressUploadForeground');
        Array.addIfNotExists(bar.classList, 'progressChecksumForeground');
        labelText = 'Checksum: ';
    } else if (stage === 'upload') {
        Array.removeIfExists(bar.classList, 'progressChecksumForeground');
        Array.addIfNotExists(bar.classList, 'progressUploadForeground');
        var labelText = 'Upload: ';
    } else {
        return;
    }

    bar.style = "width: " + percent + "%;";
    label.innerText = labelText + percent.toFixed(2) + "%";
}

UploadElement.prototype._setJobCheckSumDone = function() {
    var label = $(this).find('div.progressLabel')[0];
    Array.removeIfExists(label.classList, 'errorLabel');
    label.innerText = "Waiting for Upload";
}

UploadElement.prototype._setJobUploadDone = function() {
    var label = $(this).find('div.progressLabel')[0];
    Array.removeIfExists(label.classList, 'errorLabel');
    label.innerText = "Done";

    this._removeCancelButton();
}

UploadElement.prototype._setJobCancelled = function() {
    var label = $(this).find('div.progressLabel')[0];
    Array.addIfNotExists(label.classList, 'errorLabel');
    label.innerText = "Cancelled";

    this._removeCancelButton();
    this._restoreRetryButton();
}

UploadElement.prototype._setJobFailed = function() {
    var label = $(this).find('div.progressLabel')[0];
    Array.addIfNotExists(label.classList, 'errorLabel');
    label.innerText = "Failed";

    this._removeCancelButton();
    this._restoreRetryButton();
}

UploadElement.prototype._removeCancelButton = function() {
    var button = $(this).find('button.cancelButton')[0];
    button.remove();
}

UploadElement.prototype._restoreCancelButton = function() {
    var cancelButtonDiv = $(this).find('div.cancelButtonDiv')[0];
    cancelButtonDiv.appendChild(this._cancelButton);
}

UploadElement.prototype._removeRetryButton = function() {
    var button = $(this).find('button.retryButton')[0];
    button.remove();
}

UploadElement.prototype._restoreRetryButton = function() {
    var cancelButtonDiv = $(this).find('div.cancelButtonDiv')[0];
    cancelButtonDiv.appendChild(this._retryButton);
}
