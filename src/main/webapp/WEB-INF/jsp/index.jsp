<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <title>Main</title>

    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("index.caption");
        });
    </script>
</head>
<body>
    <spring:eval var="env" expression="@environment.getProperty('environment')" />

    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>
                Welcome to the Portal
                <c:if test="${not empty env}">
                    (${env})
                </c:if>
            </h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

        </div>
    </div>
</body>
</html>
