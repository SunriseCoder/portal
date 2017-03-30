<!DOCTYPE html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Upload</title>

    <link rel="stylesheet" href="styles/upload.css">

    <script src="scripts/http-utils.js"></script>
    <script src="scripts/cookie-utils.js"></script>
    <script src="scripts/file-tree.js"></script>
    <script src="scripts/jquery.js"></script>
    <script src="scripts/locale-utils.js"></script>
    <script src="scripts/upload-utils.js"></script>

    <script>
        $(function() {
            Locales.writeTitle("upload.caption");

            UploadUtils.uploadUrl = "/rest/files/upload";
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
