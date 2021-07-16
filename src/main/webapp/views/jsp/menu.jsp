<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="security" class="com.aspect.calendar.config.WebSecurity"/>
<%@ page import="java.time.LocalTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<div class="menu">
    <div class="left-pane">
        <a class="menu-title" href="${pageContext.request.contextPath}/calendar">Providers calendar</a>
        <c:if test="${security.hasRoles('ADMIN', 'MANAGER')}">
            <a class="menu-title" href="${pageContext.request.contextPath}/projectManagement">Project management</a>
        </c:if>
    </div>
    <div class="right-pane">
        <c:if test="${security.hasRoles('MANAGER', 'ADMIN')}">
            <div class="time-management js_tm-open-dropdown" data-url="${pageContext.request.contextPath}/ajax/getTimeManagerDropdown" data-status-url="${pageContext.request.contextPath}/ajax/timeManagerStatus">
                <img style="height: 20px; filter: invert(1); cursor: pointer" src="${pageContext.request.contextPath}/assets/icons/clock.png">
            </div>
        </c:if>
        <span>${security.authenticatedUser.fullName}</span>
        <a href="${pageContext.request.contextPath}/logout">
            <img class="fa-icon" src="${pageContext.request.contextPath}/assets/icons/power-off.svg">
        </a>
    </div>
</div>
