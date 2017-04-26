<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <title>Main</title>

    <script src="${appRoot}/scripts/jquery.js"></script>
    <script src="${appRoot}/scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("index.caption");
        });
    </script>
</head>
<body>
    <jsp:include page="includes/header.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Welcome to the Portal</h3>
        </div>
    </div>
</body>
</html>
