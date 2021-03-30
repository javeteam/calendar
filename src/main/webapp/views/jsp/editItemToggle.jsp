<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="security" class="com.aspect.calendar.config.WebSecurity"/>
<c:set var="hasAdminRole" value="${security.hasRole('ADMIN')}"/>

<div class="toggle">
    <div class="toggle-content">
        <div class="toggle-content-header">
            <span>Modify item</span>
            <span class="toggle_close_btn">×</span>
        </div>
        <form class="item-form" id="editItem" action="${pageContext.request.contextPath}/ajax/editItem" method="post">
            <input type="hidden" name="id" value="${item.id}">
            <input type="hidden" name="groupId" value="${item.groupId}">
            <div class="form_row">
                <label class="title" for="title">Title</label>
                <div class="form_row_rc">
                    <input id="title" type="text" name="title" value="${item.title}" required>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="userName">User name</label>
                <div class="form_row_rc">
                    <input type="hidden" name="providerId" value="${item.providerId}">
                    <input type="text" id="userName" value="${item.providerName}" readonly>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="manager">Manager</label>
                <div class="form_row_rc">
                    <select id="manager" name="managerId">
                        <c:forEach var="manager" items="${managers}">
                            <option ${item.managerId == manager.id ? 'selected' : ''} value="${manager.id}">${manager.fullName}</option>
                        </c:forEach>

                    </select>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="type">Type</label>
                <div class="form_row_rc">
                    <select id="type" name="type">
                        <option selected hidden value="${item.type}">${item.type.toString()}</option>
                        <option value="CONFIRMED">Confirmed</option>
                        <option value="POTENTIAL">Potential</option>
                        <option value="ABSENCE">Absence</option>
                        <option value="NOT_AVAILABLE">Not Available</option>
                    </select>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="startDate">Start date</label>
                <c:set var="sdEditable" value="${hasAdminRole || !item.startDatePassed ? '' : 'readonly'}"/>
                <div class="form_row_rc">
                    <input class="date" min="${hasAdminRole ? '' : item.itemDate}" value="${item.itemDate}" id="startDate" type="date" name="startDate" required ${sdEditable}/>
                    <div class="rc_block">
                        <input class="input-number" type="number" name="startDateHour" required min="7" max="23" step="1" value="${item.startHour}" ${sdEditable}>
                        <span class="time-delimiter">:</span>
                        <input class="input-number" type="number" name="startDateMinute" required min="0" max="55" step="5" value="${item.startMinute}" ${sdEditable}>
                    </div>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="endDate">End date</label>
                <div class="form_row_rc">
                    <input class="date" value="${item.itemDate}" id="endDate" type="date" name="endDate" required readonly />
                    <div class="rc_block">
                        <input class="input-number" type="number" name="endDateHour" required min="7" max="23" step="1" value="${item.deadlineHour}">
                        <span class="time-delimiter">:</span>
                        <input class="input-number" type="number" name="endDateMinute" required min="0" max="55" step="5" value="${item.deadlineMinute}">
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
