package com.aspect.calendar.dao;

import com.aspect.calendar.entity.user.AppUser;
import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.entity.user.UserRole;
import com.aspect.calendar.entity.enums.Division;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@Transactional
public class UserDetailsDao extends JdbcDaoSupport {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDetailsDao(DataSource dataSource){
        this.setDataSource(dataSource);
        this.jdbcTemplate = this.getJdbcTemplate();
    }

    public AppUser findUserAccount(String login) throws UsernameNotFoundException {
        String sql =  "SELECT * FROM admin WHERE login = ?";
        try{
            return jdbcTemplate.queryForObject(sql, new AppUserMapper(), login);
        } catch ( EmptyResultDataAccessException e){
            throw new UsernameNotFoundException("Username " + login + " wasn't found");
        }
    }

    public Person get(int id){
        String selectQuery = "SELECT * FROM admin WHERE id = ?";
        return this.jdbcTemplate.queryForObject(selectQuery, new PersonRowMapper(), id);
    }

    public List<Person> getActiveProviders(){
        String selectQuery = "SELECT * FROM admin " +
                "WHERE active AND (division = 'TRANSLATOR' OR division = 'QC') " +
                "ORDER BY division, surname, name";

        try{
            return this.jdbcTemplate.query(selectQuery, new PersonRowMapper());
        } catch (EmptyResultDataAccessException ignored){
            return new ArrayList<>();
        }

    }

    public List<Person> getAllActiveManagers(){
        String selectQuery = "SELECT * FROM admin " +
                "WHERE active AND division = 'MANAGER' " +
                "ORDER BY division, surname, name";

        try{
            return this.jdbcTemplate.query(selectQuery, new PersonRowMapper());
        } catch (EmptyResultDataAccessException ignored){
            return new ArrayList<>();
        }
    }

    private static class AppUserMapper implements RowMapper<AppUser>{
        @Override
        public AppUser mapRow(ResultSet rs, int i) throws SQLException {
            AppUser user = new AppUser();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setSurname(rs.getString("surname"));
            user.setPassword(rs.getString("password"));
            user.setActive(rs.getBoolean("active"));
            user.setDivision(Division.valueOf(rs.getString("division")));
            user.setAuthorities(Collections.singletonList( new UserRole(rs.getString("role")) ));

            return user;
        }
    }

    private static class PersonRowMapper implements RowMapper<Person>{
        @Override
        public Person mapRow(ResultSet rs, int i) throws SQLException {
            Person person = new Person();
            person.setId(rs.getInt("id"));
            person.setName(rs.getString("name"));
            person.setSurname(rs.getString("surname"));
            person.setActive(rs.getBoolean("active"));
            person.setDivision(Division.valueOf(rs.getString("division")));

            return person;
        }
    }
}
