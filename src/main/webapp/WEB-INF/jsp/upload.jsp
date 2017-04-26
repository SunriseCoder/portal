<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <title>Upload</title>

    <link rel="stylesheet" href="${appRoot}/styles/upload.css">

    <script src="${appRoot}/scripts/http-utils.js"></script>
    <script src="${appRoot}/scripts/cookie-utils.js"></script>
    <script src="${appRoot}/scripts/file-tree.js"></script>
    <script src="${appRoot}/scripts/jquery.js"></script>
    <script src="${appRoot}/scripts/locale-utils.js"></script>
    <script src="${appRoot}/scripts/upload-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("upload.caption");

            UploadUtils.uploadUrl = "${appRoot}/rest/files/upload";
        });
    </script>
</head>
<body>
    <jsp:include page="includes/header.jsp" />

    <div class="container">
        <div class="starter-template">

            <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            <table id="uploadTable"></table>
            <input id="file" type="file" multiple="multiple" onchange="UploadUtils.onChange(this);" style="display: none" />
            <input type="button" value="Upload" onclick="document.getElementById('file').click();" />

        </div>
    </div>
</body>
</html>
