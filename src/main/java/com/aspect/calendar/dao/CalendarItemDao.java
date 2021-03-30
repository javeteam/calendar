package com.aspect.calendar.dao;

import com.aspect.calendar.entity.calendar.CalendarItem;
import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.entity.calendar.UserDayCalendar;
import com.aspect.calendar.entity.enums.CalendarItemType;
import com.aspect.calendar.entity.report.LoadReport;
import com.aspect.calendar.entity.report.ReportRow;
import com.aspect.calendar.entity.report.ReportRowUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.aspect.calendar.utils.CommonUtils.*;

@Repository
@Transactional
public class CalendarItemDao extends JdbcDaoSupport {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    CalendarItemDao(DataSource dataSource){
        this.setDataSource(dataSource);
        this.jdbcTemplate = this.getJdbcTemplate();
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    final String selectRequest = "SELECT calendar_item.*, " +
            "manager.name AS manager_name, manager.surname AS manager_surname, " +
            "provider.name AS provider_name, provider.surname AS provider_surname, " +
            "created_by.name AS created_by_name, created_by.surname AS created_by_surname, " +
            "modified_by.name AS modified_by_name, modified_by.surname AS modified_by_surname " +
            "FROM calendar_item " +
            "LEFT JOIN admin manager ON calendar_item.manager = manager.id " +
            "LEFT JOIN admin provider ON calendar_item.provider = provider.id " +
            "LEFT JOIN admin created_by ON calendar_item.created_by = created_by.id " +
            "LEFT JOIN admin modified_by ON calendar_item.modified_by = modified_by.id ";

    public UserDayCalendar getEmptyDayCalendar(LocalDate day){
        final String query = "SELECT MIN(start_tp) as min_start_tp, MAX(deadline_tp) AS max_deadline_tp FROM calendar_item WHERE item_date = ?";

        return this.jdbcTemplate.queryForObject(query, new CalendarDayMapper(), day.format(sqlDateFormatter));
    }

    public List<CalendarItem> getEmployeeDayItems(LocalDate day, int userId, Integer responsibleManager){
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("day", day);
        params.addValue("userId", userId);
        params.addValue("responsibleManager", responsibleManager);

        final String whereClause = "WHERE calendar_item.item_date = :day AND calendar_item.provider = :userId " +
                (responsibleManager == null ? "" : "AND calendar_item.manager = :responsibleManager ") +
                "ORDER BY start_tp";

        try{
            return this.namedJdbcTemplate.query(selectRequest + whereClause, params, new ItemRowMapper());
        } catch (EmptyResultDataAccessException ignored){
            return new ArrayList<>();
        }
    }

    public CalendarItem get(int id){
        final String whereClause = "WHERE calendar_item.id = ?";

        return this.jdbcTemplate.queryForObject(selectRequest + whereClause, new ItemRowMapper(), id);
    }


    public List<CalendarItem> getGroup(int groupId){
         final String selectRequest = "SELECT calendar_item.* " +
                 "FROM calendar_item " +
                 "WHERE group_id = ? " +
                 "ORDER BY STR_TO_DATE(CONCAT(calendar_item.item_date, ' ', SEC_TO_TIME(calendar_item.start_tp)), '%Y-%m-%d %H:%i')";

         try{
             return this.jdbcTemplate.query(selectRequest, new SimpleItemRowMapper(), groupId);
         }catch (EmptyResultDataAccessException ignored){
             return new ArrayList<>();
         }
    }

    public int getNextGroupId(){
        final String request = "SELECT MAX(group_id) + 1 AS next_group_id FROM calendar_item";
        try{
            Integer nextGroupId = this.jdbcTemplate.queryForObject(request, Integer.class);
            return nextGroupId == null ? 1 : nextGroupId;
        } catch (EmptyResultDataAccessException ignored){
            return 1;
        }
    }

    public String getItemsAsCSV(LocalDate dateFrom, LocalDate dateTo, int providerId){
        StringBuilder stringBuilder = new StringBuilder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("dateFrom", dateFrom.format(sqlDateFormatter));
        params.addValue("dateTo", dateTo.format(sqlDateFormatter));
        params.addValue("providerId", providerId);

        final String request = "SELECT ci.title, ci.type, ci.item_date, " +
                "CONCAT(manager.surname, ' ', manager.name) AS manager_name, " +
                "CONCAT(provider.surname, ' ', provider.name) AS provider_name, " +
                "CONCAT( ((deadline_tp - start_tp) DIV 3600), ':', ((deadline_tp - start_tp) % 3600 DIV 60) ) AS duration_hours, " +
                "((deadline_tp - start_tp) / 3600) AS duration " +
                "FROM calendar_item ci " +
                "LEFT JOIN admin manager ON ci.manager = manager.id " +
                "LEFT JOIN admin provider ON ci.provider = provider.id " +
                "WHERE ci.item_date >= :dateFrom AND ci.item_date <= :dateTo " +
                (providerId > 0 ? "AND ci.provider = :providerId" : "");

        this.namedJdbcTemplate.query(request, params, new ItemsToCSVCallbackHandler(stringBuilder));

        return stringBuilder.toString();
    }

    public void update(CalendarItem item){
        final String updateItemRequest = "UPDATE calendar_item  SET manager = ?, modified_by = ?, modification_date = ?, item_date = ?, start_tp = ?, deadline_tp = ?, type = ?, title = ?, description = ?, group_id = ? WHERE id = ?";
        final String updateGroupRequest = "UPDATE calendar_item SET manager = ?, type = ?, title = ? WHERE group_id = ? AND group_id != 0";

        this.jdbcTemplate.update( connection -> {
            PreparedStatement ps = connection.prepareStatement(updateItemRequest);
            ps.setInt(1, item.getManager().getId());
            ps.setInt(2, item.getModifiedBy().getId());
            ps.setString(3, LocalDateTime.now().format(sqlDTFormatter));
            ps.setString(4, item.getItemDate().format(sqlDateFormatter));
            ps.setInt(5, item.getStartDate().toSecondOfDay());
            ps.setInt(6, item.getDeadline().toSecondOfDay());
            ps.setString(7, item.getType().name());
            ps.setString(8, item.getTitle());
            ps.setString(9, item.getDescription());
            ps.setInt(10, item.getGroupId());
            ps.setInt(11, item.getId());

            return ps;
        });

        if(item.getGroupId() > 0){
            this.jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(updateGroupRequest);
                ps.setInt(1, item.getManager().getId());
                ps.setString(2, item.getType().name());
                ps.setString(3, item.getTitle());
                ps.setInt(4, item.getGroupId());

                return ps;
            });
        }
    }

    public void save(CalendarItem item){
        final String request = "INSERT INTO calendar_item (manager, provider, created_by, creation_date, item_date, start_tp, deadline_tp, type, title, description, group_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        this.jdbcTemplate.update( connection -> {
            PreparedStatement ps = connection.prepareStatement(request);
            ps.setInt(1, item.getManager().getId());
            ps.setInt(2, item.getProvider().getId());
            ps.setInt(3, item.getCreatedBy().getId());
            ps.setString(4, item.getCreationDate().format(sqlDTFormatter));
            ps.setString(5, item.getItemDate().format(sqlDateFormatter));
            ps.setInt(6, item.getStartDate().toSecondOfDay());
            ps.setInt(7, item.getDeadline().toSecondOfDay());
            ps.setString(8, item.getType().name());
            ps.setString(9, item.getTitle());
            ps.setString(10, item.getDescription());
            ps.setInt(11, item.getGroupId());
            return ps;
        });
    }

    public boolean deleteGroup(int groupId){
        final String request = "DELETE FROM calendar_item WHERE group_id = ?";
        int rowsCount = this.jdbcTemplate.update(request, groupId);
        return rowsCount > 0;
    }

    public boolean deleteItem(int itemId){
        final String request = "DELETE FROM calendar_item WHERE id = ?";
        int rowsCount = this.jdbcTemplate.update(request, itemId);
        return rowsCount > 0;
    }

    public List<String> checkForIntersection(@NotNull CalendarItem item){
        int userId = item.getProvider().getId();
        String itemDate = item.getItemDate().format(sqlDateFormatter);
        int startTP = item.getStartDate().toSecondOfDay();
        int endTP = item.getDeadline().toSecondOfDay();

        String request = "SELECT title FROM calendar_item " +
                "WHERE id != ? AND provider = ? AND item_date = ? AND (start_tp < ? AND deadline_tp > ? OR start_tp < ? AND deadline_tp > ? OR start_tp >= ? AND deadline_tp <= ?) ";
        try{
            return this.jdbcTemplate.query(request, new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps) throws SQLException {
                    ps.setInt(1, item.getId());
                    ps.setInt(2, userId);
                    ps.setString(3, itemDate);
                    ps.setInt(4,startTP);
                    ps.setInt(5,startTP);
                    ps.setInt(6,endTP);
                    ps.setInt(7,endTP);
                    ps.setInt(8,startTP);
                    ps.setInt(9,endTP);

                }
            }, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int i) throws SQLException {
                    return rs.getString("title");
                }
            });
        }catch (EmptyResultDataAccessException ignored){
            return new ArrayList<>();
        }
    }

    private static class CalendarDayMapper implements RowMapper<UserDayCalendar>{
        @Override
        public UserDayCalendar mapRow(ResultSet rs, int i) throws SQLException {
            Integer min_start_tp = (Integer) rs.getObject("min_start_tp");
            Integer max_deadline_tp = (Integer) rs.getObject("max_deadline_tp");

            return new UserDayCalendar(min_start_tp, max_deadline_tp);
        }
    }


    private static class ItemRowMapper implements RowMapper<CalendarItem>{
        @Override
        public CalendarItem mapRow(ResultSet rs, int i) throws SQLException {
            CalendarItem item = new CalendarItem();
            item.setId(rs.getInt("id"));
            item.setCreationDate(LocalDateTime.parse(rs.getString("creation_date"), sqlDTFormatter));
            item.setModificationDate(rs.getString("modification_date") == null ? null : LocalDateTime.parse(rs.getString("modification_date"), sqlDTFormatter));
            item.setItemDate(LocalDate.parse(rs.getString("item_date"), sqlDateFormatter));
            item.setStartDate(LocalTime.ofSecondOfDay(rs.getInt("start_tp")));
            item.setDeadline(LocalTime.ofSecondOfDay(rs.getInt("deadline_tp")));
            item.setType(CalendarItemType.valueOf(rs.getString("type")));
            item.setTitle(rs.getString("title"));
            item.setDescription(rs.getString("description"));
            item.setGroupId(rs.getInt("group_id"));


            Person person = item.getProvider();
            person.setId(rs.getInt("provider"));
            person.setName(rs.getString("provider_name"));
            person.setSurname(rs.getString("provider_surname"));

            person = item.getManager();
            person.setId(rs.getInt("manager"));
            person.setName(rs.getString("manager_name"));
            person.setSurname(rs.getString("manager_surname"));

            person = item.getCreatedBy();
            person.setId(rs.getInt("created_by"));
            person.setName(rs.getString("created_by_name"));
            person.setSurname(rs.getString("created_by_surname"));

            person = item.getModifiedBy();
            person.setId(rs.getInt("modified_by"));
            person.setName(rs.getString("modified_by_name"));
            person.setSurname(rs.getString("modified_by_surname"));

            return item;
        }
    }

    private static class SimpleItemRowMapper implements RowMapper<CalendarItem>{
        @Override
        public CalendarItem mapRow(ResultSet rs, int i) throws SQLException {
            CalendarItem item = new CalendarItem();
            item.setId(rs.getInt("id"));
            item.setItemDate(LocalDate.parse(rs.getString("item_date"), sqlDateFormatter));
            item.setStartDate(LocalTime.ofSecondOfDay(rs.getInt("start_tp")));
            item.setDeadline(LocalTime.ofSecondOfDay(rs.getInt("deadline_tp")));

            return item;
        }
    }

    public LoadReport getReport(@NotNull LocalDate dateFrom, @NotNull LocalDate dateTo, @NotNull List<Person> providers){
        final LoadReport report = new LoadReport(dateFrom, dateTo, providers);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        String from = dateFrom.format(sqlDateFormatter);
        String to = dateTo.format(sqlDateFormatter);

        Set<Integer> providerIds = new HashSet<>();
        for(Person provider : providers){
            providerIds.add(provider.getId());
        }

        parameters.addValue("from", from);
        parameters.addValue("to", to);
        parameters.addValue("providerIds", providerIds);

        final String request = "SELECT a.name, a.surname, a.division, ci.provider, ci.item_date, ci.type, SUM(ci.deadline_tp) - SUM(ci.start_tp) AS duration " +
                "FROM calendar_item ci " +
                "LEFT JOIN admin a on ci.provider = a.id " +
                "WHERE ci.item_date >= (:from) AND ci.item_date <= (:to) AND ci.provider IN (:providerIds) " +
                "GROUP BY ci.item_date, ci.provider, ci.type, a.surname, a.name, a.division " +
                "ORDER BY a.division, a.surname, a.name, ci.item_date";

        this.namedJdbcTemplate.query(request, parameters, new ReportRowCallbackHandler(report));

        return report;
    }


    private static class ReportRowCallbackHandler implements RowCallbackHandler{
        private final LoadReport report;
        private ReportRow currentRow;

        ReportRowCallbackHandler (LoadReport report){
            this.report = report;
        };

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            int providerId = rs.getInt("provider");
            if(currentRow == null || currentRow.getPerson().getId() != providerId){
                currentRow = report.getRow(providerId);
            }

            LocalDate itemDate = LocalDate.parse(rs.getString("item_date"), sqlDateFormatter);
            ReportRowUnit rowItem = currentRow.getRowItem(itemDate);
            CalendarItemType type = CalendarItemType.valueOf(rs.getString("type"));
            int duration = rs.getInt("duration");

            switch (type){
                case ABSENCE:
                    rowItem.setAbsence(duration);
                    break;
                case CONFIRMED:
                    rowItem.setConfirmed(duration);
                    break;
                case POTENTIAL:
                    rowItem.setPotential(duration);
                    break;
            }
        }
    }

    private static class ItemsToCSVCallbackHandler implements RowCallbackHandler{
        private final StringBuilder sb;

        ItemsToCSVCallbackHandler (StringBuilder sb){
            this.sb = sb;
            sb.append("\"title\";")
                    .append("\"item type\";")
                    .append("\"item date\";")
                    .append("\"manager name\";")
                    .append("\"provider name\";")
                    .append("\"duration hours\";")
                    .append("\"duration\"\n");
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            sb.append('"').append(rs.getString("title")).append("\";")
                    .append('"').append(rs.getString("type")).append("\";")
                    .append('"').append(LocalDate.parse(rs.getString("item_date"), sqlDateFormatter).format(dtFormatter)).append("\";")
                    .append('"').append(rs.getString("manager_name")).append("\";")
                    .append('"').append(rs.getString("provider_name")).append("\";")
                    .append('"').append(rs.getString("duration_hours")).append("\";")
                    .append('"').append(rs.getString("duration")).append("\"\n");
        }
    }
}