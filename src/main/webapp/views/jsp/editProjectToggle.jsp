<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="security" class="com.aspect.calendar.config.WebSecurity"/>
<c:set var="hasAdminRole" value="${security.hasRole('ADMIN')}"/>

<div class="toggle">
    <div class="toggle-content">
        <div class="toggle-content-header">
            <span>Modify project</span>
            <span class="toggle_close_btn">×</span>
        </div>
        <form class="item-form" id="projectEditForm" action="${pageContext.request.contextPath}/ajax/editProject" method="post">
            <input type="hidden" name="id" value="${project.id}">
            <input type="hidden" name="xtrfId" value="${project.xtrfId}">
            <div class="form_row">
                <label class="title" for="projectName">Name</label>
                <div class="form_row_rc">
                    <input class="verifiable" id="projectName" type="text" name="name" value="${project.name}" required pattern="\d{4}_\d{4}_[A-Z]{2,}_[A-Z]{2}-[A-Z]{2}(,[A-Z]{2})*">
                    <span class="validity"></span>
                </div>
            </div>
            <div class="form_row big_fr">
                <label class="title" for="clientEmailSubject">Description</label>
                <div class="form_row_rc">
                    <textarea class="description_textarea" id="clientEmailSubject" name="clientEmailSubject" rows="4" maxlength="500">${project.clientEmailSubject}</textarea>
                </div>
            </div>
              <div class="form_row">
                <label class="title"></label>
                <div class="form_row_rc j_checkbox_wrapper">
                    <input class="j_checkbox" type="checkbox" name="fewTranslatorsAllowed" id="fewTRAllowed" ${project.fewTranslatorsAllowed ? 'checked' : ''}>
                    <label class="j_checkbox_label" for="fewTRAllowed">Few translators allowed</label>
                </div>
            </div>
            <div class="form_row">
                <label class="title"></label>
                <div class="form_row_rc j_checkbox_wrapper">
                    <input class="j_checkbox" type="checkbox" name="fewQCAllowed" id="fewQCAllowed" ${project.fewQCAllowed ? 'checked' : ''}>
                    <label class="j_checkbox_label" for="fewQCAllowed">Few QC allowed</label>
                </div>
            </div>
        </form>
        <div class="submit-section">
            <span class="hidden" id="error-section"></span>
            <button id="editProjectSubmit" class="toggle-submit-btn" disabled>Save</button>
        </div>
    </div>
</div>
