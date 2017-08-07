<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <title>Login</title>

    <link rel="stylesheet" href="${appRoot}/css/form.css">

    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("login.caption");
        });
    </script>
</head>
<body onload="document.forms.loginForm.username.focus()">

    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="container">
        <div class="starter-template">

            <h3>Login</h3>
            <c:if test="${not empty error}">
                <h5 class="error-text">${error}</h5>
            </c:if>

            <jsp:include page="/WEB-INF/jspf/loginForm.jsp">
                <jsp:param name="formId" value="loginForm" />
            </jsp:include>
            <input type="submit" value="Login" onclick="document.getElementById('loginForm').submit();" />

        </div>
    </div>
</body>
</html>
