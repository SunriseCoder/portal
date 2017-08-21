<%@ page import="app.util.NumberUtils" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <title>Upload</title>

    <link rel="stylesheet" href="${appRoot}/css/upload.css">

    <script src="${appRoot}/js/http-utils.js"></script>
    <script src="${appRoot}/js/cookie-utils.js"></script>
    <script src="${appRoot}/js/file-tree.js"></script>
    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>
    <script src="${appRoot}/js/upload-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("upload.caption");

            UploadUtils.uploadUrl = "${appRoot}/rest/files/upload";
        });
        
        function resumeDownload(id) {
            document.getElementById('fileId').value = id;
            var file = document.getElementById('file');
            file.removeAttribute('multiple');
            file.click();
        }
    </script>
</head>
<body>
    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="container">
        <h3>Incomplete uploads:</h3>

        <div class="starter-template">
            <table>
                <thead>
                    <tr>
                        <th class="textAlignCenter">Filename:</th>
                        <th class="textAlignCenter">Size:</th>
                        <th class="textAlignCenter">Done:</th>
                        <th class="textAlignCenter">Actions:</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${nonCompleted}" var="item">
                        <tr>
                            <td>${item.name}</td>
                            <td class="textAlignRight">${NumberUtils.humanReadableSize(item.size)}</td>
                            <td class="textAlignRight">${NumberUtils.format(100 * item.uploadedBytes / item.size, "##0")}%</td>
                            <td>
                                <a class="noHref" onclick="resumeDownload('${item.id}')">Resume</a>
                                <!-- TODO Implement -->
                                <a class="noHref">Delete</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="container">
        <h3>Upload:</h3>

        <div class="starter-template">

            <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            <table id="uploadTable"></table>
            <input id="fileId" type="hidden" />
            <input id="file" type="file" onchange="UploadUtils.onChange(this);" style="display: none" />
            <input type="button" value="Upload" onclick="UploadUtils.onClick(this);" />

        </div>
    </div>

    <div class="container">
        <h3>Upload history:</h3>

        <div class="starter-template">
            <table>
                <thead>
                    <tr>
                        <th class="textAlignCenter">Filename:</th>
                        <th class="textAlignCenter">Size:</th>
                        <th class="textAlignCenter">Completed:</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${completed}" var="item">
                        <tr>
                            <td>${item.name}</td>
                            <td class="textAlignRight">${NumberUtils.humanReadableSize(item.size)}</td>
                            <td>${item.lastUpdated}</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

</body>
</html>
