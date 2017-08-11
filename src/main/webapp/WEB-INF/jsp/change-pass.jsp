<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <title>Change password</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/css/form.css" />

    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/form-utils.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("change-pass.caption");
        });
    </script>
</head>
<body onload="document.forms.changePassForm.pass.focus()">

    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Please change password</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <form:form id="changePassForm" action="${appRoot}/change-pass" method="post" modelAttribute="changePassDTO">
                <table class="formTable">
                    <tr>
                        <td>Password:</td>
                        <td>
                            <form:input id="pass" path="pass" type="password" maxlength="64" /><br />
                        </td>
                        <td>
                            <form:errors path="pass" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td>Confirm:</td>
                        <td>
                            <form:input path="confirm" type="password" maxlength="64" /><br />
                        </td>
                        <td>
                            <form:errors path="confirm" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3" align="center">
                            <input type="submit" value="Change" />
                        </td>
                    </tr>
                </table>
            </form:form>
        </div>
    </div>

</body>
</html>
