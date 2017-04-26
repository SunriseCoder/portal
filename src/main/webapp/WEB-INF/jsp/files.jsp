<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <title>Files</title>

    <script src="${appRoot}/scripts/http-utils.js"></script>
    <script src="${appRoot}/scripts/file-tree.js"></script>
    <script src="${appRoot}/scripts/jquery.js"></script>
    <script src="${appRoot}/scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("files.caption");

            FileTree.appRoot = "${appRoot}";
            FileTree.setHtmlNode("fileTree");
            FileTree.listUrl = "${appRoot}/rest/files/list";
            FileTree.downloadUrl = "${appRoot}/rest/files/get/";
            FileTree.build();
        });
    </script>
</head>
<body>
    <jsp:include page="includes/header.jsp" />

    <div class="container">
        <div class="starter-template">

            Search:
            <input autofocus="autofocus" id="filter" type="text" oninput="FileTree.applyFilter();" />

            Display:
            <select id="limit" onchange="FileTree.applyLimit();">
                <option value="10">10</option>
                <option value="100" selected="selected">100</option>
                <option value="1000">1000</option>
                <option value="0">All</option>
            </select>

            <div id="fileTree">Loading...</div>

        </div>
    </div>
</body>
</html>
