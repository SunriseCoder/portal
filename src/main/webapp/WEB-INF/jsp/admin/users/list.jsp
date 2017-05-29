<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />
<c:set var="adminRoot" value="${appRoot}/admin" />
<c:set var="usersRoot" value="${adminRoot}/users" />

<!DOCTYPE html>
<html>
<head>
    <title>User Management</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/styles/form.css" />

    <script src="${appRoot}/scripts/jquery.js"></script>
    <script src="${appRoot}/scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("admin.caption");
        });

        function confirmUser(id, name) {
            var comment = prompt("Optional comment to confirm identity of user " + name);
            if (comment != null) {
                $('#confirmId')[0].value = id;
                $('#confirmComment')[0].value = comment;
                $('#confirmUserForm')[0].submit();
            }
        }

        function unconfirmUser(id, name) {
            var confirmed = confirm("Are You sure to reject identity confirmation of user " + name);
            if (confirmed) {
                $('#unconfirmId')[0].value = id;
                $('#unconfirmUserForm')[0].submit();
            }
        }

        function lockUser(id, name) {
            var reason = prompt("Please enter lock reason for user " + name);
            if (reason != null) {
                $('#lockId')[0].value = id;
                $('#lockReason')[0].value = reason;
                $('#lockUserForm')[0].submit();
            }
        }

        function unlockUser(id, name) {
            var confirmed = confirm("Are You sure to unlock user " + name);
            if (confirmed) {
                $('#unlockId')[0].value = id;
                $('#unlockUserForm')[0].submit();
            }
        }
    </script>
</head>
<body>

    <jsp:include page="../../includes/header.jsp" />

    <jsp:include page="../adminHeader.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>User Management</h3>

            <c:if test="${not empty message}">
                <p class="success">${message}</p>
            </c:if>

            <table class="listTable">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Login</th>
                        <th>Display Name</th>
                        <th>E-Mail</th>
                        <th>Locked</th>
                        <th>Confirmed</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <c:forEach items="${userList}" var="item">
                    <tr>
                        <td>${item.id}</td>
                        <td>${item.login}</td>
                        <td>${item.displayName}</td>
                        <td>${item.email}</td>
                        <td>
                            <c:if test="${item.locked}">
                                <span class="warning" title="${item.lock.reason} (by ${item.lock.lockedBy.displayName})">Locked</span>
                            </c:if>
                        </td>
                        <td>
                            <c:if test="${item.confirmed}">
                                <span class="confirmed" title="${item.confirmation.comment} (by ${item.confirmation.confirmedBy.displayName})">Confirmed</span>
                            </c:if>
                        </td>

                        <td>
                            <a href="${usersRoot}/edit/${item.id}">Edit</a> /
                            <c:choose>
                                <c:when test="${item.locked}"><a class="noHref" onclick="unlockUser(${item.id}, '${item.displayName}');">Unlock</a></c:when>
                                <c:otherwise><a class="noHref" onclick="lockUser(${item.id}, '${item.displayName}');">Lock</a></c:otherwise>
                            </c:choose> /
                            <c:choose>
                                <c:when test="${item.confirmed}"><a class="noHref" onclick="unconfirmUser(${item.id}, '${item.displayName}');">Reject confirm</a></c:when>
                                <c:otherwise><a class="noHref" onclick="confirmUser(${item.id}, '${item.displayName}');">Confirm</a></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </table>

        </div>
    </div>

    <form id="confirmUserForm" action="${usersRoot}/confirm" method="post">
        <input id="confirmCsrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input id="confirmId" type="hidden" name="id" />
        <input id="confirmComment" type="hidden" name="comment" />
    </form>

    <form id="unconfirmUserForm" action="${usersRoot}/unconfirm" method="post">
        <input id="unconfirmCsrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input id="unconfirmId" type="hidden" name="id" />
    </form>

    <form id="lockUserForm" action="${usersRoot}/lock" method="post">
        <input id="lockCsrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input id="lockId" type="hidden" name="id" />
        <input id="lockReason" type="hidden" name="reason" />
    </form>

    <form id="unlockUserForm" action="${usersRoot}/unlock" method="post">
        <input id="unlockCsrf" type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input id="unlockId" type="hidden" name="id" />
    </form>

</body>
</html>
