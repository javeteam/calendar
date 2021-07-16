package com.aspect.calendar.controllers;

import com.aspect.calendar.entity.calendar.Project;
import com.aspect.calendar.entity.exceptions.AuthenticationRequiredException;
import com.aspect.calendar.entity.exceptions.EntityNotExistException;
import com.aspect.calendar.entity.exceptions.FolderCreationException;
import com.aspect.calendar.service.ProjectEntitiesService;
import com.aspect.calendar.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.mapping.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
public class ProjectController extends PlainController{
    private final ProjectEntitiesService projectEntitiesService;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public ProjectController(ProjectEntitiesService projectEntitiesService, UserDetailsServiceImpl userDetailsService) {
        this.projectEntitiesService = projectEntitiesService;
        this.userDetailsService = userDetailsService;
    }

    @RequestMapping(value = {"/ajax/projectList"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getProjectList(String name){
        try {
            List<Project> projects = this.projectEntitiesService.getProjectsByName(name);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.AUTO_DETECT_CREATORS,
                    MapperFeature.AUTO_DETECT_FIELDS,
                    MapperFeature.AUTO_DETECT_GETTERS,
                    MapperFeature.AUTO_DETECT_IS_GETTERS);

            return "{\"items\":" + mapper.writeValueAsString(projects) + "}";
        } catch (JsonProcessingException ex){
            return "{}";
        }
    }

    @RequestMapping(value = {"/ajax/newFolderToggle"}, method = RequestMethod.POST)
    public String getNewFolderToggle(Model model){
        model.addAttribute("clients", this.projectEntitiesService.getClients());
        model.addAttribute("workflows", this.projectEntitiesService.getWorkflows());
        model.addAttribute("languages", this.projectEntitiesService.getLanguages());
        return "newFolderToggle";
    }

    @RequestMapping(value = {"/ajax/addNewFolder"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createFolder(String client, String workflow, String sourceLanguage, String[] targetLanguages, String clientEmailSubject, HttpServletResponse response){
        try{
            return this.projectEntitiesService.createFolder(client, workflow, sourceLanguage, targetLanguages, clientEmailSubject);
        } catch (FolderCreationException ex){
            response.setStatus(406);
            return "{\"error\":\"" + ex.getMessage() + "\"}";
        }
    }

    @RequestMapping(value = {"/ajax/getXtrfMissingProjects"}, method = RequestMethod.POST)
    public String getAbsentXtrfProjects(Model model){
        model.addAttribute("missingProjects", this.projectEntitiesService.getXtrfMissingProjects());

        return "xtrfMissingProjectsToggle";
    }

    @RequestMapping(value = {"/projectManagement"}, method = RequestMethod.GET)
    public String getProjectManager(@RequestParam(value = "dateFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
                                    @RequestParam(value = "dateTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
                                    @RequestParam(value = "manager", required = false) Integer managerId,
                                    Model model) throws AuthenticationRequiredException {

        if(dateFrom == null) dateFrom = LocalDate.now().withDayOfMonth(1);
        if(dateTo == null) dateTo = dateFrom.withDayOfMonth(dateFrom.lengthOfMonth());
        if(managerId == null) managerId = this.getAuthenticatedUser().getId();

        model.addAttribute("projects", this.projectEntitiesService.getProjectsToManage(dateFrom, dateTo, managerId));
        model.addAttribute("managers", this.userDetailsService.getAllActiveManagers());
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("managerId", managerId);
        return "projectManagement";
    }

    @RequestMapping(value = {"/ajax/editProjectToggle"}, method = RequestMethod.POST)
    public String editProjectToggle(@RequestParam ("id") long id, Model model) throws EntityNotExistException {
        model.addAttribute("project", this.projectEntitiesService.getProjectById(id));

        return "editProjectToggle";
    }

    @RequestMapping(value = {"/ajax/editProject"}, method = RequestMethod.POST)
    public String editProject(Project project, Model model) throws EntityNotExistException {
        this.projectEntitiesService.updateProject(project);
        Project updatedProject = this.projectEntitiesService.getProjectToManage(project.getId());
        model.addAttribute("projects", Collections.singletonList(updatedProject));

        return "projectManagement";
    }

    @RequestMapping(value = {"/ajax/refreshProjectData"}, method = RequestMethod.POST)
    public String refreshProjectData(@RequestParam ("id") long id, Model model) throws EntityNotExistException {
        Project project = this.projectEntitiesService.refreshProjectData(id);
        model.addAttribute("projects", Collections.singletonList(project));

        return "projectManagement";
    }


}
