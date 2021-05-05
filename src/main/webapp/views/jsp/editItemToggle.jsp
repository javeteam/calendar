<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="security" class="com.aspect.calendar.config.WebSecurity"/>
<%@ page import="com.aspect.calendar.entity.enums.CalendarItemType" %>
<c:set var="hasAdminRole" value="${security.hasRole('ADMIN')}"/>
<c:set var="isProject" value="${item.group.type == CalendarItemType.PROJECT}"/>

<div class="toggle">
    <div class="toggle-content">
        <div class="toggle-content-header">
            <span>Modify item</span>
            <span class="toggle_close_btn">×</span>
        </div>
        <form class="item-form j-calendar-item-form" id="editItem" action="${pageContext.request.contextPath}/ajax/editItem" method="post">
            <input type="hidden" name="id" value="${item.id}">
            <input type="hidden" name="groupId" value="${item.group.id}" ${isProject ? 'disabled' : ''}>
            <div class="form_row ${isProject ? '' : 'hidden'}">
                <label class="title" for="groupId">Project</label>
                <div class="form_row_rc">
                    <select id="groupId" name="groupId" data-url="${pageContext.request.contextPath}/ajax/projectList" required ${isProject ? '' : 'disabled'}>
                        <option selected value="${item.group.id}">${item.group.name}</option>
                    </select>
                </div>
            </div>
            <div class="form_row ${isProject ? 'hidden' : ''}">
                <label class="title" for="title">Title</label>
                <div class="form_row_rc">
                    <input id="title" type="text" name="title" value="${item.group.name}" required ${isProject ? 'disabled' : ''}>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="userName">User name</label>
                <div class="form_row_rc">
                    <input type="hidden" name="providerId" value="${item.provider.id}">
                    <input type="text" id="userName" value="${item.provider.fullName}" readonly>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="manager">Manager</label>
                <div class="form_row_rc">
                    <select id="manager" name="managerId">
                        <c:forEach var="manager" items="${managers}">
                            <option ${item.manager.id == manager.id ? 'selected' : ''} value="${manager.id}">${manager.fullName}</option>
                        </c:forEach>

                    </select>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="type">Type</label>
                <div class="form_row_rc">
                    <select id="type" name="type">
                        <c:forEach var="type" items="${CalendarItemType.values()}">
                            <option ${item.group.type == type ? 'selected' : ''} value="${type}">${type.title}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="startDate">Start date</label>
                <c:set var="sdEditable" value="${hasAdminRole || !item.startDatePassed() ? '' : 'readonly'}"/>
                <div class="form_row_rc">
                    <input class="date" min="${hasAdminRole ? '' : item.itemDate}" value="${item.itemDate}" id="startDate" type="date" name="startDate" required ${sdEditable}/>
                    <div class="rc_block">
                        <input class="input-number" type="number" name="startDateHour" required min="7" max="23" step="1" value="${item.startDate.hour}" ${sdEditable}>
                        <span class="time-delimiter">:</span>
                        <input class="input-number" type="number" name="startDateMinute" required min="0" max="55" step="5" value="${item.startDate.minute}" ${sdEditable}>
                    </div>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="endDate">End date</label>
                <div class="form_row_rc">
                    <input class="date" value="${item.itemDate}" id="endDate" type="date" name="endDate" required readonly />
                    <div class="rc_block">
                        <input class="input-number" type="number" name="endDateHour" required min="7" max="23" step="1" value="${item.deadline.hour}">
                        <span class="time-delimiter">:</span>
                        <input class="input-number" type="number" name="endDateMinute" required min="0" max="55" step="5" value="${item.deadline.minute}">
                    </div>
                </div>
            </div>
            <div class="form_row big_fr">
                <label class="title" for="description">Description</label>
                <div class="form_row_rc">
                    <textarea class="description_textarea" id="description" name="description" rows="4" maxlength="250">${item.description}</textarea>
                </div>
            </div>
        </form>
        <div class="submit-section">
            <span class="hidden" id="error-section"></span>
            <button id="editItemSubmit" class="toggle-submit-btn" disabled>Save</button>
        </div>
    </div>
</div>
