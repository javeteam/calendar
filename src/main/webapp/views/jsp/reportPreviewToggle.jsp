<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="toggle hidden">
    <div class="toggle-content">
        <div class="toggle-content-header">
            <span>Workload report ${report.periodDisplayValue}</span>
            <span class="toggle_close_btn">×</span>
        </div>
        <div class="content-area">
            <table>
                <thead>
                <tr>
                    <th>Full Name</th>
                    <th>Division</th>
                    <th>Fact</th>
                    <th>Planned</th>
                    <th>Idle, %</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="reportRow" items="${report.rows}">
                    <tr>
                        <td>${reportRow.person.fullName}</td>
                        <td>${reportRow.person.division.toString()}</td>
                        <td>${reportRow.fact}</td>
                        <td>${reportRow.planned}</td>
                        <td>${reportRow.idle}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <div class="submit-section">
            <c:if test="${report.rows.size() > 0}">
                <div class="option-block">
                    <div class="option">
                        <a class="export">Export</a>
                        <form hidden action="${pageContext.request.contextPath}/getCSVReport" method="post">
                            <input type="hidden" name="reportHash" value="${report.hashCode()}">
                        </form>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</div>
