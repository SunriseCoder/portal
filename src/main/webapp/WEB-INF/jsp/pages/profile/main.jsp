<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="usersRoot" value="${adminRoot}/users" />

<!DOCTYPE html>
<html>
<head>
    <title>User Profile</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/styles/form.css" />

    <script src="${appRoot}/scripts/jquery.js"></script>
    <script src="${appRoot}/scripts/form-utils.js"></script>
    <script src="${appRoot}/scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("profile.caption");
        });
    </script>
</head>
<body>

    <jsp:include page="../../includes/header.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>User Profile</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <form:form action="${appRoot}/profile" method="POST" modelAttribute="userProfile">
                <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                <table class="formTable">
                    <tr>
                        <td>Display Name: </td>
                        <td>
                            <form:input path="displayName" type="text" maxlength="64" /> (How the other people will see You)<br />
                            <form:errors path="displayName" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td>E-Mail: </td>
                        <td>
                            <form:input path="email" type="text" maxlength="64" />
                            <form:errors path="email" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td>Password: </td>
                        <td>
                            <form:input path="pass" type="password" /> (Leave empty if You don't want to change password)
                            <form:errors path="pass" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td>Confirm: </td>
                        <td>
                            <form:input path="confirm" type="password" />
                            <form:errors path="confirm" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2"><hr /></td>
                    </tr>
                    <tr>
                        <td>Current password: </td>
                        <td>
                            <form:input path="currentPass" type="password" />
                            <form:errors path="currentPass" cssClass="error-text" /><br />
                             (Please enter current password to commit changes)
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center"><input type="submit" value="Save"></td>
                    </tr>
                </table>
            </form:form>
        </div>
    </div>

</body>
</html>
