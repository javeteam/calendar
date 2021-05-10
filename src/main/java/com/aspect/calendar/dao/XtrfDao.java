package com.aspect.calendar.dao;

import com.aspect.calendar.entity.calendar.Job;
import com.aspect.calendar.entity.calendar.Project;
import com.aspect.calendar.entity.user.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Transactional
public class XtrfDao extends JdbcDaoSupport {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public XtrfDao(@Qualifier("xtrf_datasource") DataSource dataSource) {
        this.setDataSource(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

    }

    public void setXtrfIds(List<Project> allProjects){
        final String request = "SELECT project_id, name FROM project WHERE name IN (:projectNames)";

        Map<String, Project> projectsMap = new HashMap<>();
        for(Project project : allProjects){
            projectsMap.put(project.getName(), project);
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("projectNames", projectsMap.keySet());

        this.namedParameterJdbcTemplate.query(request, params, new ProjectByNameRowCallbackHandler(projectsMap));
    }

    public void setProjectJobs(Map<Long, Project> projectMap){
        if(projectMap.keySet().isEmpty()) return;
        MapSqlParameterSource params = new MapSqlParameterSource("projectIds", projectMap.keySet());

        final String request = "SELECT proj.project_id, a.provider_id, p.name as provider_name, " +
                "(CASE WHEN ac.calculation_unit_id = 7 THEN ac.quantity ELSE 0 END) AS quantity " +
                "FROM project proj " +
                "LEFT JOIN task t on proj.project_id = t.project_id " +
                "LEFT JOIN activity a on t.task_id = a.task_id " +
                "LEFT JOIN activity_charge ac on a.activity_id = ac.activity_id " +
                "LEFT JOIN provider p ON a.provider_id = p.provider_id " +
                "WHERE proj.project_id IN (:projectIds) AND p.in_house";

        this.namedParameterJdbcTemplate.query(request, params, new ProjectByIdRowCallbackHandler(projectMap));
    }

    private static class ProjectByNameRowCallbackHandler implements RowCallbackHandler{
        private final Map<String, Project> calendarProjects;

        public ProjectByNameRowCallbackHandler(Map<String, Project> calendarProjects){
            this.calendarProjects = calendarProjects;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            String projectName = rs.getString("name");
            if(calendarProjects.containsKey(projectName)){
                Project project = calendarProjects.get(projectName);
                project.setXtrfId((Long) rs.getObject("project_id"));
            }
        }
    }

    private static class ProjectByIdRowCallbackHandler implements RowCallbackHandler{
        private final Map<Long, Project> projects;

        public ProjectByIdRowCallbackHandler(Map<Long, Project> projects){
            this.projects = projects;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            long projectId = rs.getLong("project_id");
            int xtrfProviderId = rs.getInt("provider_id");
            float quantity = rs.getFloat("quantity");

            Project project = this.projects.get(projectId);
            Job job = null;
            // looking for job where the same provider was involved
            for(Job loopJob : project.getJobs()){
                if(loopJob.getProvider().getXtrfId() == xtrfProviderId){
                    job = loopJob;
                    break;
                }
            }
            if(job == null){
                job = new Job();

                Person provider = new Person();
                provider.setXtrfId(xtrfProviderId);
                setProviderFullName(provider, rs.getString("provider_name"));

                job.setProvider(provider);
                project.addJob(job);
            }

            job.addXtrfDuration(quantity);
        }

        private void setProviderFullName(Person person, String fullName){
            if(fullName == null || fullName.isBlank()) return;

            int indexOfSpace = fullName.indexOf(' ');
            if(indexOfSpace > 0){
                person.setSurname(fullName.substring(0, indexOfSpace));
                fullName = fullName.substring(indexOfSpace).trim();
                indexOfSpace = fullName.indexOf(' ');
                if(indexOfSpace > 0) {
                    person.setName(fullName.substring(0, indexOfSpace));
                }
                if(indexOfSpace > 0) person.setName(fullName.substring(0, indexOfSpace));
                else if(fullName.length() > 2) person.setName(fullName);
            } else {
                person.setName("-");
                person.setSurname(fullName);
            }
        }
    }




}
