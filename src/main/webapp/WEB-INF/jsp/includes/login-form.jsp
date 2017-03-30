<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form id="login" action="/login" method="POST" modelAttribute="login">
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
                <form:input path="password" type="password" />
                <form:errors path="password" cssClass="error-text" />
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <form:checkbox path="rememberMe" /> Remember me on this computer.
                <form:errors path="rememberMe" cssClass="error-text" />
            </td>
        </tr>
        <tr>
            <td colspan="2"><input type="submit" value="Login"></td>
        </tr>
    </table>
</form:form>
