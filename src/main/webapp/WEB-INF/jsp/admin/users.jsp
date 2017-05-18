<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="appRoot" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html>
<head>
    <title>Main</title>

    <link rel="stylesheet" type="text/css" href="${appRoot}/styles/form.css" />

    <script src="${appRoot}/scripts/jquery.js"></script>
    <script src="${appRoot}/scripts/locale-utils.js"></script>

    <script>
        $(function() {
            Locales.appRoot = '${appRoot}';
            Locales.writeTitle("admin.caption");
        });
    </script>
</head>
<body>

    <jsp:include page="../includes/header.jsp" />

    <jsp:include page="admin_header.jsp" />

    <div class="container">
        <div class="starter-template">
            <h3>Users Management</h3>

            <table class="listTable">
                <thead>
                    <tr>
                        <th>ID</th><th>Login</th><th>Display Name</th><th>E-Mail</th><th>Actions</th>
                    </tr>
                </thead>
                <c:forEach items="${userList}" var="item">
                    <tr>
                        <td>${item.id}</td>
                        <td>${item.login}</td>
                        <td>${item.displayName}</td>
                        <td>${item.email }</td>
                        <td>
                            <a href="#">Details</a> /
                            <a href="#">Edit</a> /
                            <a href="#">Ban</a> /
                            <a href="#">Lock</a> /
                            <a href="#">Delete</a> /
                            <a href="#">Confirm</a>
                        </td>
                    </tr>
                </c:forEach>
            </table>

        </div>
    </div>

</body>
</html>
