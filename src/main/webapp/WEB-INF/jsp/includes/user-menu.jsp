<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${appRoot}/styles/form.css">

<ul class="nav navbar-nav">
    <li><a href="${appRoot}/profile">Profile</a></li>
    <li><a class="noHref" onclick="logout.submit();">Logout</a></li>
</ul>
