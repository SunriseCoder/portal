<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="placeRoot" value="${appRoot}/places" />

<!DOCTYPE html>
<html>
<head>
    <title>Places</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/styles/form.css" />

    <script src="${appRoot}/scripts/jquery.js"></script>
    <script src="${appRoot}/scripts/form-utils.js"></script>
    <script src="${appRoot}/scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("places.caption");
        });
    </script>
</head>
<body>

    <jsp:include page="../../includes/header.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Place</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <form:form action="${placeRoot}/save" method="post" modelAttribute="place">
                <form:hidden path="id" />

                <table class="formTable">
                    <tr>
                        <td>Name:</td>
                        <td>
                            <form:input path="name" type="text" />
                            <form:errors path="name" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td>Parent:</td>
                        <td>
                            <form:select path="parent.id">
                                <form:option value="0">&lt; Root &gt;</form:option>
                                <form:options items="${allPlaces}" itemValue="id" itemLabel="path" />
                            </form:select>
                            <form:errors path="parent" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td align="center">
                            <input type="submit" value="Save" />
                        </td>
                    </tr>
                </table>
            </form:form>
        </div>
    </div>

</body>
</html>
