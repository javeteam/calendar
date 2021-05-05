<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="com.aspect.calendar.utils.CommonUtils" %>
<%@ page import="java.time.format.TextStyle" %>
<%@ page import="java.util.Locale" %>

<html>
<head>
    <title>Providers calendar</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/common.css"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/select2.css"/>
</head>
<body>
<jsp:include page="menu.jsp"/>
<div class="document">
    <c:if test="${managerView}">
        <form id="newItemForm" hidden action="${pageContext.request.contextPath}/ajax/newItemToggle" method="post">
            <input id="newItemDate" type="date" name="itemDate" value="${calendarDate}">
            <input id="newItemStartDate" type="number" name="startTP" value="0">
            <input id="newItemUserId" type="text" name="providerId" value="0">
        </form>
    </c:if>
    <div class="cal-option-box">
        <div class="period-selector">
            <div class="back" data-target-date="${calendarDate.minusDays(1)}"><i class="css-arrow left"></i></div>
            <div class="calendar-date">
                <span>${calendarDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${calendarDate.dayOfMonth}, ${calendarDate.year}</span>
                <input id="calendarDate" type="date" name="date" value="${calendarDate}">
            </div>
            <div class="next" data-target-date="${calendarDate.plusDays(1)}"><i class="css-arrow right"></i></div>
            <c:if test="${managerView}">
                <select id="responsibleManager" name="responsibleManager">
                    <option value="">All managers</option>
                    <c:forEach var="manager" items="${managers}">
                        <option ${responsibleManager == manager.id ? 'selected' : ''} value="${manager.id}">${manager.fullName}</option>
                    </c:forEach>
                </select>
            </c:if>
        </div>
        <c:if test="${managerView}">
            <div class="img-button statistic-button">
                <img src="${pageContext.request.contextPath}/assets/icons/statistic.png" alt="Report"/>
                <form hidden action="${pageContext.request.contextPath}/ajax/statisticParamsToggle" method="post"></form>
            </div>
            <div class="img-button missing-projects">
                <img src="${pageContext.request.contextPath}/assets/icons/missing.png" alt="Missing projects"/>
                <form hidden action="${pageContext.request.contextPath}/ajax/getXtrfMissingProjects" method="post"></form>
            </div>
            <div class="img-button new-folder-button">
                <img src="${pageContext.request.contextPath}/assets/icons/folder.png" alt="Add folder"/>
                <form hidden action="${pageContext.request.contextPath}/ajax/newFolderToggle" method="post"></form>
            </div>
        </c:if>
    </div>
    <div class="calendar ${calendarDate.dayOfWeek.value > 5 ? 'weekend' : 'workday'}">
        <table>
            <thead>
            <tr>
                <th class="calendar-head">
                    <span class="dayOfWeek">${calendarDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}</span>
                </th>
                <c:forEach var="cell" items="${calendar[0].calendarCells}">
                    <th class="calendar-head">
                        <span class="cell_time">${cell.time}</span>
                        <c:if test="${currentTP >= cell.startTP && currentTP < cell.startTP + cell.defaultDuration}">
                            <c:set var="left" value="${(currentTP - cell.startTP) * 100 / cell.defaultDuration}"/>
                            <div class="current-time-line" style="left: ${left}%"></div>
                        </c:if>
                    </th>
                </c:forEach>
            </tr>
            </thead>
            <tbody>
            <c:set var="userRole" value=""/>
            <c:forEach var="calendarRow" items="${calendar}" varStatus="loop">
                <c:if test="${loop.index > 0}">
                    <c:if test="${userRole != calendarRow.user.division}">
                        <c:if test="${not empty userRole}">
                            <tr class="division-delimiter"></tr>
                        </c:if>
                        <c:set var="userRole" value="${calendarRow.user.division}"/>
                    </c:if>
                    <tr ${managerView ? '' : 'class="providerCalendarRow"'}>
                        <td class="userInfo">
                            <input type="hidden" value="${calendarRow.user.id}">
                            <div>
                                <span class="username">${calendarRow.user.fullName}</span>
                                <span title="${calendarRow.efficiency}" class="userEfficiency">${calendarRow.userLoad} </span>
                            </div>
                        </td>
                        <c:forEach var="cell" items="${calendarRow.calendarCells}">
                            <td class="cell ${cell.workingHours ? '' : 'not-working-hours'} ${managerView ? 'j_cell' : ''}">
                                <input type="hidden" value="${cell.startTP}">
                                <c:forEach var="item" items="${cell.items}" varStatus="loop">
                                    <div class="calendar-cell-item ${item.group.size > 1 ? 'items_group' : ''} ${(managerView && item.group.type == 'PROJECT' && item.group.hasCollision) ? 'invalid' : item.group.type.toString().toLowerCase()}" style="${CommonUtils.getCSSStyles(cell, loop.index) }">
                                        <input type="hidden" name="groupId" value="${item.group.id}">
                                        <form hidden action="${pageContext.request.contextPath}/ajax/itemInfo" method="post">
                                            <input type="hidden" name="itemId" value="${item.id}">
                                        </form>
                                        <span title="${item.period}&#013;${item.group.name}">${item.group.shortName}</span>
                                    </div>
                                </c:forEach>
                            </td>
                        </c:forEach>
                    </tr>
                </c:if>
            </c:forEach>
            </tbody>
        </table>
    </div>

</div>
<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/jquery-3.3.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/select2.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/common.js"></script>
</body>
</html>
