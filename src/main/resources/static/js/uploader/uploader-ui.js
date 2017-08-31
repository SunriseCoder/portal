// UploadTable

var UploadTableProto = Object.create(HTMLElement.prototype);
var UploadTable = document.registerElement('upload-table', { prototype: UploadTableProto, extends: 'table' });

UploadTable.prototype.setJobProgress = function(jobId, stage, percent) {
    var element = this._findByJobId(jobId);
    if (element !== undefined) {
        element._setJobProgress(stage, percent);
    }
};

UploadTable.prototype.setJobCheckSumDone = function(jobId) {
    var element = this._findByJobId(jobId);
    if (element !== undefined) {
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

                '<div class="info1">' +
                    '<div class="titleGroup">Title: <input class="titleInput" /></div>' +
                    '<div class="dateGroup">Date: <input class="dateInput" size="10" /></div>' +
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

UploadElement.prototype.jobId = undefined;
UploadElement.prototype._filename = undefined;

Object.defineProperty(UploadElement.prototype, 'filename', {
    get: function() {
        return this._filename;
    },

    set: function(newValue) {
        this._filename = newValue;
        var element = $(this).find('div.filename')[0];
        element.innerText = this._filename;
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

    this._cancelButton.remove();
}

UploadElement.prototype._setJobCancelled = function() {
    var label = $(this).find('div.progressLabel')[0];
    Array.addIfNotExists(label.classList, 'errorLabel');
    label.innerText = "Cancelled";

    this._cancelButton.remove();
    this._restoreRetryButton();
}

UploadElement.prototype._setJobFailed = function() {
    var label = $(this).find('div.progressLabel')[0];
    Array.addIfNotExists(label.classList, 'errorLabel');
    label.innerText = "Failed";

    this._cancelButton.remove();
    this._restoreRetryButton();
}

UploadElement.prototype._restoreCancelButton = function() {
    var cancelButtonDiv = $(this).find('div.cancelButtonDiv')[0];
    cancelButtonDiv.appendChild(this._cancelButton);
}

UploadElement.prototype._restoreRetryButton = function() {
    var cancelButtonDiv = $(this).find('div.cancelButtonDiv')[0];
    cancelButtonDiv.appendChild(this._retryButton);
}
