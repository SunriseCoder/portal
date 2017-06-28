<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="rolesRoot" value="${adminRoot}/roles" />

<!DOCTYPE html>
<html>
<head>
    <title>Edit Role</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/styles/form.css" />

    <script src="${appRoot}/scripts/jquery.js"></script>
    <script src="${appRoot}/scripts/form-utils.js"></script>
    <script src="${appRoot}/scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("roles.caption");
        });
    </script>
</head>
<body>

    <jsp:include page="../../includes/header.jsp" />

    <jsp:include page="../adminHeader.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Role Details</h3>

            <form:form action="${rolesRoot}/save" method="post" modelAttribute="roleEntity">
                <form:hidden path="id" />

                <table class="formTable">
                    <tr>
                        <td>ID:</td>
                        <td>${roleEntity.id}</td>
                    </tr>
                    <tr>
                        <td>Name:</td>
                        <td>
                            <form:input path="name" type="text" maxlength="64" size="50" /><br />
                            <form:errors path="name" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td>Comment:</td>
                        <td>
                            <form:textarea path="comment" type="text" maxlength="255" cols="50" rows="5" /><br />
                            <form:errors path="comment" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td>Permissions:</td>
                        <td>
                            <c:forEach items="${allPermissions}" var="permission">
                                <form:checkbox path="permissions" value="${permission}" /><b>${permission.id}: ${permission.name}</b><br />
                                <span class="permission">(${permission.comment})</span><br />
                            </c:forEach>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input type="submit" value="Save" />
                        </td>
                    </tr>
                </table>
            </form:form>
        </div>
    </div>

</body>
</html>
