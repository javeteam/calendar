package com.aspect.calendar.dao;

import com.aspect.calendar.entity.calendar.Project;
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

        this.namedParameterJdbcTemplate.query(request, params, new ProjectRowCallbackHandler(projectsMap));
    }

    private static class ProjectRowCallbackHandler implements RowCallbackHandler{
        private final Map<String, Project> calendarProjects;

        public ProjectRowCallbackHandler(Map<String, Project> calendarProjects){
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



}
