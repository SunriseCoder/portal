<!DOCTYPE html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
<head>
    <title>Login</title>

    <link rel="stylesheet" href="/styles/form.css">

    <script src="/scripts/jquery.js"></script>
    <script src="/scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.writeTitle("login.caption");
        });
    </script>
</head>
<body onload='document.forms.loginForm.username.focus();'>
    <jsp:include page="includes/header.jsp" />

    <div class="container">
        <div class="starter-template">

            <h3>Login</h3>
            <c:if test="${not empty error}">
                <h5 class="error-text">${error}</h5>
            </c:if>

            <jsp:include page="includes/login-form.jsp">
                <jsp:param name="formId" value="loginForm" />
            </jsp:include>
            <input type="submit" value="Login" onclick="document.getElementById('loginForm').submit();" />

        </div>
    </div>
</body>
</html>
