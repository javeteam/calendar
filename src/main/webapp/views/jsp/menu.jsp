<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="security" class="com.aspect.calendar.config.WebSecurity"/>

<div class="menu">
    <h3>Providers calendar</h3>
    <div class="right-pane">
        <span>${security.authenticatedUser.fullName}</span>
        <a href="${pageContext.request.contextPath}/logout">
            <img class="fa-icon" src="${pageContext.request.contextPath}/assets/icons/power-off.svg">
        </a>
    </div>

</div>
