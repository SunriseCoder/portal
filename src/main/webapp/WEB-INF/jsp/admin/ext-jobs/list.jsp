<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.LocalDate" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="extJobRoot" value="${adminRoot}/ext-jobs" />

<!DOCTYPE html>
<html>
<head>
    <title>External Jobs</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/css/form.css" />

    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("ext-jobs.caption");
        });
    </script>
</head>
<body>

    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <jsp:include page="/WEB-INF/jspf/adminHeader.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>External Jobs</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <table class="listTable">
                <thead>
                    <tr>
                        <th>Job</th>
                        <th>Started</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <c:if test="${not empty jobInfo}">
                    <tr>
                        <td>${jobInfo.command}</td>
                        <td>${jobInfo.started}</td>
                        <td>
                            <c:if test="${not empty user && user.hasPermission('ADMIN_EXTJOBS_KILL')}">
                                <form action="${extJobRoot}/kill" method="post">
                                    <input name="${_csrf.parameterName}" type="hidden" value="${_csrf.token}" />
                                    <input type="submit" value="Kill" />
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:if>
            </table>

        </div>
    </div>

</body>
</html>
