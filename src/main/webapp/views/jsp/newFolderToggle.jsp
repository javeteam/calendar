<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="security" class="com.aspect.calendar.config.WebSecurity"/>
<c:set var="hasAdminRole" value="${security.hasRole('ADMIN')}"/>


<div class="toggle new-folder-toggle hidden">
    <div class="toggle-content">
        <div class="toggle-content-header">
            <span>Create new folder</span>
            <span class="toggle_close_btn">×</span>
        </div>
        <form class="item-form" id="createNewFolder" action="${pageContext.request.contextPath}/ajax/addNewFolder" method="post">
            <div class="form_row">
                <label class="title" for="client">Client</label>
                <div class="form_row_rc">
                    <select id="client" name="client" required>
                        <option></option>
                        <c:forEach var="client" items="${clients}">
                            <option value="${client}">${client}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="workflow">Workflow</label>
                <div class="form_row_rc">
                    <select id="workflow" name="workflow" required>
                        <option></option>
                        <c:forEach var="workflow" items="${workflows}">
                            <option value="${workflow}">${workflow}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="sLang">Source language</label>
                <div class="form_row_rc">
                    <select id="sLang" name="sourceLanguage" required>
                        <option value=""></option>
                        <c:forEach var="language" items="${languages}">
                            <option value="${language}">${language}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="tLang">Target languages</label>
                <div class="form_row_rc">
                    <select id="tLang" name="targetLanguages" multiple required>
                        <option value=""></option>
                        <c:forEach var="language" items="${languages}">
                            <option value="${language}">${language}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="form_row">
                <label class="title" for="clientEmailSubject">Email subject</label>
                <div class="form_row_rc">
                    <input id="clientEmailSubject" type="text" value=" " name="clientEmailSubject">
                </div>
            </div>
        </form>
        <div class="submit-section">
            <span class="hidden" id="error-section"></span>
            <button id="addFolderSubmit" class="toggle-submit-btn" disabled>Save</button>
        </div>
    </div>
</div>
