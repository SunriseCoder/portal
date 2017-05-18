<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" type="text/css" href="${appRoot}/webjars/bootstrap/3.3.7/css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="${appRoot}/styles/header.css" />

<nav class="navbar navbar-inverse">
    <div class="container">
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li><a href="${appRoot}/admin/">Dashboard</a></li>
            </ul>
        </div>
    </div>
</nav>
