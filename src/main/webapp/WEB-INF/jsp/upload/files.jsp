<%@ page import="app.util.NumberUtils" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="t" tagdir="/WEB-INF/tags/files" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="restFilesRoot" value="${appRoot}/rest/files" />
<c:set var="uploaderRoot" value="${appRoot}/js/uploader" />

<!DOCTYPE html>
<html>
<head>
    <title>Upload</title>

    <link rel="stylesheet" href="${appRoot}/css/form.css">
    <link rel="stylesheet" href="${appRoot}/css/upload.css">

    <script src="${appRoot}/js/prototypes.js"></script>
    <script src="${appRoot}/js/http-utils.js"></script>
    <script src="${appRoot}/js/cookie-utils.js"></script>
    <script src="${appRoot}/js/file-tree.js"></script>
    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>
    <script src="${uploaderRoot}/uploader.js"></script>
    <script src="${uploaderRoot}/uploader-ui.js"></script>
    <script src="${uploaderRoot}/upload-handlers.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("upload.caption");

            Uploader.checkSumWorkerUrl = "${uploaderRoot}/checksum-worker.js"
            Uploader.chunkUploadWorkerUrl = "${uploaderRoot}/upload-worker.js";
            Uploader.createFilePlaceholderUrl = "${restFilesRoot}/create";
            Uploader.uploadChunkUrl = "${restFilesRoot}/upload-chunk";
            Uploader.deleteFileUrl = "${restFilesRoot}/delete";

            UploaderUI.saveFileInfoUrl = "${restFilesRoot}/save-info";
            UploaderUI.publishFileUrl = "${restFilesRoot}/publish";
            
            DeleteFileHandler.deleteFileUrl = "${restFilesRoot}/delete";
            PublishFileHandler.publishFileUrl = "${restFilesRoot}/publish";
        });
    </script>
</head>
<body>
    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <!-- Non-completed -->

    <div class="container">
        <c:if test="${nonCompleted.size() > 0}">
            <h3>Incompletely uploaded files:</h3>

            <div class="starter-template">
                <table>
                    <thead>
                        <tr>
                            <th class="textAlignCenter">
                                <input id="incomplete-checkbox_all" type="checkbox" onclick="DeleteFileHandler.checkBoxesToggle(this, 'incomplete-checkbox-');" />
                            </th>
                            <th class="textAlignCenter">Filename</th>
                            <th class="textAlignCenter">Size</th>
                            <th class="textAlignCenter">Done</th>
                            <th class="textAlignCenter">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${nonCompleted}" var="item">
                            <tr id="file_${item.id}">
                                <td><input id="incomplete-checkbox-${item.id}" type="checkbox" /></td>
                                <td>${item.filename}</td>
                                <td class="textAlignRight">${NumberUtils.humanReadableSize(item.size)}</td>
                                <td class="textAlignRight">${NumberUtils.format(100 * item.uploadedBytes / item.size, "##0")}%</td>
                                <td>
                                    <a class="noHref" onclick="DeleteFileHandler.resumeDownload(${item.id})">Resume</a>
                                    <a class="noHref" onclick="DeleteFileHandler.deleteFile(${item.id}, '${item.filename}')">Delete</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <input id="incomplete-delete-selected" type="button" value="Delete" onclick="DeleteFileHandler.deleteSelected(this, 'incomplete-checkbox-');" />
            </div>
        </c:if>
    </div>

    <div class="container">
        <h3>Upload:</h3>

        <div class="starter-template">

            <div id="uploadTable"></div><br />

            <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            <input id="fileId" type="hidden" />
            <input id="file" type="file" onchange="Uploader.uploadFiles(this);" style="display: none" />
            <input id="uploadButton" type="button" value="Upload" onclick="Uploader.selectFiles(this);" />

        </div>
    </div>

    <!-- Non-published -->

    <div class="container">
        <c:if test="${nonPublished.size() > 0}">
            <h3>Non-published files:</h3>

            <div class="starter-template">
                <c:forEach items="${nonPublished}" var="item">
                    <t:file-element file="${item}" tablePrefix="non-public"></t:file-element>
                </c:forEach>

                <div class="selectionCheckbox">
                    <input id="non-public-checkbox_all" type="checkbox" onclick="DeleteFileHandler.checkBoxesToggle(this, 'non-public-checkbox-');" />
                    <label for="non-public-checkbox_all" style="font-weight: normal;">Select All</label> -
                    <input id="non-public-delete-selected" type="button" value="Delete" onclick="DeleteFileHandler.deleteSelected(this, 'non-public-checkbox-');" />
                    <input id="non-public-publish-selected" type="button" value="Publish" onclick="PublishFileHandler.publishSelected(this, 'non-public-checkbox-');" />
                </div>
            </div>
        </c:if>
    </div>

    <!-- Published -->

    <div class="container">
        <c:if test="${published.size() > 0}">
            <h3>Published files:</h3>

            <div class="starter-template">
                <c:forEach items="${published}" var="item">
                    <t:file-element file="${item}" tablePrefix="public"></t:file-element>
                </c:forEach>

                <div class="selectionCheckbox">
                    <input id="public-checkbox_all" type="checkbox" onclick="DeleteFileHandler.checkBoxesToggle(this, 'public-checkbox-');" />
                    <label for="public-checkbox_all" style="font-weight: normal;">Select All</label> - 
                    <input id="public-delete-selected" type="button" value="Delete" onclick="DeleteFileHandler.deleteSelected(this, 'public-checkbox-');" />
                </div>
            </div>
        </c:if>
    </div>

</body>
</html>
