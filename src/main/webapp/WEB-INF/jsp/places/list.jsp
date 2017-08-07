<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ taglib prefix="t" tagdir="/WEB-INF/tags/trees" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="placesRoot" value="${appRoot}/places" />

<!DOCTYPE html>
<html>
<head>
    <title>Places</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/css/form.css" />

    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("places.caption");
        });

        function deletePlace(id, name) {
            var confirmed = confirm("Are You sure to delete place '" + name + "'?");
            if (confirmed) {
                $('#deletePlaceId')[0].value = id;
                $('#deletePlaceForm')[0].submit();
            }
        }
    </script>
</head>
<body>

    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="container">
        <div class="starter-template">

            <h3>Places</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <c:set var="canEdit" value="${not empty user && user.hasPermission('ADMIN_PLACES_EDIT')}" />

            <c:if test="${canEdit}">
                <a href="${placesRoot}/add">Add new place</a>
            </c:if>

            <t:places indent="0" items="${placeList}" canEdit="${canEdit}" placesRoot="${placesRoot}" />

        </div>
    </div>

    <form id="deletePlaceForm" action="${placesRoot}/delete" method="post">
        <input id="deletePlaceCsrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input id="deletePlaceId" type="hidden" name="id" />
    </form>

</body>
</html>
