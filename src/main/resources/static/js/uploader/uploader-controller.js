var UploaderController = {
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
        this._deleteFileOnServer(params, this._deleteFilesSuccess, this._deleteFileError, ids);
    },

    _deleteFilesSuccess: function(ids) {
        if (!Array.isArray(ids)) {
            UploaderController._deleteFileFromTable(ids);
            return;
        }

        for (var i = 0; i < ids.length; i++) {
            var id = ids[i];
            UploaderController._deleteFileFromTable(id);
        }
    },

    _deleteFileError: function() {
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
        var row = document.getElementById('file' + id);
        if (row != undefined) {
            var parent = row.parentNode;
            parent.removeChild(row);
            if (parent.children.length == 0) {
                parent.parentNode.style.visibility = 'hidden';
            }
        }
    }
}
