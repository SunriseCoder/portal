<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" type="text/css" href="${appRoot}/webjars/bootstrap/3.3.7/css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="${appRoot}/styles/header.css" />

<script src="${appRoot}/scripts/form-utils.js"></script>
<script src="${appRoot}/scripts/popup-utils.js"></script>

<nav class="navbar navbar-inverse">
    <div class="container">
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li><a href="${appRoot}/">Main</a></li>
                <li><a href="${appRoot}/files">Files</a></li>

                <c:if test="${not empty user && user.hasPermission('UPLOAD_FILES')}">
                    <li><a href="${appRoot}/upload">Upload</a></li>
                </c:if>

                <c:if test="${not empty user && user.hasPermission('ADMIN_PAGE')}">
                    <li><a href="${appRoot}/admin">Administration</a></li>
                </c:if>

                <c:choose>
                    <c:when test="${empty user}">
                        <li>

                            <div class="popup">
                                <span id="popup" class="popupspan">
                                    <jsp:include page="loginForm.jsp">
                                        <jsp:param name="formId" value="headerLoginForm" />
                                    </jsp:include>
                                    <span class="buttons">
                                        <button onclick="FormUtils.submitForm('headerLoginForm')">Login</button>
                                        <button onclick="PopupUtils.hidePopup()">Cancel</button>
                                    </span>
                                </span>
                            </div>

                        </li>
                        <li><a href="#" onclick="PopupUtils.showPopup(document.getElementById('headerLoginForm').username)">Login</a></li>
                        <li><a href="${appRoot}/register">Register</a></li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <div>
                                Welcome, ${user.displayName}
                                (<a href="#" onclick="logout.submit();">Logout</a>)
                            </div>
                        </li>

                        <form id="logout" action="${appRoot}/logout" method="post">
                            <input id="csrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        </form>
                    </c:otherwise>
                </c:choose>

            </ul>
        </div>
    </div>
</nav>
