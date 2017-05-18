<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" type="text/css" href="${appRoot}/webjars/bootstrap/3.3.7/css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="${appRoot}/styles/header.css" />
<link rel="stylesheet" type="text/css" href="${appRoot}/styles/popup.css" />

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

                <li class="separator">&nbsp;</li>

                <c:choose>
                    <c:when test="${empty user}">
                        <li>

                            <div class="popup">
                                <span id="popup" class="popupspan">
                                    <jsp:include page="login-form.jsp">
                                        <jsp:param name="formId" value="headerLoginForm" />
                                    </jsp:include>
                                    <span class="buttons">
                                        <button onclick="FormUtils.submitForm('headerLoginForm')">Login</button>
                                        <button onclick="PopupUtils.hidePopup()">Cancel</button>
                                    </span>
                                </span>
                            </div>

                        </li>
                        <li><a id="loginLink" href="#" onclick="PopupUtils.showPopup('loginLink', document.getElementById('headerLoginForm').username)">Login</a></li>
                        <li><a href="${appRoot}/register">Register</a></li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <div class="popup">
                                <span id="popup" class="popupspan">
                                    <jsp:include page="user-menu.jsp" />
                                </span>
                            </div>
                        </li>
                        <li>
                            <div>
                                Welcome,
                                <a id="userName" href="#" onclick="PopupUtils.showPopup('userName');">${user.displayName}</a>
<%--                                  ${user.displayName} --%>
<!--                                 (<a href="#" onclick="logout.submit();">Logout</a>) -->
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
