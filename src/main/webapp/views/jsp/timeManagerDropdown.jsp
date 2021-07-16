<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="java.time.LocalTime" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.TextStyle" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<c:set var="timeFormatter" value="${DateTimeFormatter.ofPattern('HH:mm')}"/>

<div class="dropdown">
    <c:forEach var="overdueAttendance" items="${overdueAttendance}">
        <div class="attendance">
            <c:set var="fullDate" value="${overdueAttendance.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)}, ${overdueAttendance.date.dayOfMonth} ${overdueAttendance.date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}"/>
            <span class="date">${overdueAttendance.date.isEqual(LocalDate.now().minusDays(1)) ? 'Yesterday' : fullDate}</span>
            <div class="information-block">
                <div class="information-block-item">
                    <span>Clock in</span>
                    <span class="time clock-in-time">${overdueAttendance.clockIn.format(timeFormatter)}</span>
                </div>
                <div class="information-block-item">
                    <span>Clock out</span>
                    <span class="time clock-out-time">--:--</span>
                </div>
            </div>
            <div class="action-block" data-url="${pageContext.request.contextPath}/ajax/updateAttendance">
                <input type="hidden" name="id" value="${overdueAttendance.id}">
                <input type="time" name="time" min="${overdueAttendance.clockIn.format(timeFormatter)}" value="${overdueAttendance.clockIn.plusHours(9).isBefore(overdueAttendance.clockIn) ? '23:59' : overdueAttendance.clockIn.plusHours(9)}"/>
                <button>
                    <span>Clock Out</span>
                    <img src="${pageContext.request.contextPath}/assets/icons/tick.png">
                </button>
            </div>
        </div>
    </c:forEach>
    <div class="attendance today-attendance">
        <span class="date">Today</span>
        <div class="information-block">
            <div class="information-block-item">
                <span>Clock in</span>
                <span class="time clock-in-time">${todayAttendance.clockIn == null ? '--:--' : todayAttendance.clockIn.format(timeFormatter)}</span>
            </div>
            <div class="information-block-item">
                <span>Clock out</span>
                <span class="time clock-out-time">${todayAttendance.clockOut == null ? '--:--' : todayAttendance.clockOut.format(timeFormatter)}</span>
            </div>
        </div>
        <div class="action-block" data-url="${pageContext.request.contextPath}/ajax/updateAttendance">
            <c:set var="actionImposible" value="${todayAttendance.clockOut != null || (todayAttendance.clockIn != null && todayAttendance.clockIn.plusMinutes(30).isAfter(LocalTime.now()))}"/>
            <input type="hidden" name="id" value="${todayAttendance.id}">
            <input type="time" name="time" min="${todayAttendance.clockIn == null ? '' : todayAttendance.clockIn.format(timeFormatter)}" value="${LocalTime.now().format(timeFormatter)}" ${actionImposible ? 'style="visibility: hidden" readonly' : ''} />
            <button ${actionImposible ? 'disabled' : '' }>
                <span>${todayAttendance.clockIn == null ? 'Clock In' : 'Clock Out'}</span>
                <img src="${pageContext.request.contextPath}/assets/icons/tick.png">
            </button>
        </div>
    </div>
</div>