<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <title>Error</title>

    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("error.caption");
        });
    </script>
</head>
<body>

    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>${code}</h3>
            <p>${message}</p>
        </div>
    </div>
</body>
</html>
