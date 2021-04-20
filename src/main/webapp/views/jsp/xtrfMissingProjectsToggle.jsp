<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="security" class="com.aspect.calendar.config.WebSecurity"/>
<c:set var="hasAdminRole" value="${security.hasRole('ADMIN')}"/>


<div class="toggle hidden">
    <div class="toggle-content">
        <div class="toggle-content-header">
            <span>Projects missing in XTRF</span>
            <span class="toggle_close_btn">×</span>
        </div>
        <div class="content-area">
            <table>
                <thead>
                <tr>
                    <th>#</th>
                    <th>Project name</th>
                    <th>Manager</th>
                </tr>
                </thead>
                <c:if test="${!empty missingProjects}">
                    <tbody>
                    <c:set var="userId" value="${missingProjects.get(0).createdBy.id}"/>
                    <c:forEach var="project" items="${missingProjects}" varStatus="loop">
                        <c:if test="${project.createdBy.id != userId}">
                            <tr class="division-delimiter"></tr>
                            <c:set var="userId" value="${project.createdBy.id}"/>
                        </c:if>
                        <tr>
                            <td>${loop.index + 1}</td>
                            <td>${project.name}</td>
                            <td>${project.createdBy.fullName}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </c:if>
            </table>
        </div>
    </div>
</div>
