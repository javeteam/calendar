<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="com.aspect.calendar.utils.CommonUtils" %>
<%@ page import="java.time.format.TextStyle" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.lang.Math" %>

<html>
<head>
    <title>Project management</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/common.css"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/select2.css"/>
</head>
<body>
<jsp:include page="menu.jsp"/>
<div class="document project-management">
    <form id="projectManagementFilterForm" class="option-box">
        <div class="option-item">
            <label for="dateFrom">Date From:</label>
            <input id="dateFrom" type="date" value="${dateFrom}" name="dateFrom">
        </div>
        <div class="option-item">
            <label for="dateTo">Date To:</label>
            <input id="dateTo" type="date" value="${dateTo}" name="dateTo">
        </div>
        <div class="option-item">
            <label for="manager">Manager :</label>
            <select id="manager" name="manager">
                <option value="0">All managers</option>
                <c:forEach var="manager" items="${managers}">
                    <option ${managerId == manager.id ? 'selected' : ''} value="${manager.id}">${manager.fullName}</option>
                </c:forEach>
            </select>
        </div>
        <div class="option-item">
            <button disabled type="submit">Apply filter</button>
        </div>
    </form>
    <div class="projects-wrapper">
        <c:forEach var="project" items="${projects}">
            <div class="project-row">
                <div class="project-information" data-project-id="${project.id}">
                    <span class="project-name" data-url="${pageContext.request.contextPath}/ajax/editProjectToggle">${project.name}</span>
                    <span>${project.createdBy.fullName}</span>
                    <div class="project-parameters">
                        <span class="${project.fewTranslatorsAllowed ? '' : 'inactive'}">Few TR</span>
                        <span class="${project.fewQCAllowed ? '' : 'inactive'}">Few QC</span>
                        <span class="${project.xtrfId != 0 ? '' : 'inactive'}">In XTRF</span>
                    </div>
                    <c:if test="${project.hasJobsWithDifferentValues()}">
                        <span class="refresh-info" data-url="${pageContext.request.contextPath}/ajax/refreshProjectData">↻</span>
                    </c:if>
                </div>
                <div class="project-jobs-section">
                    <c:forEach var="job" items="${project.jobs}">
                        <c:set var="itemsInfo" value=""/>
                        <c:forEach var="item" items="${job.calendarItems}">
                            <c:set var="itemsInfo" value="${itemsInfo} ${Math.round(item.duration / 36) / 100} - ${item.itemDate.format(CommonUtils.dtFormatter)} (${item.startDate} - ${item.deadline})&#13;"/>
                        </c:forEach>
                        <div class="project-job">
                            <div class="job-provider">
                                <span class="provider-name">${job.provider.fullName}</span>
                                <c:if test="${job.calendarItems.size() > 0}">
                                    <span class="calendar-items-info" title="${itemsInfo}">ⓘ</span>
                                </c:if>
                            </div>
                            <div class="job-units">
                                <c:set var="valuesDifferent" value="${job.valuesDifferent()}"/>
                                <div class="job-units-item">
                                    <div class="header ${valuesDifferent ? 'incorrect' : ''}">XTRF</div>
                                    <div class="value">${job.xtrfDuration}</div>
                                </div>
                                <div class="job-units-item">
                                    <div class="header ${valuesDifferent ? 'incorrect' : ''}">CAL</div>
                                    <div class="value">${job.calendarDuration}</div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </c:forEach>

    </div>
</div>
<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/jquery-3.3.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/select2.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/common.js"></script>
</body>
</html>
