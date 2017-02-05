var FileTree = {
    // Settings
    htmlNode: undefined,
    ajaxUrl: undefined,
    downloadUrl: undefined,

    // Ajax data
    serverData: undefined,

    // Filtering data
    filterText: undefined,
    filteredData: undefined,
    displayLimit: 100,

    // Counters
    displayLimitCounter: 0,
    totalFilesCount: 0,
    totalFoldersCount: 0,
    filteredFilesCount: 0,
    filteredFoldersCount: 0,

    setHtmlNode: function(id) {
        var node = $("#" + id);
        this.htmlNode = node;
    },

    build: function() {
        this.refresh();
    },

    refresh: function() {
        Ajax.call(this, this.ajaxUrl, this._onAjaxSuccess, this._onAjaxError);
    },

    _onAjaxSuccess: function(object, data) {
        var response = data.response;
        object.serverData = JSON.parse(response);

        object._processServerData(object.serverData, "/");

        object.applyFilter();
    },

    _processServerData: function(folder, url) {
        this.totalFoldersCount++;

        folder.size = 0;

        var folderUrl = this._encodeUrl(url);
        folder.url = folderUrl;

        for (var i = 0; i < folder.folders.length; i++) {
            var nextFolder = folder.folders[i];
            nextFolder.parent = folder;

            var nextUrl = url + nextFolder.name + "/";

            this._processServerData(nextFolder, nextUrl);

            folder.size += nextFolder.size;
        }

        for (var i = 0; i < folder.files.length; i++) {
            var file = folder.files[i];

            file.parent = folder;

            var fileUrl = url + file.name;
            fileUrl = this._encodeUrl(fileUrl);
            file.url = fileUrl;

            file.readableSize = this._getReadableSize(file.size);
            folder.size += file.size;

            this.totalFilesCount++;
        }

        folder.readableSize = this._getReadableSize(folder.size);
    },

    _getReadableSize(size) {
        var unit = 1024;
        if (size < unit) {
            return size + " B";
        }

        var exp = Math.log(size) / Math.log(unit);
        exp = Math.floor(exp);
        var suffix = "kMGTPE".charAt(exp - 1);
        var notation = size / Math.pow(unit, exp);
        var result = notation.toFixed(1) + " " + suffix + "B";
        return result;
    },

    _encodeUrl: function(url) {
        url = encodeURIComponent(url);
        var encoded = btoa(url);
        encoded = encoded.replace("/", "_");
        url = this.downloadUrl + encoded; 
        return url;
    },

    applyFilter: function() {
        var filter = $('#filter')[0].value;
        this.filter = filter.toLowerCase().trim();

        if (this.serverData == undefined) {
            return;
        }

        this._applyFilter();

        this.displayLimitCounter = 0;
        var nodes = [];
        var tree = this.serverData;
        this._buildTreeRecursively(tree, nodes, 0);

        var htmlText = this._makeHtmlText(nodes);

        this.htmlNode.html(htmlText);
    },
    
    applyLimit: function() {
        var limit = $('#limit')[0].value;
        this.displayLimit = limit;
        this.applyFilter();
    },
    
    _onAjaxError: function(object, data) {
        Locales.write("tree", "loading.error");
    },

    _buildTreeRecursively: function(folder, nodes, indent) {
        if (this.displayLimit > 0 && this.displayLimit <= this.displayLimitCounter) {
            return;
        }

        if (folder.passedFilter) {
            //Add Folder
            var element = {};
            element.indent = indent;
            element.name = folder.name;
            element.isFolder = true;
            element.readableSize = folder.readableSize;
            element.url = folder.url;
            nodes.push(element);
        }

        for (var i = 0; i < folder.folders.length; i++) {
            //Scan Recursively
            var nextFolder = folder.folders[i];
            this._buildTreeRecursively(nextFolder, nodes, indent + 1);
        }
        var index = 0;
        for (var i = 0; i < folder.files.length; i++) {
            if (this.displayLimit > 0 && this.displayLimit <= this.displayLimitCounter) {
                return;
            }
            var file = folder.files[i];
            if (file.passedFilter) {
                //Add File
                var element = {};
                element.indent = indent + 1;
                element.name = file.name;
                element.isFile = true;
                element.index = index++;
                element.readableSize = file.readableSize;
                element.url = file.url;
                nodes.push(element);
                this.displayLimitCounter++;
            }
        }
    },

    _makeHtmlText: function(nodes) {
        var htmlText = "Total: " + this.totalFilesCount + " file(s) and " + this.totalFoldersCount + " folder(s)<br />";
        htmlText += "Found: " + this.filteredFilesCount + " file(s) and " + this.filteredFoldersCount + " folder(s)<br />";

        for (var i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            htmlText += '<div style="position: relative; left: ' + node.indent * 20 + 'px;">';
            if (node.isFolder) {
                htmlText += '<a href="' + node.url + '"><img src="/icons/folder.png" />' + node.name + '</a>';
                htmlText += " (" + node.readableSize + ")";
            } else if (node.isFile) {
                htmlText += '<a href="' + node.url + '"><img src="/icons/file.png" />' + node.name + '</a>';
                htmlText += " (" + node.readableSize + ")";
            }
            htmlText += '</div>';
        }
        return htmlText;
    },

    _applyFilter: function() {
        // Mark each elements in the Tree as non-passed 
        this._resetFiltered(this.serverData);
        // Scan and match each element
        this._filterData(this.serverData);
    },

    _resetFiltered: function(folder) {
        this.filteredFilesCount = 0,
        this.filteredFoldersCount = 0,

        folder.passedFilter = false;
        for (var i = 0; i < folder.folders.length; i++) {
            var nextFolder = folder.folders[i];
            this._resetFiltered(nextFolder);
        }
        for (var i = 0; i < folder.files.length; i++) {
            var file = folder.files[i];
            file.passedFilter = false;
        }
    },

    _filterData: function(folder) {
        if (this._match(folder)) {
            this._markBranchPassedFilter(folder);
            this._markChildrenPassedFilter(folder);
            return;
        }

        for (var i = 0; i < folder.folders.length; i++) {
            var nextFolder = folder.folders[i];
            this._filterData(nextFolder);
        }

        for (var i = 0; i < folder.files.length; i++) {
            var file = folder.files[i];
            if (this._match(file)) {
                file.passedFilter = true;
                this.filteredFilesCount++;
                if (!folder.passedFilter) {
                    this.filteredFoldersCount++;
                    this._markBranchPassedFilter(folder);
                }
            }
        }
    },

    // TODO here should be complex matcher for entries, maybe like name, hash-tags and so on.
    // Maybe also separated algorithms for files/lectures and folders/labels
    _match: function(value) {
        var result;

        if (this.filter) {
            var name = value.name;
            var lowered = name.toLowerCase();
            result = lowered.includes(this.filter);
        } else {
            result = true;
        }

        return result;
    },

    _markBranchPassedFilter: function(folder) {
        if (!folder.passedFilter) {
            folder.passedFilter = true;
            this.filteredFoldersCount++;            
        }

        if (folder.parent) {
            var parent = folder.parent;
            this._markBranchPassedFilter(parent);
        }
    },

    _markChildrenPassedFilter: function(folder) {
        for (var i = 0; i < folder.folders.length; i++) {
            var nextFolder = folder.folders[i];
            if (!nextFolder.passedFilter) {
                nextFolder.passedFilter = true;
                this.filteredFoldersCount++;
            }
            this._markChildrenPassedFilter(nextFolder);
        }

        for (var i = 0; i < folder.files.length; i++) {
            var file = folder.files[i];
            if (!file.passedFilter) {
                file.passedFilter = true;
                this.filteredFilesCount++;
            }
        }
    }
}
