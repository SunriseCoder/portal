<%@ page import="app.util.NumberUtils" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="restFilesRoot" value="${appRoot}/rest/files" />
<c:set var="uploaderRoot" value="${appRoot}/js/uploader" />

<!DOCTYPE html>
<html>
<head>
    <title>Upload</title>

    <link rel="stylesheet" href="${appRoot}/css/upload.css">

    <script src="${appRoot}/js/prototypes.js"></script>
    <script src="${appRoot}/js/http-utils.js"></script>
    <script src="${appRoot}/js/cookie-utils.js"></script>
    <script src="${appRoot}/js/file-tree.js"></script>
    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>
    <script src="${uploaderRoot}/uploader.js"></script>

    <script>
        var deleteFileUrl = "${restFilesRoot}/delete";

        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("upload.caption");

            Uploader.checkSumWorkerUrl = "${uploaderRoot}/checksum-worker.js"
            Uploader.chunkUploadWorkerUrl = "${uploaderRoot}/upload-worker.js";
            Uploader.createFilePlaceholderUrl = "${restFilesRoot}/create";
            Uploader.uploadChunkUrl = "${restFilesRoot}/upload-chunk";
            Uploader.deleteFileUrl = "${restFilesRoot}/delete";
        });

        function resumeDownload(id) {
            document.getElementById('fileId').value = id;
            var file = document.getElementById('file');
            file.removeAttribute('multiple');
            file.click();
        }

        function deleteFile(id, name) {
            var confirmed = confirm('Are You sure to delete file: ' + name + '?');
            if (confirmed) {
                var csrf = document.getElementById("csrf");
                var params = csrf.name + '=' + csrf.value;
                params += '&ids=' + id;
                deleteFileOnServer(deleteFileUrl, params, deleteFilesSuccess, deleteFileError, id);
            }
        }

        function deleteFilesSuccess(ids) {
            if (!Array.isArray(ids)) {
                deleteFileFromTable(ids);
                return;
            }

            for (var i = 0; i < ids.length; i++) {
                var id = ids[i];
                deleteFileFromTable(id);
            }
        }

        function deleteFileFromTable(id) {
            var row = document.getElementById('file' + id);
            if (row != undefined) {
                var parent = row.parentNode;
                parent.removeChild(row);
                if (parent.children.length == 0) {
                    parent.parentNode.style.visibility = 'hidden';
                }
            }
        }

        function deleteFileError() {
            alert('Could not delete file');
        }

        function deleteFileOnServer(url, params, success, error, id) {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', url, true);
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

        function checkBoxesToggle(that, pattern) {
            var value = that.checked;
            var checkboxes = $('[id^=' + pattern + ']');
            for (var i = 0; i < checkboxes.length; i++) {
                var checkbox = checkboxes[i];
                checkbox.checked = value;
            }
        }

        function deleteSelected(that, pattern) {
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
            deleteFileOnServer(deleteFileUrl, params, deleteFilesSuccess, deleteFileError, ids);
        }
    </script>
</head>
<body>
    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="container">
        <c:if test="${nonCompleted.size() > 0}">
            <h3>Incomplete uploads:</h3>
    
            <div class="starter-template">
                <table>
                    <thead>
                        <tr>
                            <th class="textAlignCenter">
                                <input id="incomplete-checkbox_all" type="checkbox" onclick="checkBoxesToggle(this, 'incomplete-checkbox-');" />
                            </th>
                            <th class="textAlignCenter">Filename</th>
                            <th class="textAlignCenter">Size</th>
                            <th class="textAlignCenter">Done</th>
                            <th class="textAlignCenter">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${nonCompleted}" var="item">
                            <tr id="file${item.id}">
                                <td><input id="incomplete-checkbox-${item.id}" type="checkbox" /></td>
                                <td>${item.name}</td>
                                <td class="textAlignRight">${NumberUtils.humanReadableSize(item.size)}</td>
                                <td class="textAlignRight">${NumberUtils.format(100 * item.uploadedBytes / item.size, "##0")}%</td>
                                <td>
                                    <a class="noHref" onclick="resumeDownload(${item.id})">Resume</a>
                                    <a class="noHref" onclick="deleteFile(${item.id}, '${item.name}')">Delete</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <input id="incomplete-delete-selected" type="button" value="Delete" onclick="deleteSelected(this, 'incomplete-checkbox-');" />
            </div>
        </c:if>
    </div>

    <div class="container">
        <h3>Upload:</h3>

        <div class="starter-template">

            <table id="uploadTable"></table>

            <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            <input id="fileId" type="hidden" />
            <input id="file" type="file" onchange="Uploader.uploadFiles(this);" style="display: none" />
            <input id="uploadButton" type="button" value="Upload" onclick="Uploader.selectFiles(this);" />

        </div>
    </div>

    <div class="container">
        <c:if test="${completed.size() > 0}">
            <h3>Upload history:</h3>

            <div class="starter-template">
                <table>
                    <thead>
                        <tr>
                            <th class="textAlignCenter">
                                <input id="complete-checkbox_all" type="checkbox" onclick="checkBoxesToggle(this, 'complete-checkbox-');" />
                            </th>
                            <th class="textAlignCenter">Filename</th>
                            <th class="textAlignCenter">Size</th>
                            <th class="textAlignCenter">Completed</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${completed}" var="item">
                            <tr id="file${item.id}">
                                <td><input id="complete-checkbox-${item.id}" type="checkbox" /></td>
                                <td>${item.name}</td>
                                <td class="textAlignRight">${NumberUtils.humanReadableSize(item.size)}</td>
                                <td>${item.lastUpdated}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <input id="complete-delete-selected" type="button" value="Delete" onclick="deleteSelected(this, 'complete-checkbox-');" />
            </div>
        </c:if>
    </div>

</body>
</html>
