<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <title>Register</title>

    <link rel="stylesheet" href="${appRoot}/css/form.css" />

    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("register.caption");
        });
    </script>
</head>
<body onload="document.forms.userForm.login.focus()">

    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Registration</h3>

            <form:form action="${appRoot}/register" method="POST" modelAttribute="userForm">
                <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                <table>
                    <tr>
                        <td>Login: </td>
                        <td>
                            <form:input path="login" type="text" maxlength="32" /> (just for log in, nobody will see it)<br />
                            <form:errors path="login" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td>Password: </td>
                        <td>
                            <form:input path="pass" type="password" />
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
                        <td>Display Name: </td>
                        <td>
                            <form:input path="displayName" type="text" maxlength="64" /> (how the other people will see You)<br />
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
                        <td colspan="2"><input type="submit" value="Register"></td>
                    </tr>
                </table>
            </form:form>

        </div>
    </div>
</body>
</html>
