<!DOCTYPE html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
<head>
    <title>Register</title>

    <link rel="stylesheet" href="styles/register.css">
    <link rel="stylesheet" href="styles/form.css">

    <script src="scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.writeTitle("register.caption");
        });
    </script>
</head>
<body>
    <jsp:include page="includes/header.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Registration</h3>

            <form:form action="/register" method="POST" modelAttribute="user">
                <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                <table>
                    <tr>
                        <td>Login: </td>
                        <td>
                            <form:input path="login" type="text" />
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
                        <td>E-Mail: </td>
                        <td>
                            <form:input path="email" type="text" />
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
