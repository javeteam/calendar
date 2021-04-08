package com.aspect.calendar.service;

import com.aspect.calendar.config.WebSecurity;
import com.aspect.calendar.dao.ProjectEntitiesDao;
import com.aspect.calendar.entity.AppConfig;
import com.aspect.calendar.entity.exceptions.FolderCreationException;
import com.aspect.calendar.entity.exceptions.InvalidValueException;
import com.aspect.calendar.entity.user.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectEntitiesService {
    private final ProjectEntitiesDao projectEntitiesDao;
    private final NotificationService notificationService;
    private final AppConfig config;
    private final Logger logger;

    @Autowired
    public ProjectEntitiesService(ProjectEntitiesDao projectEntitiesDao, NotificationService notificationService, AppConfig config){
        this.projectEntitiesDao = projectEntitiesDao;
        this.notificationService = notificationService;
        this.config = config;
        this.logger = LoggerFactory.getLogger(ProjectEntitiesService.class);
    }

    public List<String> getLanguages(){
        return this.projectEntitiesDao.getLanguages();
    }

    public List<String> getClients(){
        return this.projectEntitiesDao.getClients();
    }

    public List<String> getWorkflows(){
        return this.projectEntitiesDao.getWorkflows();
    }

    public String createFolder(String clientName, String workflow, String sourceLanguage, String[] targetLanguages, String clientEmailSubject) throws FolderCreationException {
        clientEmailSubject = clientEmailSubject == null ? "" : clientEmailSubject.trim();
        WebSecurity security = new WebSecurity();
        AppUser user = security.getAuthenticatedUser();
        if(user == null || user.getName() == null || user.getSurname() == null) throw new FolderCreationException("Application user not defined");
        Path link;

        EntitiesHandler handler = new EntitiesHandler(this.projectEntitiesDao);
        clientName = handler.validateClientName(clientName);
        workflow = handler.validateWorkflow(workflow);
        String sLanguage = handler.validateSourceLanguage(sourceLanguage);
        String tLanguages = handler.validateTargetLanguages(targetLanguages);
        String projectDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        String projectNumber = this.projectEntitiesDao.getNextProjectNumber();

        Path projectPath = config.projectsRoot.resolve(projectDate).resolve(projectNumber);
        try{
            Files.createDirectories(projectPath.getParent(), config.UNIX_CHMOD_755);
            Files.createDirectory(projectPath, config.UNIX_CHMOD_775);
            copyDataToFolderFromTemplate(projectPath);
            link = config.pmRoot.resolve(user.getSurname() + "_" + user.getName());
            if(Files.notExists(link)) Files.createDirectories(link, config.UNIX_CHMOD_755);
            link = link.resolve(LocalDate.now().toString());
            if(Files.notExists(link)) Files.createDirectory(link, config.UNIX_CHMOD_775);
            link = link.resolve(projectDate + "_" + projectNumber + "_" + workflow + "_" + sLanguage + "-" + tLanguages + "_" + clientName);
            if(Files.notExists(link)) Files.createSymbolicLink(link, projectPath);
        } catch (IOException ex){
            String message = "Error during folders creation";
            logger.error(message, ex);
            throw new FolderCreationException(message);
        }
        handler.saveNewEntities();
        handler.updateUsageCounter(clientName);
        String message = projectNumber + " was allocated by " + user.getFullName() + (clientEmailSubject.isBlank() ? " " : " for " + clientEmailSubject);
        notificationService.sendMessage(message);

        String url = config.pmRoot.relativize(link).toString();
        return "{\"status\": \"Success\", \"url\": \"" + url + "\", \"projectName\": \"" + projectDate + "_" + projectNumber + "\"}";
    }

    private void copyDataToFolderFromTemplate(Path targetFolder) throws IOException{
        Files.walk(config.projectTemplate)
                .forEach(source -> {
                    Path relPath = config.projectTemplate.relativize(source);
                    if(!relPath.toString().isBlank()){
                        Path destination = targetFolder.resolve(relPath);
                        try {
                            Files.copy(source, destination);
                        } catch (IOException ex) {
                            logger.warn("Failed to copy files from project template", ex);
                        }
                    }
                });
    }


    private static class EntitiesHandler{
        private final ProjectEntitiesDao projectEntitiesDao;
        private String clientName;
        private String workflow;
        private final List<String> languages = new ArrayList<>();

        public EntitiesHandler(ProjectEntitiesDao projectEntitiesDao){
            this.projectEntitiesDao = projectEntitiesDao;
        }

        public String validateClientName(String clientName) throws InvalidValueException {
            if(clientName == null || clientName.trim().length() < 3) throw new InvalidValueException("Client name is invalid");
            if(clientName.indexOf('#') == 0){
                this.clientName = clientName.substring(1);
                return this.clientName;
            } else return clientName;
        }

        public String validateWorkflow(String workflow) throws InvalidValueException {
            if(workflow == null || workflow.trim().length() < 2) throw new InvalidValueException("Workflow is invalid");
            if(workflow.indexOf('#') == 0){
                this.workflow = workflow.substring(1);
                return this.workflow;
            } else return workflow;
        }

        public String validateSourceLanguage(String sourceLanguage) throws InvalidValueException{
            return validateLanguage(sourceLanguage);
        }

        public String validateTargetLanguages(String[] targetLanguages) throws InvalidValueException{
            if(targetLanguages == null || targetLanguages.length == 0) throw new InvalidValueException("At least one target language required");
            StringBuilder tLanguages = new StringBuilder();
            for(String language : targetLanguages){
                if(tLanguages.length() > 0) tLanguages.append(',');
                tLanguages.append(validateLanguage(language));
            }

            return tLanguages.toString();
        }

        private String validateLanguage(String language) throws InvalidValueException {
            if(language == null) throw new InvalidValueException("Language can't be null");
            language = language.trim();
            if(language.length() == 3 && language.indexOf('#') == 0){
                language = language.substring(1);
              languages.add(language);
              return language;
            } else if(language.length() == 2) return language;
            else throw new InvalidValueException("Language code must fit ISO 639-1");
        }

        public void saveNewEntities(){
            if(clientName != null) this.projectEntitiesDao.addClient(clientName);
            if(workflow != null) this.projectEntitiesDao.addWorkflow(workflow);
            for(String language : languages){
                this.projectEntitiesDao.addLanguage(language);
            }
        }

        public void updateUsageCounter(String clientName){
            if(this.clientName == null) this.projectEntitiesDao.incrementClientUsageCounter(clientName);
        }
    }


}
