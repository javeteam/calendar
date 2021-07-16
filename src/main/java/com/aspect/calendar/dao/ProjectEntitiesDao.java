package com.aspect.calendar.dao;

import com.aspect.calendar.entity.calendar.CalendarItem;
import com.aspect.calendar.entity.calendar.Project;
import com.aspect.calendar.entity.calendar.Job;
import com.aspect.calendar.entity.enums.Division;
import com.aspect.calendar.entity.exceptions.EntityNotExistException;
import com.aspect.calendar.entity.user.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aspect.calendar.utils.CommonUtils.sqlDTFormatter;
import static com.aspect.calendar.utils.CommonUtils.sqlDateFormatter;

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
            ps.setString(4, LocalDateTime.now().format(sqlDTFormatter));
            ps.setInt(5, project.getCreatedBy().getId());

            return ps;
        }, keyHolder);
        if(keyHolder.getKey() != null) project.setId(keyHolder.getKey().longValue());
    }

    public Project get(long id) throws EntityNotExistException {
        final String request = "SELECT items_group.*, admin.name as pm_name, admin.surname AS pm_surname FROM items_group " +
                "LEFT JOIN admin ON items_group.created_by = admin.id " +
                "WHERE items_group.id = ?";
        try{
            return this.jdbcTemplate.queryForObject(request, new ProjectRowMapper(), id);
        } catch (EmptyResultDataAccessException ignored){
            throw new EntityNotExistException("Project doesn't exist");
        }
    }

    public List<Project> getXtrfMissingProjects(){
        final String request = "SELECT items_group.*, admin.name as pm_name, admin.surname AS pm_surname FROM items_group " +
                "LEFT JOIN admin ON items_group.created_by = admin.id " +
                "WHERE items_group.type = 'PROJECT' AND items_group.xtrf_id IS NULL " +
                "ORDER BY pm_surname, items_group.name";
        try{
            return this.jdbcTemplate.query(request, new ProjectRowMapper());
        } catch (EmptyResultDataAccessException ignored){
            return new ArrayList<>();
        }
    }

    public void updateProjects(List<Project> projectList){
        final String request = "UPDATE items_group SET name = ?, xtrf_id = ?, client_email_subject = ?, few_translators_allowed = ?, few_qc_allowed = ? WHERE id = ?";
        this.jdbcTemplate.batchUpdate(request, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Project project = projectList.get(i);
                ps.setString(1, project.getName());
                ps.setObject(2, project.getXtrfId());
                ps.setString(3, project.getClientEmailSubject());
                ps.setBoolean(4, project.isFewTranslatorsAllowed());
                ps.setBoolean(5, project.isFewQCAllowed());
                ps.setLong(6, project.getId());
            }

            @Override
            public int getBatchSize() {
                return projectList.size();
            }
        });
    }

    private final String projectsWithItemsRequest = "SELECT ig.id, ig.xtrf_id, ig.name, ig.client_email_subject, ig.few_translators_allowed, ig.few_qc_allowed, ig.creation_date AS project_creation_date, ig.created_by AS project_created_by, " +
            "u.name as pm_name, u.surname AS pm_surname, " +
            "ci.id AS item_id, ci.item_date, ci.start_tp, ci.deadline_tp, ci.deleted, " +
            "manager.id AS manager_id, manager.name AS manager_name, manager.surname AS manager_surname, " +
            "provider.id AS provider_id, provider.xtrf_id AS provider_xtrf_id, provider.name AS provider_name, provider.surname AS provider_surname, provider.division AS provider_division, " +
            "created_by.id AS created_by, created_by.name AS created_by_name, created_by.surname AS created_by_surname, " +
            "modified_by.id AS modified_by_id, modified_by.name AS modified_by_name, modified_by.surname AS modified_by_surname " +
            "FROM items_group ig " +
            "LEFT JOIN admin u ON ig.created_by = u.id " +
            "LEFT JOIN calendar_item ci on ig.id = ci.group_id AND NOT ci.deleted " +
            "LEFT JOIN admin manager ON ci.manager = manager.id " +
            "LEFT JOIN admin provider ON ci.provider = provider.id " +
            "LEFT JOIN admin created_by ON ci.created_by = created_by.id " +
            "LEFT JOIN admin modified_by ON ci.modified_by = modified_by.id ";

    public List<Project> getProjectsToManage(LocalDate dateFrom, LocalDate dateTo, int responsibleManager) {
        Map<Long, Project> projectMap = new HashMap<>();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("dateFrom", dateFrom);
        params.addValue("dateTo", dateTo);
        params.addValue("responsibleManager", responsibleManager);

         final String whereClause = "WHERE ig.creation_date >= :dateFrom AND ig.creation_date <= :dateTo AND ig.type = 'PROJECT' " +
                (responsibleManager > 0 ? "AND ig.created_by = :responsibleManager " : "") +
                "ORDER BY ig.id, provider_id";
        try {
            this.namedParameterJdbcTemplate.query(projectsWithItemsRequest + whereClause, params, new ProjectRowCallbackHandler(projectMap));
        } catch (EmptyResultDataAccessException ignored) {
            return new ArrayList<>();
        }
        return new ArrayList<>(projectMap.values());
    }

    public Project getProjectToManage(long id) throws EntityNotExistException{
        Map<Long, Project> projectMap = new HashMap<>();
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        final String whereClause = "WHERE ig.id = :id ORDER BY provider_id";
        try {
            this.namedParameterJdbcTemplate.query(projectsWithItemsRequest + whereClause, params, new ProjectRowCallbackHandler(projectMap));
        } catch (EmptyResultDataAccessException ignored) {
            throw new EntityNotExistException("Project doesn't exist");
        }

        if(!projectMap.containsKey(id)) throw new EntityNotExistException("Project doesn't exist");
        else return projectMap.get(id);
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
            project.setCreationDate(LocalDateTime.parse(rs.getString("creation_date"), sqlDTFormatter));
            project.setFewTranslatorsAllowed(rs.getBoolean("few_translators_allowed"));
            project.setFewQCAllowed(rs.getBoolean("few_qc_allowed"));

            Person person = new Person(rs.getInt("created_by"));
            person.setName(rs.getString("pm_name"));
            person.setSurname(rs.getString("pm_surname"));

            project.setCreatedBy(person);

            return project;
        }
    }

    private static class ProjectRowCallbackHandler implements RowCallbackHandler{
        private final Map<Long, Project> projectMap;

        public ProjectRowCallbackHandler(Map<Long, Project> projectMap) {
            this.projectMap = projectMap;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            long projectId = rs.getLong("id");
            Project project;
            // get project form map if it exist or create new one if no
            if(!projectMap.containsKey(projectId)){
                project = new Project();
                project.setId(projectId);
                fillInProjectFields(project, rs);
                projectMap.put(projectId, project);
            } else {
                project = projectMap.get(projectId);
            }

            // if item_id is null there are no calendar items on this project just skip it
            if(rs.getObject("item_id") != null){
                CalendarItem item = new CalendarItem();
                item.setGroup(project);
                fillInCalendarItemFields(item, rs);

                Job job = null;
                // looking for job where the same provider was involved
                for(Job loopJob : project.getJobs()){
                    if(loopJob.getProvider().getId() == item.getProvider().getId()){
                        job = loopJob;
                        break;
                    }
                }
                if(job == null){
                    job = new Job();
                    job.setProvider(item.getProvider());
                    project.addJob(job);
                }

                job.addCalendarItem(item);
            }

        }

        private void fillInProjectFields(Project project, ResultSet rs) throws SQLException{
            project.setXtrfId((Long)rs.getObject("xtrf_id"));
            project.setName(rs.getString("name"));
            project.setClientEmailSubject(rs.getString("client_email_subject"));
            project.setFewTranslatorsAllowed(rs.getBoolean("few_translators_allowed"));
            project.setFewQCAllowed(rs.getBoolean("few_qc_allowed"));
            project.setCreationDate(LocalDateTime.parse(rs.getString("project_creation_date"), sqlDTFormatter));

            Person pm = new Person(rs.getInt("project_created_by"));
            pm.setName(rs.getString("pm_name"));
            pm.setSurname(rs.getString("pm_surname"));
            project.setCreatedBy(pm);
        }

        private void fillInCalendarItemFields(CalendarItem item, ResultSet rs) throws SQLException{
            item.setId(rs.getLong("item_id"));
            item.setItemDate(LocalDate.parse(rs.getString("item_date"), sqlDateFormatter));
            item.setStartDate(LocalTime.ofSecondOfDay(rs.getInt("start_tp")));
            item.setDeadline(LocalTime.ofSecondOfDay(rs.getInt("deadline_tp")));
            item.setDeleted(rs.getBoolean("deleted"));

            Person person = item.getManager();
            person.setId(rs.getInt("manager_id"));
            person.setName(rs.getString("manager_name"));
            person.setSurname(rs.getString("manager_surname"));

            person = item.getProvider();
            person.setId(rs.getInt("provider_id"));
            person.setXtrfId(rs.getInt("provider_xtrf_id"));
            person.setName(rs.getString("provider_name"));
            person.setSurname(rs.getString("provider_surname"));
            person.setDivision(Division.valueOf(rs.getString("provider_division")));

            person = item.getCreatedBy();
            person.setId(rs.getInt("created_by"));
            person.setName(rs.getString("created_by_name"));
            person.setSurname(rs.getString("created_by_surname"));
        }
    }
}
