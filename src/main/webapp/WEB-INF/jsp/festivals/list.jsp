<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="festsRoot" value="${appRoot}/festivals" />

<!DOCTYPE html>
<html>
<head>
    <title>Festivals</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/css/form.css" />

    <script src="${appRoot}/js/jquery.js"></script>
    <script src="${appRoot}/js/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("festivals.caption");
        });

        function deleteFestival(id, name) {
            var confirmed = confirm("Are You sure to delete festival " + name);
            if (confirmed) {
                $('#deleteFestivalId')[0].value = id;
                $('#deleteFestivalForm')[0].submit();
            }
        }
    </script>
</head>
<body>

    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Festivals</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <c:set var="canAdd" value="${not empty user && user.hasPermission('ADMIN_FESTIVALS_ADD')}" />
            <c:set var="canEdit" value="${not empty user && user.hasPermission('ADMIN_FESTIVALS_EDIT')}" />
            <c:set var="canDelete" value="${not empty user && user.hasPermission('ADMIN_FESTIVALS_DELETE')}" />

            <c:if test="${canAdd}">
                <a href="${festsRoot}/create">Add new festival</a>
            </c:if>

            <table class="listTable">
                <c:forEach items="${festivalList}" var="item">
                    <tr>
                        <td>${item.place}</td>
                        <td>
                            ${item.start} - ${item.end} (Added by ${item.addedBy})<br />
                            ${item.details}
                        </td>
                        <c:set var="isOwner" value="${user.id.equals(item.ownerId)}" />
                        <c:if test="${canEdit || canDelete || isOwner}">
                            <td>
                                <c:if test="${canEdit || isOwner}">
                                    <a href="${festsRoot}/edit?id=${item.id}">Edit</a>
                                </c:if>
                                <c:if test="${canDelete}">
                                    <a class="noHref" onclick="deleteFestival(${item.id}, '${item.start} - ${item.end} - ${item.place}');">Delete</a>
                                </c:if>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
            </table>

        </div>
    </div>

    <form id="deleteFestivalForm" action="${festsRoot}/delete" method="post">
        <input id="deleteFestivalCsrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input id="deleteFestivalId" type="hidden" name="id" />
    </form>

</body>
</html>
