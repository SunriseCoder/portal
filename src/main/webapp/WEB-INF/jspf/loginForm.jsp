<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" type="text/css" href="${appRoot}/css/form.css" />

<script src="${appRoot}/js/form-utils.js"></script>

<form:form id="${param.formId}" action="${appRoot}/login" method="POST" modelAttribute="login">
    <table>
        <tr>
            <td>User:</td>
            <td>
                <form:input path="username" type="text" />
                <form:errors path="username" cssClass="error-text" />
            </td>
        </tr>
        <tr>
            <td>Password:</td>
            <td>
                <form:input path="password" type="password"
                    onkeypress="FormUtils.submitOnEnterPressed(event, '${param.formId}')" />
                <form:errors path="password" cssClass="error-text" />
            </td>
        </tr>
    </table>
</form:form>
