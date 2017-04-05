<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<link rel="stylesheet" type="text/css" href="/webjars/bootstrap/3.3.7/css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="/styles/form.css" />

<script src="/scripts/form-utils.js"></script>

<form:form id="${param.formId}" action="/login" method="POST" modelAttribute="login">
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
        <tr>
            <td colspan="2">
                <form:checkbox path="rememberMe" /> Remember me on this computer.
                <form:errors path="rememberMe" cssClass="error-text" />
            </td>
        </tr>
    </table>
</form:form>
