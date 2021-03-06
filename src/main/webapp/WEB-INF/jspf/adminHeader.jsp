<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />

<link rel="stylesheet" type="text/css" href="${appRoot}/webjars/bootstrap/3.3.7/css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="${appRoot}/css/header.css" />

<nav class="navbar navbar-inverse">
    <div class="container">
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">

                <li><a href="${adminRoot}/">Dashboard</a></li>

                <c:if test="${not empty user && user.hasPermission('ADMIN_USERS_VIEW')}">
                    <li><a href="${adminRoot}/users">Users</a></li>
                </c:if>

                <c:if test="${not empty user && user.hasPermission('ADMIN_ROLES_VIEW')}">
                    <li><a href="${adminRoot}/roles">Roles</a></li>
                </c:if>

                <c:if test="${not empty user && user.hasPermission('ADMIN_IPBAN_VIEW')}">
                    <li><a href="${adminRoot}/ip-bans">IP-Bans</a></li>
                </c:if>

                <c:if test="${not empty user && user.hasPermission('ADMIN_AUDIT_VIEW')}">
                    <li><a href="${adminRoot}/audit">Audit</a></li>
                </c:if>

                <c:if test="${not empty user && user.hasPermission('ADMIN_LOGS_VIEW')}">
                    <li><a href="${adminRoot}/logs">Logs</a></li>
                </c:if>

                <c:if test="${not empty user && user.hasPermission('ADMIN_CACHE_VIEW')}">
                    <li><a href="${adminRoot}/cache">Cache</a></li>
                </c:if>

                <c:if test="${not empty user && user.hasPermission('ADMIN_EXTJOBS_VIEW')}">
                    <li><a href="${adminRoot}/ext-jobs">External Jobs</a></li>
                </c:if>

            </ul>
        </div>
    </div>
</nav>
