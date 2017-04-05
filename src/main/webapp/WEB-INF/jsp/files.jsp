<!DOCTYPE html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Files</title>

    <script src="/scripts/http-utils.js"></script>
    <script src="/scripts/file-tree.js"></script>
    <script src="/scripts/jquery.js"></script>
    <script src="/scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.writeTitle("files.caption");

            FileTree.setHtmlNode("fileTree");
            FileTree.listUrl = "/rest/files/list";
            FileTree.downloadUrl = "/rest/files/get/";
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
