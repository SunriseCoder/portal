<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<link rel="stylesheet" type="text/css" href="webjars/bootstrap/3.3.7/css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="/styles/header.css" />

<nav class="navbar navbar-inverse">
    <div class="container">
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li><a href="/">Main</a></li>
                <li><a href="/files">Files</a></li>
                <li><a href="/upload">Upload</a></li>

                <c:choose>
                    <c:when test="${pageContext.request.userPrincipal.name == null}">
                        <li>
                            <div>
                                <form id="headerLogin" action="/login" method="POST">
                                    User: <input type="text" name="username" value="" />
                                    Password: <input type="password" name="password" />
                                    <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                    <input type="submit" name="submit" value="Login" />
                                </form>
                            </div>
                        </li>
                        <li><a href="/login">Login</a></li>
                        <li><a href="/register">Register</a></li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <div>
                                Welcome, ${pageContext.request.userPrincipal.name}
                                (<a href="#" onclick="logout.submit();">Logout</a>)
                            </div>
                        </li>

                        <form id="logout" action="/logout" method="post">
                            <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        </form>
                    </c:otherwise>
                </c:choose>

            </ul>
        </div>
    </div>
</nav>
