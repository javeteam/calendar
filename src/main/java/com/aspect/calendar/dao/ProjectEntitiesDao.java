package com.aspect.calendar.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class ProjectEntitiesDao extends JdbcDaoSupport {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall jdbcCall;
    private final Logger logger = LoggerFactory.getLogger(ProjectEntitiesDao.class);

    @Autowired
    public ProjectEntitiesDao(DataSource dataSource){
        this.setDataSource(dataSource);
        this.jdbcTemplate = this.getJdbcTemplate();
        this.jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("getProjectNumber");
    }

    public List<String> getLanguages(){
        final String request = "SELECT name FROM language ORDER BY preferred DESC, name";
        try{
            return this.jdbcTemplate.queryForList(request, String.class);
        } catch (EmptyResultDataAccessException ignored){
            return new ArrayList<>();
        }
    }

    public void addLanguage(String langCode){
        final String request = "INSERT INTO language (name) VALUES (?)";
        try{
            this.jdbcTemplate.update(request, langCode);
        } catch (DataAccessException ex){
            logger.warn("Failed to add new language ", ex);
        }
    }

    public List<String> getClients(){
        final String request = "SELECT name FROM client ORDER BY usage_count DESC, name";
        try{
            return this.jdbcTemplate.queryForList(request, String.class);
        } catch (EmptyResultDataAccessException ignored){
            return new ArrayList<>();
        }
    }

    public void addClient(String clientName){
        final String request = "INSERT INTO client (name) VALUES (?)";
        try{
            this.jdbcTemplate.update(request, clientName);
        } catch (DataAccessException ex){
            logger.warn("Failed to add new client ", ex);
        }
    }

    public List<String> getWorkflows(){
        final String request = "SELECT name FROM workflow ORDER BY preferred DESC, name";
        try{
            return this.jdbcTemplate.queryForList(request, String.class);
        } catch (EmptyResultDataAccessException ignored){
            return new ArrayList<>();
        }
    }

    public void addWorkflow(String workflowName){
        final String request = "INSERT INTO workflow (name) VALUES (?)";
        try{
            this.jdbcTemplate.update(request, workflowName);
        } catch (DataAccessException ex){
            logger.warn("Failed to add new workflow ", ex);
        }
    }

    public String getNextProjectNumber(){
        Map<String, Object> out = jdbcCall.execute();
        return (String) out.get("number");
    }

    public void incrementClientUsageCounter(String clientName){
        final String request = "UPDATE client SET usage_count = usage_count + 1 WHERE name = ?";
        this.jdbcTemplate.update(request, clientName);
    }
}
