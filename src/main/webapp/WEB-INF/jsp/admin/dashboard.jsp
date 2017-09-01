<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <title>Main</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/css/form.css" />

    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("admin.caption");
        });
    </script>
</head>
<body>

    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <jsp:include page="/WEB-INF/jspf/adminHeader.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Dashboard</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty user && user.hasPermission('ADMIN_DASHBOARD')}">
                <h4>System information</h4>

                <table>
                    <thead>
                        <tr>
                            <th>Parameter</th>
                            <th>Value</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Free space:</td>
                            <td>
                                <c:if test="${not empty freeSpace}">${freeSpace}</c:if>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </c:if>
        </div>
    </div>

</body>
</html>
