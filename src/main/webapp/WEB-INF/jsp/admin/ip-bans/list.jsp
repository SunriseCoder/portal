<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="ipbanRoot" value="${adminRoot}/ip-bans" />

<!DOCTYPE html>
<html>
<head>
    <title>IP-Bans</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/styles/form.css" />

    <script src="${appRoot}/scripts/jquery.js"></script>
    <script src="${appRoot}/scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("ip-bans.caption");
        });

        function removeIP(id, ip, reason) {
            var confirmed = confirm("Are You sure to unban IP-address '" + ip + "', banned for '" + reason + "'?'");
            if (confirmed) {
                $('#removeId')[0].value = id;
                $('#removeIPForm')[0].submit();
            }
        }

    </script>
</head>
<body>

    <jsp:include page="../../includes/header.jsp" />

    <jsp:include page="../adminHeader.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>IP-Bans (Your IP is ${myIP})</h3>

            <c:if test="${not empty error}">
                <p class="error">${error}</p>
            </c:if>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <c:set var="canEdit" value="${not empty user && user.hasPermission('ADMIN_IPBAN_EDIT')}" />

            <c:if test="${canEdit}">
                <form:form id="addIPForm" action="${ipbanRoot}/add" method="post" commandName="ipBan">
                    <input id="addIpCsrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    IP: <form:input id="addIpIp" type="text" path="ip" />
                    Reason: <form:input id="addIpReason" type="text" path="reason" />
                    <input type="submit" value="Add" />
                </form:form>
            </c:if>

            <table class="listTable">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>IP-Address</th>
                        <th>Date</th>
                        <th>Banned by</th>
                        <th>Reason</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <c:forEach items="${entityList}" var="item">

                    <tr>
                        <td>${item.id}</td>
                        <td>${item.ip}</td>
                        <td>${item.date}</td>
                        <td>${item.bannedBy.displayName} (${item.bannedBy.login})</td>
                        <td>${item.reason}</td>
                        <td>
                            <c:if test="${canEdit}">
                                <a class="noHref" onclick="removeIP(${item.id}, '${item.ip}', '${item.reason}');">Remove</a>
                            </c:if>
                        </td>
                    </tr>

                </c:forEach>

            </table>

        </div>
    </div>

    <form id="removeIPForm" action="${ipbanRoot}/remove" method="post">
        <input id="removeCsrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input id="removeId" type="hidden" name="id" />
    </form>

</body>
</html>
