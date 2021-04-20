<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="security" class="com.aspect.calendar.config.WebSecurity"/>
<c:set var="hasAdminRole" value="${security.hasRole('ADMIN')}"/>
<c:set var="hasManagerRole" value="${security.hasRole('MANAGER')}"/>

<% pageContext.setAttribute("newLineChar", "\n"); %>

<div class="toggle toggle-item-info">
    <div class="toggle-content">
        <div class="toggle-content-header">
            <span class="item-title-info">${item.title}</span>
            <span class="toggle_close_btn">×</span>
        </div>
        <div class="item-info-section">
            <div class="start_and_finish">
                <span>${item.groupPeriod}</span>
            </div>
            <c:if test="${item.groupId > 0}">
                <div class="form_row">
                    <span class="title">Group:</span>
                    <span class="form_row_rc">Item ${item.positionInGroup} of ${item.groupSize}</span>
                </div>
                <div class="form_row">
                    <span class="title">Group duration:</span>
                    <span class="form_row_rc">${item.formattedGroupDuration}</span>
                </div>
            </c:if>
            <div class="form_row">
                <span class="title">Item duration:</span>
                <span class="form_row_rc">${item.formattedDuration} (${item.period})</span>
            </div>
            <c:if test="${hasAdminRole || hasManagerRole}">
                <div class="form_row">
                    <span class="title">Provider:</span>
                    <span class="form_row_rc">${item.providerName}</span>
                </div>
            </c:if>
            <div class="form_row">
                <span class="title">Manager:</span>
                <span class="form_row_rc" title="Created: ${item.createdByName} ${item.creationDate}<c:if test="${not empty item.modificationDate}">&#10;Modified: ${item.modifiedByName} ${item.modificationDate} </c:if>">${item.managerName}</span>
            </div>
            <div class="form_row">
                <span class="item-description">${fn:replace(item.description, newLineChar, "<br>")}</span>
            </div>
        </div>
        <c:if test="${hasManagerRole || hasAdminRole}">
            <div class="submit-section">
                <div class="option-block">
                    <c:if test="${hasAdminRole || !item.deadlinePassed}">
                        <div class="option">
                            <a class="edit">Edit</a>
                            <form id="editForm" hidden action="${pageContext.request.contextPath}/ajax/editItemToggle" method="post">
                                <input type="hidden" name="itemId" value="${item.id}">
                            </form>
                        </div>
                        <c:if test="${minSplitTime.isBefore(maxSplitTime)}">
                            <div class="option">
                                <a class="split">Split</a>
                            </div>
                            <div id="splitOptions" class="option-details hidden">
                                <form id="splitForm" action="${pageContext.request.contextPath}/ajax/split" method="post">
                                    <span class="title">Split point:</span>
                                    <input type="hidden" name="itemId" value="${item.id}">
                                    <input type="time" name="splitTime" min="${minSplitTime}" max="${maxSplitTime}" value="${maxSplitTime}" step="300" required>
                                    <a class="text-btn" id="splitSubmit">Save</a>
                                </form>
                                <span id="error-section" class="hidden">Value must be between ${minSplitTime} and ${maxSplitTime} and multiple of 5</span>
                            </div>
                        </c:if>
                    </c:if>
                    <c:if test="${hasAdminRole || !item.startDatePassed}">
                        <div class="option">
                            <a class="delete">Delete</a>
                            <form id="deleteForm" hidden action="${pageContext.request.contextPath}/ajax/delete" method="post">
                                <input type="hidden" name="itemId" value="${item.id}">
                                <input type="hidden" name="groupId" value="${item.groupId}">
                            </form>
                        </div>
                        <c:if test="${item.groupId > 0 && (hasAdminRole || !item.groupStartDatePassed)}">
                            <div id="deleteOptions" class="option-details hidden">
                                <a class="text-btn" id="delItem">Delete item</a>
                                <a class="text-btn" id="delGroup">Delete group</a>
                            </div>
                        </c:if>
                    </c:if>
                </div>
            </div>
        </c:if>
    </div>
</div>
