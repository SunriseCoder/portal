<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="rolesRoot" value="${adminRoot}/users" />

<!DOCTYPE html>
<html>
<head>
    <title>Role Management</title>

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
            <h3>Role Management</h3>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <table class="listTable">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Role</th>
                        <th>Permissions</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <c:forEach items="${roleList}" var="item">
                    <tr>
                        <td>${item.id}</td>
                        <td>
                            <b>${item.name}</b><br />
                            ${item.comment}
                        </td>
                        <td>
                            <c:forEach items="${item.permissions}" var="permission">
                                ${permission.name}<br />
                            </c:forEach>
                        </td>
                        <td>
                            <a href="${rolesRoot}/edit">Create</a> /
                            <a href="${rolesRoot}/edit/${item.id}">Edit</a> /
                            <a href="${rolesRoot}/delete/${item.id}">Delete</a>
                        </td>
                    </tr>
                </c:forEach>
            </table>

        </div>
    </div>

</body>
</html>
