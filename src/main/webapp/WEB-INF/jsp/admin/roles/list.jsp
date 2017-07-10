<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="rolesRoot" value="${adminRoot}/roles" />

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
            Locales.writeTitle("roles.caption");
        });

        function deleteRole(id, name) {
            var confirmed = confirm("Are You sure to delete role " + name);
            if (confirmed) {
                $('#deleteRoleId')[0].value = id;
                $('#deleteRoleForm')[0].submit();
            }
        }
    </script>
</head>
<body>

    <jsp:include page="../../includes/header.jsp" />

    <jsp:include page="../adminHeader.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Role Management</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <a href="${rolesRoot}/create">Create new Role</a>

            <table class="listTable">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Role</th>
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
                            <c:choose>
                                <c:when test="${not empty user && user.hasPermission('ADMIN_USERS_EDIT')}">
                                    <a href="${rolesRoot}/edit?id=${item.id}">Edit</a> /
                                    <a class="noHref" onclick="deleteRole(${item.id}, '${item.name}');">Delete</a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${rolesRoot}/edit?id=${item.id}">View</a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </table>

            <h3>Role-Permission Matrix</h3>

            <table class="listTable" border="1">
                <thead>
                    <tr>
                        <th>Permissions \ Roles</th>
                        <c:forEach items="${roleList}" var="role">
                            <th>${role.name}</th>
                        </c:forEach>
                    </tr>
                </thead>
                <c:forEach items="${permissionList}" var="permission">
                    <tr>
                        <td><b>${permission.name}</b></td>
                        <c:forEach items="${roleList}" var="role">
                            <td style="text-align: center;">
                                <c:if test="${role.permissions.contains(permission)}">
                                    X
                                </c:if>
                            </td>
                        </c:forEach>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </div>

    <form id="deleteRoleForm" action="${rolesRoot}/delete" method="post">
        <input id="deleteRoleCsrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input id="deleteRoleId" type="hidden" name="id" />
    </form>

</body>
</html>
