<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="security" class="com.aspect.calendar.config.WebSecurity"/>
<c:set var="hasAdminRole" value="${security.hasRole('ADMIN')}"/>
<%@ page import="com.aspect.calendar.entity.enums.CalendarItemType" %>


<div class="toggle hidden">
    <div class="toggle-content">
        <div class="toggle-content-header">
            <span>Create item</span>
            <span class="toggle_close_btn">×</span>
        </div>
        <form class="item-form j-calendar-item-form" id="addNewItem" action="${pageContext.request.contextPath}/ajax/addNewItem" method="post">
            <div class="form_row">
                <label class="title" for="groupId">Project</label>
                <div class="form_row_rc">
                    <select id="groupId" name="groupId" data-url="${pageContext.request.contextPath}/ajax/projectList" required></select>
                </div>
            </div>
            <div class="form_row hidden">
                <label class="title" for="title">Title</label>
                <div class="form_row_rc">
                    <input id="title" type="text" name="title" required disabled>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="userName">User name</label>
                <div class="form_row_rc">
                    <input type="hidden" name="providerId" value="${provider.id}">
                    <input type="text" id="userName" value="${provider.fullName} (${ provider.division.toString()})" readonly>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="manager">Manager</label>
                <div class="form_row_rc">
                    <select id="manager" name="managerId">
                        <c:forEach var="manager" items="${managers}">
                            <option ${loggedUser.id == manager.id ? 'selected' : ''} value="${manager.id}">${manager.fullName}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="type">Type</label>
                <div class="form_row_rc">
                    <select id="type" name="type">
                        <c:forEach var="type" items="${CalendarItemType.values()}">
                            <option value="${type}">${type.title}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="startDate">Start date</label>
                <div class="form_row_rc">
                    <input class="date" min="${hasAdminRole ? '' : currentDate}" value="${itemDate}" id="startDate" type="date" name="startDate" required />
                    <div class="rc_block">
                        <input class="input-number" type="number" name="startDateHour" required min="7" max="22" step="1" value="${itemStartHour}">
                        <span class="time-delimiter">:</span>
                        <input class="input-number" type="number" name="startDateMinute" required min="0" max="55" step="5" value="${itemStartMinute}">
                    </div>
                    <div class="rc_block">
                        <input type="checkbox" name="excludeLunchtime" value="true" checked="checked">
                        <span class="checkbox_title">Exclude lunchtime</span>
                    </div>
                </div>
            </div>
            <div class="form_row hidden">
                <label class="title" for="endDate">End date</label>
                <div class="form_row_rc">
                    <input class="date" min="${hasAdminRole ? '' : currentDate}" value="${itemDate}" id="endDate" type="date" name="endDate" required disabled />
                    <div class="rc_block">
                        <input class="input-number" type="number" name="endDateHour" required min="7" max="22" step="1" value="${itemStartHour}" disabled>
                        <span class="time-delimiter">:</span>
                        <input class="input-number" type="number" name="endDateMinute" required min="0" max="55" step="5" value="${itemStartMinute}" disabled>
                    </div>
                    <div class="rc_block">
                        <i class="switch-link deadline">Set duration</i>
                    </div>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="duration">Duration</label>
                <div class="form_row_rc">
                    <input class="input-number" type="number" id="duration" name="durationHours" required min="0" max="999" value="0">
                    <span class="time-delimiter">:</span>
                    <input class="input-number" type="number" name="durationMinutes" required min="0" max="55" step="5" value="30">
                    <div class="rc_block">
                        <i class="switch-link duration">Set deadline</i>
                    </div>
                </div>
            </div>
            <div class="form_row hidden" id="repetitions">
                <label class="title" for="repetitions">Repetitions</label>
                <div class="form_row_rc">
                    <div class="checkbox_item">
                        <input type="checkbox" name="repetitionDays" value="1">Mon
                    </div>
                    <div class="checkbox_item">
                        <input type="checkbox" name="repetitionDays" value="2">Tue
                    </div>
                    <div class="checkbox_item">
                        <input type="checkbox" name="repetitionDays" value="3">Wed
                    </div>
                    <div class="checkbox_item">
                        <input type="checkbox" name="repetitionDays" value="4">Thu
                    </div>
                    <div class="checkbox_item">
                        <input type="checkbox" name="repetitionDays" value="5">Fri
                    </div>
                </div>
            </div>

            <div class="form_row big_fr">
                <label class="title" for="description">Description</label>
                <div class="form_row_rc">
                    <textarea class="description_textarea" id="description" name="description" rows="4" maxlength="250"></textarea>
                </div>
            </div>
        </form>
        <div class="submit-section">
            <span class="hidden" id="error-section"></span>
            <button id="addItemSubmit" class="toggle-submit-btn" disabled>Save</button>
        </div>
    </div>
</div>
