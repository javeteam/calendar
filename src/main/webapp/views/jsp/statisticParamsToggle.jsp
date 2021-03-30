<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="toggle hidden">
    <div class="toggle-content">
        <div class="toggle-content-header">
            <span>Workload report</span>
            <span class="toggle_close_btn">×</span>
        </div>
        <form class="item-form" id="reportForm" action="${pageContext.request.contextPath}/ajax/reportPreview" method="post">
            <div class="form_row">
                <label class="title" for="dateFrom">Start date</label>
                <div class="form_row_rc">
                    <input class="date" id="dateFrom" type="date" value="${lastMonthStart}" name="dateFrom" required />
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="dateTo">Start date</label>
                <div class="form_row_rc">
                    <input class="date" value="${lastMonthEnd}" id="dateTo" type="date" name="dateTo" required />
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="providerId">Type</label>
                <div class="form_row_rc">
                    <select id="providerId" name="providerId" style="width: 100%">
                        <option value="0">All users</option>
                        <c:forEach var="division" items="${divisions}">
                            <optgroup label="${division[0].division.toString()}">
                                <c:forEach var="provider" items="${division}">
                                    <option value="${provider.id}">${provider.fullName}</option>
                                </c:forEach>
                            </optgroup>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="form_row">
                <div class="form_row_rc j_checkbox_wrapper">
                    <input class="j_checkbox" type="checkbox" id="itemsExport" data-url="${pageContext.request.contextPath}/exportItemsAsCSV">
                    <label class="j_checkbox_label" for="itemsExport">Export records to CSV</label>
                </div>
            </div>
        </form>
        <div class="submit-section">
            <span class="hidden" id="error-section"></span>
            <button id="reportSubmitBtn" class="toggle-submit-btn">Save</button>
        </div>
    </div>
</div>
