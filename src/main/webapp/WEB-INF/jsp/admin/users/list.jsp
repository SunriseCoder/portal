<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="usersRoot" value="${adminRoot}/users" />

<!DOCTYPE html>
<html>
<head>
    <title>Main</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/styles/form.css" />

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

    <jsp:include page="../../includes/header.jsp" />

    <jsp:include page="../adminHeader.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Users Management</h3>

            <table class="listTable">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Login</th>
                        <th>Display Name</th>
                        <th>E-Mail</th>
                        <th>Locked</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <c:forEach items="${userList}" var="item">
                    <tr>
                        <td>${item.id}</td>
                        <td>${item.login}</td>
                        <td>${item.displayName}</td>
                        <td>${item.email}</td>
                        <td>
                            <c:if test="${item.locked}">
                                <span class="warning" title="${item.lock.reason} (by ${item.lock.lockedBy.displayName})">Locked</span>
                            </c:if>
                        </td>

                        <td>
                            <a href="${usersRoot}/edit/${item.id}">Edit</a> /
                            <c:choose>
                                <c:when test="${item.locked}"><a href="#">Unlock</a></c:when>
                                <c:otherwise><a href="#">Lock</a></c:otherwise>
                            </c:choose> /
                            <a href="#">Confirm</a>
                        </td>
                    </tr>
                </c:forEach>
            </table>

        </div>
    </div>

</body>
</html>
