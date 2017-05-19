<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

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
            Locales.writeTitle("admin.caption");
        });
    </script>
</head>
<body>

    <jsp:include page="../includes/header.jsp" />

    <jsp:include page="adminHeader.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Dashboard</h3>

            <c:if test="${not empty user && user.hasPermission('ADMIN_DASHBOARD')}">
                <p>Secret block here</p>
            </c:if>
        </div>
    </div>

</body>
</html>
