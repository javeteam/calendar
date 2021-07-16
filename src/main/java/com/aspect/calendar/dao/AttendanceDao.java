package com.aspect.calendar.dao;

import com.aspect.calendar.entity.exceptions.EntityNotExistException;
import com.aspect.calendar.entity.timeManagement.Attendance;
import com.aspect.calendar.entity.user.Person;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
import java.util.List;

import static com.aspect.calendar.utils.CommonUtils.*;

@Repository
@Transactional
public class AttendanceDao extends JdbcDaoSupport {
    private final JdbcTemplate jdbcTemplate;

    public AttendanceDao(DataSource dataSource){
        this.setDataSource(dataSource);
        this.jdbcTemplate = getJdbcTemplate();
    }

    private final String selectRequest = "SELECT tm.*, a.id AS user_id FROM time_management tm " +
            "LEFT JOIN admin a ON tm.user_id = a.id ";

    public Attendance get(int id) throws EntityNotExistException {
        try {
            return this.jdbcTemplate.queryForObject(selectRequest + "WHERE tm.id = ?", new AttendanceRowMapper(), id);
        } catch (EmptyResultDataAccessException ignored){
            throw new EntityNotExistException("Requested attendance doesn't exist");
        }
    }

    public void save(Attendance attendance){
        final String request = "INSERT INTO time_management (user_id, date, clock_in, actual_clock_in) VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(request, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, attendance.getUser().getId());
            ps.setString(2, attendance.getDate().format(sqlDateFormatter));
            ps.setString(3, attendance.getClockIn().format(sqlTimeFormatter));
            ps.setString(4, attendance.getActualClockIn().format(sqlDTFormatter));

            return ps;
        }, keyHolder);
        if(keyHolder.getKey() != null) attendance.setId(keyHolder.getKey().intValue());
    }

    public void update(Attendance attendance){
        final String request = "UPDATE time_management SET clock_out = ?, actual_clock_out = ? WHERE id = ?";

        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(request);
            ps.setString(1, attendance.getClockOut().format(sqlTimeFormatter));
            ps.setString(2, attendance.getActualClockOut().format(sqlDTFormatter));
            ps.setInt(3, attendance.getId());

            return ps;
        });
    }

    public List<Attendance> getOverdueAttendances(int userId){
        final String whereClause = "WHERE tm.user_id = ? AND tm.clock_out IS NULL AND tm.date < ? ORDER BY tm.date";

        try {
            return this.jdbcTemplate.query(selectRequest + whereClause, new AttendanceRowMapper(), userId, LocalDate.now().format(sqlDateFormatter));
        } catch (EmptyResultDataAccessException ignored){
            return new ArrayList<>();
        }
    }

    public Attendance getTodayAttendance(int userId){
        final String whereClause = "WHERE tm.user_id = ? AND date = ?";

        try {
            return this.jdbcTemplate.queryForObject(selectRequest + whereClause, new AttendanceRowMapper(), userId, LocalDate.now().format(sqlDateFormatter));
        } catch (EmptyResultDataAccessException ignored){
            return null;
        }
    }

    private static class AttendanceRowMapper implements RowMapper<Attendance>{
        @Override
        public Attendance mapRow(ResultSet rs, int i) throws SQLException {
            Attendance attendance = new Attendance();
            attendance.setId(rs.getInt("id"));
            attendance.setDate(LocalDate.parse(rs.getString("date"), sqlDateFormatter));
            attendance.setClockIn(LocalTime.parse(rs.getString("clock_in"), sqlTimeFormatter));
            attendance.setClockOut(rs.getObject("clock_out") == null ? null : LocalTime.parse(rs.getString("clock_out"), sqlTimeFormatter) );
            attendance.setActualClockIn(LocalDateTime.parse(rs.getString("actual_clock_in"), sqlDTFormatter));
            attendance.setActualClockOut(rs.getObject("actual_clock_out") == null ? null : LocalDateTime.parse(rs.getString("actual_clock_out"), sqlDTFormatter));

            Person person = new Person(rs.getInt("user_id"));
            attendance.setUser(person);

            return attendance;
        }
    }
}
