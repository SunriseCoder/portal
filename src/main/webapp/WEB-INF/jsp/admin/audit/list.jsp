<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.LocalDate" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="usersRoot" value="${adminRoot}/users" />
<c:set var="auditRoot" value="${adminRoot}/audit" />

<!DOCTYPE html>
<html>
<head>
    <title>Audit</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/styles/form.css" />

    <script src="${appRoot}/scripts/jquery.js"></script>
    <script src="${appRoot}/scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("audit.caption");
        });
    </script>
</head>
<body>

    <jsp:include page="../../includes/header.jsp" />

    <jsp:include page="../adminHeader.jsp" />

    <div class="container wide-page">
        <div class="starter-template">
            <h3>Audit</h3>

            <form action="" method="get">
                <%
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String today = LocalDate.now().format(formatter);
                    String from = request.getParameter("from");
                    pageContext.setAttribute("from", from == null || from.isEmpty() ? today : from);
                    String to = request.getParameter("to");
                    pageContext.setAttribute("to", to == null || to.isEmpty() ? today : to);
                %>
                From: <input type="date" name="from" value="${from}" />
                To: <input type="date" name="to" value="${to}" />
                <br />
                User: <input type="text" name="user" value="${param.user}" />
                IP: <input type="text" name="ip" value="${param.ip}" />
                <br />
                Operation:
                <select name="operation">
                    <option value="">&lt;ALL&gt;</option>
                    <c:forEach items="${operationList}" var="item">
                        <option value="${item.id}"${param.operation == item.id ? ' selected="selected"' : ''}>${item.name} (${item.severity})</option>
                    </c:forEach>
                </select>
                Types:
                <select name="type">
                    <option value="">&lt;ALL&gt;</option>
                    <c:forEach items="${typeList}" var="item">
                        <option value="${item.id}"${param.type == item.id ? ' selected="selected"' : ''}>${item.name} (${item.severity})</option>
                    </c:forEach>
                </select>
                <input type="submit" value="Filter" />
            </form>

            <table class="listTable">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>User</th>
                        <th>Date/Time</th>
                        <th>IP</th>
                        <th>Operation</th>
                        <th>Event Type</th>
                        <th>Object before</th>
                        <th>Object after</th>
                        <th>Error</th>
                    </tr>
                </thead>
                <c:forEach items="${auditEventList}" var="item">
                    <tr>
                        <td>${item.id}</td>
                        <td><a href="${usersRoot}">${item.user.login}</a></td>
                        <td>${item.date}</td>
                        <td>${item.ip}</td>
                        <td>${item.operation.severity} : ${item.operation.name}</td>
                        <td>${item.type.severity} : ${item.type.name}</td>
                        <td>${item.objectBefore}</td>
                        <td>${item.objectAfter}</td>
                        <td>${item.error}</td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </div>

</body>
</html>
