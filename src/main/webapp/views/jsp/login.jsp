<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

    <head>
        <title>Вхід</title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/common.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/login.css"/>
    </head>
    <body>
    <div class="container">
        <div class="page-header">
            <h1>Provider calendar</h1>
        </div>
        <div class="login-area">
            <form action="${pageContext.request.contextPath}/j_spring_security_check" method="POST">
                <div class="form-group">
                    <label for="login">User name</label>
                    <input type="text" name="login" id="login" placeholder="Login" required>
                </div>
                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" name="password" id="password" placeholder="Password" required>
                </div>
                <button class="login-btn" type="submit">Log in</button>
            </form>
        </div>

    </div>



    </body>
</html>

