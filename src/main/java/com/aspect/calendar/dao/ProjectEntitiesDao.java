package com.aspect.calendar.dao;

import com.aspect.calendar.entity.calendar.Project;
import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class ProjectEntitiesDao extends JdbcDaoSupport {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcCall jdbcCall;
    private final Logger logger = LoggerFactory.getLogger(ProjectEntitiesDao.class);

    @Autowired
    public ProjectEntitiesDao(DataSource dataSource){
        this.setDataSource(dataSource);
        this.jdbcTemplate = this.getJdbcTemplate();
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
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

    public void addProject(Project project){
        final String request = "INSERT INTO items_group (name, type, client_email_subject, creation_date, created_by) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(request, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, project.getName());
            ps.setString(2, project.getType().name());
            ps.setString(3, project.getClientEmailSubject());
            ps.setString(4, LocalDateTime.now().format(CommonUtils.sqlDTFormatter));
            ps.setInt(5, project.getCreatedBy().getId());

            return ps;
        }, keyHolder);
        if(keyHolder.getKey() != null) project.setId(keyHolder.getKey().longValue());
    }

    public List<Project> getXtrfMissingProjects(){
        final String request = "SELECT items_group.*, admin.name as pm_name, admin.surname AS pm_surname FROM items_group " +
                "LEFT JOIN admin ON items_group.created_by = admin.id " +
                "WHERE type = 'PROJECT' AND xtrf_id IS NULL " +
                "ORDER BY pm_surname, name";
        try{
            return this.jdbcTemplate.query(request, new ProjectRowMapper());
        } catch (EmptyResultDataAccessException ignored){
            return new ArrayList<>();
        }
    }

    public void updateProjects(List<Project> projectList){
        final String request = "UPDATE items_group SET xtrf_id = ?, few_translators_allowed = ?, few_qc_allowed = ? WHERE id = ?";
        this.jdbcTemplate.batchUpdate(request, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Project project = projectList.get(i);
                ps.setLong(1, project.getXtrfId());
                ps.setBoolean(2, project.isFewTranslatorsAllowed());
                ps.setBoolean(3, project.isFewQCAllowed());
                ps.setLong(4, project.getId());
            }

            @Override
            public int getBatchSize() {
                return projectList.size();
            }
        });
    }

    public List<Project> getRecentProjects() {
        final String request = "SELECT items_group.*, admin.name as pm_name, admin.surname AS pm_surname FROM items_group " +
                "LEFT JOIN admin ON items_group.created_by = admin.id " +
                "WHERE creation_date > '2021-04-01' AND items_group.type = 'PROJECT' " +
                "ORDER BY creation_date";
        try {
            return this.jdbcTemplate.query(request, new ProjectRowMapper());
        } catch (EmptyResultDataAccessException ignored) {
            return new ArrayList<>();
        }
    }

    public List<Project> getProjectsByName(String name){
        final String request = "SELECT id, name FROM items_group " +
                "WHERE type = 'PROJECT' AND name LIKE :name " +
                "ORDER BY creation_date DESC LIMIT 100";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", "%" + name + "%");
        try {
            return this.namedParameterJdbcTemplate.query(request, params, (rs, i) -> {
                Project project = new Project();
                project.setId(rs.getLong("id"));
                project.setName(rs.getString("name"));

                return project;
            });
        } catch (EmptyResultDataAccessException ignored) {
            return new ArrayList<>();
        }
    }

    private static class ProjectRowMapper implements RowMapper<Project> {
        @Override
        public Project mapRow(ResultSet rs, int i) throws SQLException {
            Project project = new Project();
            project.setId(rs.getLong("id"));
            project.setXtrfId((Long) rs.getObject("xtrf_id"));
            project.setName(rs.getString("name"));
            project.setClientEmailSubject(rs.getString("client_email_subject"));
            project.setCreationDate(LocalDateTime.parse(rs.getString("creation_date"), CommonUtils.sqlDTFormatter));
            project.setFewTranslatorsAllowed(rs.getBoolean("few_translators_allowed"));
            project.setFewQCAllowed(rs.getBoolean("few_qc_allowed"));

            Person person = new Person(rs.getInt("created_by"));
            person.setName(rs.getString("pm_name"));
            person.setSurname(rs.getString("pm_surname"));

            project.setCreatedBy(person);

            return project;
        }
    }
}
