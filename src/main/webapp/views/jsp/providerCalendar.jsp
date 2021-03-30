<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="java.time.format.TextStyle" %>
<%@ page import="java.util.Locale" %>

<html>
<head>
    <title>Personal calendar</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/common.css"/>
</head>
<body>
<jsp:include page="menu.jsp"/>
<div class="document">
    <div class="cal-option-box">
    <form class="period-selector" id="calendarPropertiesForm" action="${pageContext.request.contextPath}/calendar" method="post">
        <div class="back"><i class="css-arrow left"></i></div>
        <div class="calendar-date">
            <span>${calendarDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${calendarDate.dayOfMonth}, ${calendarDate.year}</span>
            <input id="calendarDate" type="date" name="date" value="${calendarDate}">
        </div>
        <div class="next"><i class="css-arrow right"></i></div>
    </form>
    </div>
    <div class="calendar ${calendarDate.dayOfWeek.value > 5 ? 'weekend' : 'workday'}">
        <table>
            <thead class="calendar-head">
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
            <c:set var="calendarRow" value="${calendar[1]}"/>
            <tr class="providerCalendarRow">
                <td class="userInfo">
                    <input type="hidden" value="${calendarRow.user.id}">
                    <div>
                        <span class="username">${calendarRow.user.fullName}</span>
                        <span title="${calendarRow.efficiency}" class="userEfficiency">${calendarRow.userLoad}</span>
                    </div>
                </td>
                <c:forEach var="cell" items="${calendarRow.calendarCells}">
                    <td class="${cell.workingHours ? '' : 'not-working-hours'}">
                        <input type="hidden" value="${cell.startTP}">
                        <c:forEach var="item" items="${cell.items}">
                            <div class="calendar-cell-item ${item.type.toString().toLowerCase()} ${item.groupId > 0 ? 'items_group' : ''}" style="${item.CSSStyles}">
                                <input type="hidden" name="groupId" value="${item.groupId}">
                                <form hidden action="${pageContext.request.contextPath}/ajax/itemInfo" method="post">
                                    <input type="hidden" name="itemId" value="${item.id}">
                                </form>
                                <span title="${item.period}">${item.title}</span>
                            </div>
                        </c:forEach>
                    </td>
                </c:forEach>
            </tr>
            </tbody>
        </table>
    </div>

</div>
<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/jquery-3.3.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/common.js"></script>
</body>
</html>
