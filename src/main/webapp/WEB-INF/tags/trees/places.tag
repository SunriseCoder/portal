<%@ tag description="recursive places tree node" %>
<%@ attribute name="items" type="java.util.List" required="true" %>
<%@ attribute name="indent" type="java.lang.Integer" required="true" %>
<%@ attribute name="canEdit" type="java.lang.Boolean" required="true" %>
<%@ attribute name="placesRoot" type="java.lang.String" required="true" %>

<%@ taglib tagdir="/WEB-INF/tags/trees/" prefix="t" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:forEach var="place" items="${items}">

    <div style="margin-left: ${indent}px;">
        <table class="listTable">
            <tr>
                <td>${place.name}</td>
                <c:if test="${canEdit}">
                    <td><a href="${placesRoot}/add?id=${place.id}">Add</a></td>
                    <td><a href="${placesRoot}/edit?id=${place.id}">Edit</a></td>
                    <td><a class="noHref" onclick="deletePlace(${place.id}, '${place.path}');">Delete</a></td>
                </c:if>
            </tr>
        </table>
    </div>

    <c:if test="${not empty place.children}">
        <t:places items="${place.children}" indent="${indent + 50}" canEdit="${canEdit}" placesRoot="${placesRoot}"/>
    </c:if>

</c:forEach>
