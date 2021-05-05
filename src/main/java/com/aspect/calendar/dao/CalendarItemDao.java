package com.aspect.calendar.dao;

import com.aspect.calendar.entity.calendar.CalendarItem;
import com.aspect.calendar.entity.calendar.Group;
import com.aspect.calendar.entity.calendar.Project;
import com.aspect.calendar.entity.enums.Division;
import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.entity.calendar.UserDayCalendar;
import com.aspect.calendar.entity.enums.CalendarItemType;
import com.aspect.calendar.entity.report.LoadReport;
import com.aspect.calendar.entity.report.ReportRow;
import com.aspect.calendar.entity.report.ReportRowUnit;
import com.aspect.calendar.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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

    final String selectRequest = "SELECT ci.*, " +
            "g.xtrf_id AS xtrf_id, g.name, g.type, g.client_email_subject, g.few_qc_allowed, g.few_translators_allowed, " +
            "g.creation_date AS group_creation_date, g.created_by AS group_created_by, " +
            "r.tr_amount, r.qc_amount, " +
            "s.group_size, s.group_start_date, s.group_deadline, s.group_duration, " +
            "manager.name AS manager_name, manager.surname AS manager_surname, " +
            "provider.name AS provider_name, provider.surname AS provider_surname, provider.division AS provider_division, " +
            "created_by.name AS created_by_name, created_by.surname AS created_by_surname, " +
            "modified_by.name AS modified_by_name, modified_by.surname AS modified_by_surname " +
            "FROM calendar_item ci " +
            "LEFT JOIN items_group g on ci.group_id = g.id " +
            "LEFT JOIN admin manager ON ci.manager = manager.id " +
            "LEFT JOIN admin provider ON ci.provider = provider.id " +
            "LEFT JOIN admin created_by ON ci.created_by = created_by.id " +
            "LEFT JOIN admin modified_by ON ci.modified_by = modified_by.id " +
            "LEFT JOIN ( " +
                "SELECT COUNT(DISTINCT (SELECT id FROM admin where admin.id = calendar_item.provider AND admin.division = 'QC')) AS qc_amount, " +
                "COUNT(DISTINCT (SELECT id FROM admin where admin.id = calendar_item.provider AND admin.division = 'TRANSLATOR')) AS tr_amount, " +
                "calendar_item.group_id AS group_id " +
                "FROM calendar_item " +
                "GROUP BY calendar_item.group_id " +
            ") r on ci.group_id = r.group_id " +
            "LEFT JOIN ( " +
                "SELECT COUNT(calendar_item.id) AS group_size, " +
                "MIN(STR_TO_DATE(CONCAT(calendar_item.item_date, ' ', SEC_TO_TIME(calendar_item.start_tp)), '%Y-%m-%d %H:%i:%s')) AS group_start_date, " +
                "MAX(STR_TO_DATE(CONCAT(calendar_item.item_date, ' ', SEC_TO_TIME(calendar_item.deadline_tp)), '%Y-%m-%d %H:%i:%s')) AS group_deadline, " +
                "SUM(calendar_item.deadline_tp - calendar_item.start_tp) AS group_duration, " +
                "calendar_item.group_id AS group_id, " +
                "calendar_item.provider AS provider " +
                "FROM calendar_item " +
                "GROUP BY calendar_item.group_id, calendar_item.provider " +
            ") s on ci.group_id = s.group_id AND ci.provider = s.provider ";

    public UserDayCalendar getEmptyDayCalendar(LocalDate day){
        final String query = "SELECT MIN(start_tp) as min_start_tp, MAX(deadline_tp) AS max_deadline_tp FROM calendar_item WHERE item_date = ?";

        return this.jdbcTemplate.queryForObject(query, new CalendarDayMapper(), day.format(sqlDateFormatter));
    }

    public void addItems(List<UserDayCalendar> dayCalendar, LocalDate day, Integer responsibleManager){
        Map<Integer, UserDayCalendar> calendarMap = new HashMap<>();
        List<Integer> userIds = new ArrayList<>();
        for(UserDayCalendar userCalendar : dayCalendar){
            Person user = userCalendar.getUser();
            if(user != null){
                userIds.add(user.getId());
                calendarMap.put(user.getId(), userCalendar);
            }
        }
        if(userIds.isEmpty()) return;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("day", day);
        params.addValue("userIds", userIds);
        params.addValue("responsibleManager", responsibleManager);

        final String whereClause = "WHERE ci.item_date = :day AND ci.provider IN (:userIds) " +
                (responsibleManager == null ? "" : "AND ci.manager = :responsibleManager ") +
                "ORDER BY ci.provider, ci.start_tp";

        this.namedJdbcTemplate.query(selectRequest + whereClause, params, new CalendarItemCallbackHandler(calendarMap));
    }

    public CalendarItem get(long id){
        return this.jdbcTemplate.queryForObject(selectRequest + "WHERE ci.id = ?", new ItemRowMapper(), id);
    }

    public String getItemsAsCSV(LocalDate dateFrom, LocalDate dateTo, int providerId){
        StringBuilder stringBuilder = new StringBuilder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("dateFrom", dateFrom.format(sqlDateFormatter));
        params.addValue("dateTo", dateTo.format(sqlDateFormatter));
        params.addValue("providerId", providerId);

        final String request = "SELECT ig.name, ig.type, ci.item_date, " +
                "CONCAT(manager.surname, ' ', manager.name) AS manager_name, " +
                "CONCAT(provider.surname, ' ', provider.name) AS provider_name, " +
                "CONCAT( ((deadline_tp - start_tp) DIV 3600), ':', ((deadline_tp - start_tp) % 3600 DIV 60) ) AS duration_hours, " +
                "((deadline_tp - start_tp) / 3600) AS duration " +
                "FROM calendar_item ci " +
                "LEFT JOIN items_group ig ON ci.group_id = ig.id " +
                "LEFT JOIN admin manager ON ci.manager = manager.id " +
                "LEFT JOIN admin provider ON ci.provider = provider.id " +
                "WHERE ci.item_date >= :dateFrom AND ci.item_date <= :dateTo " +
                (providerId > 0 ? "AND ci.provider = :providerId" : "");

        this.namedJdbcTemplate.query(request, params, new ItemsToCSVCallbackHandler(stringBuilder));

        return stringBuilder.toString();
    }

    public void update(CalendarItem item){
        final String updateRequest = "UPDATE calendar_item SET manager = ?, modified_by = ?, modification_date = ?, item_date = ?, start_tp = ?, deadline_tp = ?, description = ?, group_id = ? WHERE id = ?";

        this.jdbcTemplate.update( connection -> {
            PreparedStatement ps = connection.prepareStatement(updateRequest);
            ps.setInt(1, item.getManager().getId());
            ps.setInt(2, item.getModifiedBy().getId());
            ps.setString(3, LocalDateTime.now().format(sqlDTFormatter));
            ps.setString(4, item.getItemDate().format(sqlDateFormatter));
            ps.setInt(5, item.getStartDate().toSecondOfDay());
            ps.setInt(6, item.getDeadline().toSecondOfDay());
            ps.setString(7, item.getDescription());
            ps.setLong(8, item.getGroup().getId());
            ps.setLong(9, item.getId());

            return ps;
        });
    }

    public void save(CalendarItem item){
        final String request = "INSERT INTO calendar_item (manager, provider, created_by, creation_date, item_date, start_tp, deadline_tp, description, group_id) VALUES (?,?,?,?,?,?,?,?,?)";
        this.jdbcTemplate.update( connection -> {
            PreparedStatement ps = connection.prepareStatement(request);
            ps.setInt(1, item.getManager().getId());
            ps.setInt(2, item.getProvider().getId());
            ps.setInt(3, item.getCreatedBy().getId());
            ps.setString(4, item.getCreationDate().format(sqlDTFormatter));
            ps.setString(5, item.getItemDate().format(sqlDateFormatter));
            ps.setInt(6, item.getStartDate().toSecondOfDay());
            ps.setInt(7, item.getDeadline().toSecondOfDay());
            ps.setString(8, item.getDescription());
            ps.setLong(9, item.getGroup().getId());
            return ps;
        });
    }

    public void addGroup(Group group){
        final String request = "INSERT INTO items_group (name, type, creation_date, created_by) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(request, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, group.getName());
            ps.setString(2, group.getType().name());
            ps.setString(3, LocalDateTime.now().format(CommonUtils.sqlDTFormatter));
            ps.setInt(4, group.getCreatedBy().getId());

            return ps;
        }, keyHolder);
        if(keyHolder.getKey() != null) group.setId(keyHolder.getKey().longValue());
    }

    public void updateGroup(Group group){
        final String updateRequest = "UPDATE items_group SET name = ?, type = ?  WHERE id = ? AND type != 'PROJECT'";

        this.jdbcTemplate.update( connection -> {
            PreparedStatement ps = connection.prepareStatement(updateRequest);
            ps.setString(1, group.getName());
            ps.setString(2, group.getType().name());
            ps.setLong(3, group.getId());

            return ps;
        });
    }

    public void deleteGroupOfItems(int groupId, int itemId){
        final String request = "DELETE FROM calendar_item " +
                "WHERE calendar_item.group_id = ? AND calendar_item.provider = (" +
                "SELECT ci.provider " +
                "FROM (SELECT * FROM calendar_item ici WHERE ici.id = ?) AS ci )";
        this.jdbcTemplate.update(request, groupId, itemId);
    }

    public void delete(int itemId){
        final String request = "DELETE FROM calendar_item WHERE id = ?";
        this.jdbcTemplate.update(request, itemId);
    }

    public List<String> checkForIntersection(@NotNull CalendarItem item){
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("itemId",  item.getId());
        params.addValue("userId", item.getProvider().getId());
        params.addValue("itemDate", item.getItemDate().format(sqlDateFormatter));
        params.addValue("startTP", item.getStartDate().toSecondOfDay());
        params.addValue("endTP",  item.getDeadline().toSecondOfDay());

        String request = "SELECT g.name FROM calendar_item " +
                "LEFT JOIN items_group g ON calendar_item.group_id = g.id " +
                "WHERE calendar_item.id != :itemId AND provider = :userId AND item_date = :itemDate AND (start_tp < :startTP AND deadline_tp > :startTP OR start_tp < :endTP AND deadline_tp > :endTP OR start_tp >= :startTP AND deadline_tp <= :endTP) ";
        try{
            return this.namedJdbcTemplate.query(request, params, (rs, i) -> rs.getString("name"));
        } catch (EmptyResultDataAccessException ignored){
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
            item.setId(rs.getLong("id"));
            item.setCreationDate(LocalDateTime.parse(rs.getString("creation_date"), sqlDTFormatter));
            item.setModificationDate(rs.getString("modification_date") == null ? null : LocalDateTime.parse(rs.getString("modification_date"), sqlDTFormatter));
            item.setItemDate(LocalDate.parse(rs.getString("item_date"), sqlDateFormatter));
            item.setStartDate(LocalTime.ofSecondOfDay(rs.getInt("start_tp")));
            item.setDeadline(LocalTime.ofSecondOfDay(rs.getInt("deadline_tp")));
            item.setDescription(rs.getString("description"));

            Person person = item.getProvider();
            person.setId(rs.getInt("provider"));
            person.setName(rs.getString("provider_name"));
            person.setSurname(rs.getString("provider_surname"));
            person.setDivision(Division.valueOf(rs.getString("provider_division")));

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

            Group group = item.getGroup();
            group.setId(rs.getInt("group_id"));
            group.setName(rs.getString("name"));
            group.setType(CalendarItemType.valueOf(rs.getString("type")));
            group.setStartDate(LocalDateTime.parse(rs.getString("group_start_date"), sqlDTFormatter));
            group.setDeadline(LocalDateTime.parse(rs.getString("group_deadline"), sqlDTFormatter));
            group.setDuration(rs.getInt("group_duration"));
            group.setSize(rs.getInt("group_size"));
            group.setCreationDate(LocalDateTime.parse(rs.getString("group_creation_date"), sqlDTFormatter));
            group.setCreatedBy(new Person(rs.getInt("group_created_by")));

            if(group.getType() == CalendarItemType.PROJECT){
                Division userDivision = item.getProvider().getDivision();
                boolean hasCollision = userDivision == Division.QC && !rs.getBoolean("few_qc_allowed") && rs.getInt("qc_amount") > 1
                        || userDivision == Division.TRANSLATOR && !rs.getBoolean("few_translators_allowed") && rs.getInt("tr_amount") > 1;
                ((Project) group).setHasCollision(hasCollision);
            }

            return item;
        }
    }

    private static class CalendarItemCallbackHandler implements RowCallbackHandler{
        private final Map<Integer, UserDayCalendar> calendarMap;
        private final ItemRowMapper rowMapper = new ItemRowMapper();
        public CalendarItemCallbackHandler(Map<Integer, UserDayCalendar> calendarMap){
            this.calendarMap = calendarMap;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            CalendarItem item = rowMapper.mapRow(rs, 0);
            if(item != null) {
                int userId = item.getProvider().getId();
                if(calendarMap.containsKey(userId)){
                    calendarMap.get(userId).addItem(item);
                }
            }
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

        final String request = "SELECT a.name, a.surname, a.division, ci.provider, ci.item_date, g.type, SUM(ci.deadline_tp) - SUM(ci.start_tp) AS duration " +
                "FROM calendar_item ci " +
                "LEFT JOIN admin a on ci.provider = a.id " +
                "LEFT JOIN items_group g ON ci.group_id = g.id " +
                "WHERE ci.item_date >= (:from) AND ci.item_date <= (:to) AND ci.provider IN (:providerIds) " +
                "GROUP BY ci.item_date, ci.provider, g.type, a.surname, a.name, a.division " +
                "ORDER BY a.division, a.surname, a.name, ci.item_date";

        this.namedJdbcTemplate.query(request, parameters, new ReportRowCallbackHandler(report));

        return report;
    }


    private static class ReportRowCallbackHandler implements RowCallbackHandler{
        private final LoadReport report;
        private ReportRow currentRow;

        ReportRowCallbackHandler (LoadReport report){
            this.report = report;
        }

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
                case PROJECT:
                    rowItem.setProject(duration);
                    break;
                case JOB:
                    rowItem.setJob(duration);
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
            sb.append('"').append(rs.getString("name")).append("\";")
                    .append('"').append(rs.getString("type")).append("\";")
                    .append('"').append(LocalDate.parse(rs.getString("item_date"), sqlDateFormatter).format(dtFormatter)).append("\";")
                    .append('"').append(rs.getString("manager_name")).append("\";")
                    .append('"').append(rs.getString("provider_name")).append("\";")
                    .append('"').append(rs.getString("duration_hours")).append("\";")
                    .append('"').append(rs.getString("duration")).append("\"\n");
        }
    }
}