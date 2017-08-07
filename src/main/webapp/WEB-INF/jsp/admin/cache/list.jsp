<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.LocalDate" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="cacheRoot" value="${adminRoot}/cache" />

<!DOCTYPE html>
<html>
<head>
    <title>Cache</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/css/form.css" />

    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("cache.caption");
        });
    </script>
</head>
<body>

    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <jsp:include page="/WEB-INF/jspf/adminHeader.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Cache</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <table class="listTable">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Size</th>
                        <th>Last update</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <c:forEach items="${cacheList}" var="item">
                    <tr>
                        <td>${item.name}</td>
                        <td>${item.size}</td>
                        <td>${item.lastUpdate}</td>
                        <td>
                            <c:if test="${not empty user && user.hasPermission('ADMIN_CACHE_REFRESH')}">
                                <form action="${cacheRoot}/${item.url}" method="post">
                                    <input name="${_csrf.parameterName}" type="hidden" value="${_csrf.token}" />
                                    <input type="submit" value="Refresh" />
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </table>

        </div>
    </div>

</body>
</html>
