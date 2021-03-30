package com.aspect.calendar.dao;

import com.aspect.calendar.entity.AppConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@Transactional
public class AppConfigDao extends JdbcDaoSupport {
    private final JdbcTemplate jdbcTemplate;

    public AppConfigDao(DataSource dataSource) {
        this.setDataSource(dataSource);
        this.jdbcTemplate = getJdbcTemplate();
    }

    public AppConfig getConfig(){
        return jdbcTemplate.queryForObject("SELECT * FROM config LIMIT 1", new AppConfigRowMapper());
    }

    private static class AppConfigRowMapper implements RowMapper<AppConfig>{
        @Override
        public AppConfig mapRow(ResultSet rs, int i) throws SQLException {
            return new AppConfig(
                    Path.of(rs.getString("projects_root")),
                    Path.of(rs.getString("pm_root")),
                    rs.getString("bitrix_rest_url"),
                    rs.getInt("bitrix_notification_chat_id")
            );
        }
    }
}
