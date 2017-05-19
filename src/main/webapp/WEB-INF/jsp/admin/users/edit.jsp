<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="usersRoot" value="${adminRoot}/users" />

<!DOCTYPE html>
<html>
<head>
    <title>Main</title>

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
                            <form:errors path="login" cssClass="error-text" />
                        </td>
                        <td><input type="submit" value="Change" /></td>
                    </form:form>
                </tr>
                <tr>
                    <form:form action="${usersRoot}/password" method="post" modelAttribute="changePassword">
                        <form:hidden path="id" />
                        <td>Password:</td>
                        <td>
                            <form:input id="changePasswordField" path="pass" type="text" /><br />
                            <form:errors path="pass" cssClass="error-text" />
                        </td>
                        <td>
                            <input type="button" value="Generate" onclick="FormUtils.generatePassword('changePasswordField');" />
                            <input type="submit" value="Change" />
                        </td>
                    </form:form>
                </tr>
            </table>
            <!-- TODO displayName -->
            <!-- email -->
        </div>
    </div>

</body>
</html>
