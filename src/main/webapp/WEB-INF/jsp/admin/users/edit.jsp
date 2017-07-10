<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="usersRoot" value="${adminRoot}/users" />

<!DOCTYPE html>
<html>
<head>
    <title>Edit User</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/styles/form.css" />

    <script src="${appRoot}/scripts/jquery.js"></script>
    <script src="${appRoot}/scripts/form-utils.js"></script>
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
            <h3>User Details</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <table class="formTable">
                <tr>
                    <td>ID:</td>
                    <td>${userEntity.id}</td>
                </tr>
                <tr>
                    <form:form action="${usersRoot}/login" method="post" modelAttribute="changeLogin">
                        <form:hidden path="id" />
                        <td>Login:</td>
                        <td>
                            <form:input path="login" type="text" maxlength="32" /><br />
                        </td>
                        <td>
                            <c:if test="${not empty user && user.hasPermission('ADMIN_USERS_EDIT')}">
                                <input type="submit" value="Change" />
                                <form:errors path="login" cssClass="error-text" />
                            </c:if>
                        </td>
                    </form:form>
                </tr>
                <tr>
                    <form:form action="${usersRoot}/password" method="post" modelAttribute="changePassword">
                        <form:hidden path="id" />
                        <td>Password:</td>
                        <td>
                            <form:input id="changePasswordField" path="pass" type="text" /><br />
                        </td>
                        <td>
                            <c:if test="${not empty user && user.hasPermission('ADMIN_USERS_EDIT')}">
                                <input type="button" value="Generate" onclick="FormUtils.generatePassword('changePasswordField');" />
                                <input type="submit" value="Change" />
                                <form:errors path="pass" cssClass="error-text" />
                            </c:if>
                        </td>
                    </form:form>
                </tr>
                <tr>
                    <form:form action="${usersRoot}/display-name" method="post" modelAttribute="changeDisplayName">
                        <form:hidden path="id" />
                        <td>Display Name:</td>
                        <td>
                            <form:input path="displayName" type="text" maxlength="64" /><br />
                        </td>
                        <td>
                            <c:if test="${not empty user && user.hasPermission('ADMIN_USERS_EDIT')}">
                                <input type="submit" value="Change" />
                                <form:errors path="displayName" cssClass="error-text" />
                            </c:if>
                        </td>
                    </form:form>
                </tr>
                <tr>
                    <form:form action="${usersRoot}/email" method="post" modelAttribute="changeEmail">
                        <form:hidden path="id" />
                        <td>E-Mail:</td>
                        <td>
                            <form:input path="email" type="text" maxlength="64" /><br />
                        </td>
                        <td>
                            <c:if test="${not empty user && user.hasPermission('ADMIN_USERS_EDIT')}">
                                <input type="submit" value="Change" />
                                <form:errors path="email" cssClass="error-text" />
                            </c:if>
                        </td>
                    </form:form>
                </tr>
                <tr>
                    <form:form action="${usersRoot}/roles" method="post" modelAttribute="changeRoles">
                        <form:hidden path="id" />
                        <td>Roles:</td>
                        <td>
                            <c:forEach items="${allRoles}" var="role">
                                <form:checkbox path="roles" value="${role}" label="${role.name}" /><br/>
                                <c:forEach items="${role.permissions}" var="permission">
                                    <span class="permission"><b>${permission.name}</b> (${permission.comment})</span><br />
                                </c:forEach>
                            </c:forEach>
                        </td>
                        <td>
<%--                             <c:if test="${not empty user && user.hasPermission('ADMIN_USERS_ROLES')}"> --%>
                                <input type="submit" value="Change" />
                                <form:errors path="roles" cssClass="error-text" />
<%--                             </c:if> --%>
                        </td>
                    </form:form>
                </tr>
                <tr>
                    <td>Permissions:</td>
                    <td>
                        <form:form commandName="userEntity">
                            <c:forEach items="${allPermissions}" var="permission">
                                <form:checkbox disabled="true" path="permissions" value="${permission.name}" />
                                <b>${permission.name}</b> (${permission.comment})<br />
                            </c:forEach>
                        </form:form>
                    </td>
                </tr>
            </table>
        </div>
    </div>

</body>
</html>
