<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<ul class="nav navbar-nav">
    <li><a href="#">Profile</a></li>
    <li><a href="#" onclick="logout.submit();">Logout</a></li>
</ul>
