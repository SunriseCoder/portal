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
    </script>
</head>
<body>
    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <span>&nbsp;</span>

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
