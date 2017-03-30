<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html>
<head>
    <title>Login</title>

    <link rel="stylesheet" href="styles/register.css">
    <link rel="stylesheet" href="styles/form.css">

    <script src="scripts/jquery.js"></script>
    <script src="scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.writeTitle("login.caption");
        });
    </script>
</head>
<body onload='document.forms.login.username.focus();'>
    <jsp:include page="includes/header.jsp" />

    <div class="container">
        <div class="starter-template">

            <h3>Login</h3>
            <c:if test="${not empty error}">
                <h5 class="error-text">${error}</h5>
            </c:if>
            <form:form id="login" action="/login" method="POST" modelAttribute="login">
                <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                <table>
                    <tr>
                        <td>User:</td>
                        <td>
                            <form:input path="username" type="text" />
                            <form:errors path="username" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td>Password:</td>
                        <td>
                            <form:input path="password" type="password" />
                            <form:errors path="password" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <form:checkbox path="rememberMe" /> Remember me on this computer.
                            <form:errors path="rememberMe" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2"><input type="submit" value="Login"></td>
                    </tr>
                </table>
            </form:form>

        </div>
    </div>
</body>
</html>
