var UploadUtils = {
    onChange: function(input) {
        var table = document.getElementById('uploadTable');
        var files = input.files;
        this._addFilesToTable(table, files);
    },

    _addFilesToTable: function(table, files) {
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

            var progressBar = this._createProgressBar();
            td.appendChild(progressBar);
        }
    },

    _createProgressBar() {
        var background = document.createElement("div");
        background.className += " progressBackground";

        var label = document.createElement("div");
        label.className += " progressLabel";
        background.appendChild(label);

        text = document.createTextNode("0%");
        text.className = "label";
        label.appendChild(text);

        var indicator = document.createElement("div");
        indicator.className = "bar";
        indicator.className += " progressForeground";
        background.appendChild(indicator);

        return background;
    },

    //TODO Remove after multiupload is ready
    stub: function() {
        var table = document.getElementById('uploadTable');
        var files = [];
        files.push({name:"folder-size-question.png"});
        files.push({name:"another-fake-graphic.png"});
        this._addFilesToTable(table, files);
    }
}
