<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="festRoot" value="${appRoot}/festivals" />

<!DOCTYPE html>
<html>
<head>
    <title>Festival</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/css/form.css" />

    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/form-utils.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("festivals.caption");
        });

        function onPlaceChanged(element) {
            var textField = $('#placeText')[0];
            if (element.value == "1") {
                textField.style.visibility = 'visible';
            } else {
                textField.style.visibility = 'hidden';
            }
        }

        function onDetailsChanged() {
            var element = $('#detailsText')[0];
            var label = $('#symbolsLeft')[0];
            var remaining = element.maxLength - element.textLength;
            label.textContent = remaining;
        }
    </script>
</head>
<body onload="onDetailsChanged();">

    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Festival</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <form:form action="${festRoot}/${saveUrl}" method="post" modelAttribute="festEntity">
                <form:hidden path="id" />

                <table class="formTable">
                    <tr>
                        <td>Start:</td>
                        <td>
                            <form:input path="start" type="date" />
                            <form:errors path="start" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td>End:</td>
                        <td>
                            <form:input path="end" type="date" />
                            <form:errors path="end" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td>Place:</td>
                        <td>
                            <form:select path="place.id" onchange="onPlaceChanged(this);">
                                <form:option value="0" label="<-- Please select -->" />
                                <form:options items="${allPlaces}" itemValue="id" itemLabel="path" />
                            </form:select>
                            <form:errors path="place" cssClass="error-text" />
                        </td>
                        <td>
                            <form:input id="placeText" path="place.name" style="visibility: hidden;" />
                        </td>
                    </tr>
                    <tr>
                        <td>Details:</td>
                        <td colspan="3">
                            <form:textarea id="detailsText" path="details" maxlength="255" cols="50" rows="5"
                                           onkeyup="onDetailsChanged(this);" onchange="onDetailsChanged(this);" /><br />
                            <form:errors path="details" cssClass="error-text" />
                        </td>
                    </tr>
                    <tr>
                        <td></td>
                        <td>
                            <label>Symbols left: </label>
                            <label id="symbolsLeft">0</label>
                        </td>
                        <td colspan="2" align="center">
                            <input type="submit" value="Save" />
                        </td>
                    </tr>
                </table>
            </form:form>
        </div>
    </div>

</body>
</html>
