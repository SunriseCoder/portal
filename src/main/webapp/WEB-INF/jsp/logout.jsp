<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<body onload="logout.submit();">
    <form id="logout" action="${appRoot}/logout" method="post">
        <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    </form>
</body>
