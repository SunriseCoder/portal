var DeleteFileHandler = {
    deleteFileUrl: undefined,

    checkBoxesToggle: function(that, pattern) {
        var value = that.checked;
        var checkboxes = $('[id^=' + pattern + ']');
        for (var i = 0; i < checkboxes.length; i++) {
            var checkbox = checkboxes[i];
            checkbox.checked = value;
        }
    },

    resumeDownload: function(id) {
        document.getElementById('fileId').value = id;
        var file = document.getElementById('file');
        file.removeAttribute('multiple');
        file.click();
    },

    deleteFile: function(id, name) {
        var confirmed = confirm('Are You sure to delete file: ' + name + '?');
        if (confirmed) {
            var csrf = document.getElementById("csrf");
            var params = csrf.name + '=' + csrf.value;
            params += '&ids=' + id;
            this._deleteFileOnServer(params, this._deleteFilesSuccess, this._deleteFileError, id);
        }
    },

    deleteSelected: function(that, pattern) {
        var checkboxes = $('[id^=' + pattern + ']');
        var selectedCheckboxes = [];
        for (var i = 0; i < checkboxes.length; i++) {
            var checkbox = checkboxes[i];
            if (checkbox.checked) {
                selectedCheckboxes.push(checkbox);
            }
        }

        var confirmed = confirm('Are You sure to delete ' + selectedCheckboxes.length + ' file(s)?');
        if (!confirmed) {
            return;
        }

        var ids = [];
        for (var i = 0; i < selectedCheckboxes.length; i++) {
            var checkbox = selectedCheckboxes[i];
            var idParts = checkbox.id.split('-');
            var id = idParts.last();
            ids.push(id);
        }

        var csrf = document.getElementById("csrf");
        var params = csrf.name + '=' + csrf.value;
        params += '&ids=' + ids.join(',');
        this._deleteFileOnServer(params, this._deleteFilesSuccess, this._deleteFilesError, ids);
    },

    _deleteFilesSuccess: function(ids) {
        if (!Array.isArray(ids)) {
            DeleteFileHandler._deleteFileFromTable(ids);
            return;
        }

        for (var i = 0; i < ids.length; i++) {
            var id = ids[i];
            DeleteFileHandler._deleteFileFromTable(id);
        }
    },

    _deleteFilesError: function() {
        alert('Could not delete file');
    },

    _deleteFileOnServer: function(params, success, error, id) {
        var xhr = new XMLHttpRequest();
        xhr.open('POST', this.deleteFileUrl, true);
        xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xhr.onload = function () {
            if (xhr.status === 200 && success !== undefined) {
                success(id);
            } else if (error !== undefined) {
                error();
            }
        };
        xhr.send(params);
    },

    _deleteFileFromTable: function(id) {
        var row = document.getElementById('file_' + id);
        if (row != undefined) {
            var parent = row.parentNode;
            parent.removeChild(row);
            var children = $(parent).find('div.uploadedFile');
            if (children.length == 0) {
                parent = parent.parentNode;
                if (row.tagName === 'TR') { //TODO Make same style with the file element box
                    parent = parent.parentNode.parentNode;
                }
                parent.parentNode.removeChild(parent);
            }
        }
    }
}

var PublishFileHandler = {
    publishFileUrl: undefined,
    
    publishSelected: function(that, pattern) {
        var checkboxes = $('[id^=' + pattern + ']');
        var selectedCheckboxes = [];
        for (var i = 0; i < checkboxes.length; i++) {
            var checkbox = checkboxes[i];
            if (checkbox.checked) {
                selectedCheckboxes.push(checkbox);
            }
        }

        var confirmed = confirm('Are You sure to publish ' + selectedCheckboxes.length + ' file(s)?');
        if (!confirmed) {
            return;
        }

        var ids = [];
        for (var i = 0; i < selectedCheckboxes.length; i++) {
            var checkbox = selectedCheckboxes[i];
            var idParts = checkbox.id.split('-');
            var id = idParts.last();
            ids.push(id);
        }

        var csrf = document.getElementById("csrf");
        var params = csrf.name + '=' + csrf.value;
        params += '&ids=' + ids.join(',');
        this._publishFiles(params, this._publishFilesSuccess, this._publishFilesError, ids);
    },

    _publishFilesSuccess: function(ids) {
        if (!Array.isArray(ids)) {
            _deleteFileFromTable(ids);
            return;
        }

        for (var i = 0; i < ids.length; i++) {
            var id = ids[i];
            _deleteFileFromTable(id);
        }

        function _deleteFileFromTable(id) {
            var row = document.getElementById('file_' + id);
            if (row != undefined) {
                var parent = row.parentNode;
                parent.removeChild(row);

                var children = $(parent).find('div.uploadedFile');
                if (children.length == 0) {
                    parent = parent.parentNode;
                    parent.parentNode.removeChild(parent);
                }
            }
        }
    },

    _publishFilesError: function() {
        alert('Could not publish file(s)');
    },

    _publishFiles: function(params, success, error, id) {
        var xhr = new XMLHttpRequest();
        xhr.open('POST', this.publishFileUrl, true);
        xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xhr.onload = function () {
            if (xhr.status === 200 && success !== undefined) {
                success(id);
            } else if (error !== undefined) {
                error();
            }
        };
        xhr.send(params);
    }
}