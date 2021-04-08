package com.aspect.calendar.service;

import com.aspect.calendar.dao.CalendarItemDao;
import com.aspect.calendar.entity.calendar.CalendarItem;
import com.aspect.calendar.entity.exceptions.CalendarItemProcessingException;
import com.aspect.calendar.entity.user.AppUser;
import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.entity.calendar.UserDayCalendar;
import com.aspect.calendar.entity.report.LoadReport;
import com.aspect.calendar.form.CalendarItemForm;
import com.aspect.calendar.form.SubmitItemForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.aspect.calendar.utils.CommonUtils.timeFormatter;

@Service
public class CalendarItemService {
    private final CalendarItemDao calendarItemDao;
    private final UserDetailsServiceImpl userDetailsService;
    private final ConversionService conversionService;


    @Autowired
    CalendarItemService(CalendarItemDao calendarItemDao, UserDetailsServiceImpl userDetailsService, ConversionService conversionService){
        this.calendarItemDao = calendarItemDao;
        this.userDetailsService = userDetailsService;
        this.conversionService = conversionService;
    }

    public UserDayCalendar getEmptyDayCalendar(LocalDate day){
        return this.calendarItemDao.getEmptyDayCalendar(day);
    }

    public List<UserDayCalendar> getAllUsersDayCalendar(LocalDate day, Integer responsibleManager){
        List<UserDayCalendar> dayCalendar = new ArrayList<>();
        UserDayCalendar calendarTemplate = this.getEmptyDayCalendar(day);
        dayCalendar.add(calendarTemplate);
        List<Person> activeProviders = this.userDetailsService.getAllActiveProviders();
        for(Person person : activeProviders){
            dayCalendar.add(addItemsToUserCalendar(calendarTemplate, person, day, responsibleManager));
        }

        return dayCalendar;
    }

    public List<UserDayCalendar> getUserDayCalendar(Person person, LocalDate day){
        List<UserDayCalendar> dayCalendar = new ArrayList<>();
        UserDayCalendar calendarTemplate = this.getEmptyDayCalendar(day);
        dayCalendar.add(calendarTemplate);
        dayCalendar.add(addItemsToUserCalendar(calendarTemplate, person, day, null));
        return dayCalendar;
    }

    private UserDayCalendar addItemsToUserCalendar(UserDayCalendar calendarTemplate, Person person, LocalDate day, Integer responsibleManager){
        List<CalendarItem> userItems = this.calendarItemDao.getEmployeeDayItems(day, person.getId(), responsibleManager);
        UserDayCalendar userCalendar = calendarTemplate.getCopy();
        userCalendar.setUser(person);
        userCalendar.setItems(userItems.stream().map(item -> this.conversionService.convert(item, CalendarItemForm.class)).collect(Collectors.toList()));
        return userCalendar;
    }


    public CalendarItem getItemById(int id){
        CalendarItem item = this.calendarItemDao.get(id);
        if(item != null && item.getGroupId() > 0) defineGroupInfo(item);
        return item;
    }

    public String getIntersectedItemsTitle(List<CalendarItem> itemsGroup){
        List<String> intersectedItems = new ArrayList<>();
        for(CalendarItem item : itemsGroup ){
            intersectedItems.addAll(this.calendarItemDao.checkForIntersection(item));
        }

        // Remove duplicates
        intersectedItems = new ArrayList<>(new HashSet<>(intersectedItems));
        return listToString(intersectedItems);
    }

    public String getIntersectedItemsTitle(CalendarItem item){
        List<String> intersectedItems = this.calendarItemDao.checkForIntersection(item);

        // Remove duplicates
        intersectedItems = new ArrayList<>(new HashSet<>(intersectedItems));

        return listToString(intersectedItems);
    }

    private String listToString(List<String> list){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < list.size(); i++){
            sb.append('\'').append(list.get(i)).append('\'');
            if(i != list.size() - 1) sb.append(", ");
        }
        return sb.toString().length() == 0 ? null : sb.toString();
    }


    public void saveGroup(List<CalendarItem> group){
        if(group.size() == 1){
            this.saveItem(group.get(0));
        } else if(group.size() > 1){
            int groupId = group.get(0).getGroupId();
            if(groupId == 0) groupId = this.calendarItemDao.getNextGroupId();
            for(CalendarItem item : group){
                item.setGroupId(groupId);
                saveItem(item);
            }
        }
    }

    public void saveItem(CalendarItem item){
        if(item.getId() > 0) this.calendarItemDao.update(item);
        else this.calendarItemDao.save(item);
    }

    public boolean deleteGroup(int id){
        return this.calendarItemDao.deleteGroup(id);
    }

    public boolean deleteItem(int id){
        return this.calendarItemDao.deleteItem(id);
    }

    public LoadReport getReport(LocalDate dateFrom, LocalDate dateTo, int providerId){
        List<Person> providers = new ArrayList<>();

        if(providerId > 0){ providers.add(this.userDetailsService.getPersonById(providerId));
        } else providers = this.userDetailsService.getAllActiveProviders();

        return this.calendarItemDao.getReport(dateFrom, dateTo, providers);
    }

    private void defineGroupInfo(@NotNull CalendarItem item){
        List<CalendarItem> group = this.calendarItemDao.getGroup(item.getGroupId());
        // Group must contains at least two items
        if(group.size() < 2){
            item.setGroupId(0);
        } else {
            item.setGroupSize(group.size());
            int groupDuration = 0;
            for(int i = 0; i < group.size(); i++){
                CalendarItem groupItem = group.get(i);
                groupDuration += (groupItem.getDeadline().toSecondOfDay() - groupItem.getStartDate().toSecondOfDay());
                if(groupItem.getId() == item.getId()) {
                    item.setPositionInGroup(i+1);
                }
                // First and last items of group
                if(i == 0) item.setGroupStartDate(groupItem.getItemDate().atTime(groupItem.getStartDate()));
                else if(i == group.size() -1 ) item.setGroupDeadline(groupItem.getItemDate().atTime(groupItem.getDeadline()));
            }
            item.setGroupDuration(groupDuration);
        }

    }

    public String getItemsAsCSV(LocalDate dateFrom, LocalDate dateTo, int providerId){
        return this.calendarItemDao.getItemsAsCSV(dateFrom, dateTo, providerId);
    }

    public String addNewItem(SubmitItemForm form, AppUser authenticatedUser) throws CalendarItemProcessingException {
        LocalDateTime startDateTime = form.getStartDate().atTime(form.getStartDateHour(), form.getStartDateMinute());
        if(startDateTime.isBefore(LocalDateTime.now()) && ! authenticatedUser.hasRole("ADMIN")){
            throw new CalendarItemProcessingException("{\"status\":\"Error\",\"message\":\"" + "You don't allowed to create items in the past" + "\"}", HttpStatus.FORBIDDEN);
        }

        if(!form.isValid()) throw new CalendarItemProcessingException("{\"status\":\"Error\",\"message\":\"" + form.getErrorMessage() + "\"}", HttpStatus.BAD_REQUEST);

        List<List<CalendarItem>> groups = form.split();

        StringBuilder sb = new StringBuilder();
        for(List<CalendarItem> group : groups){
            String intersectedItems = getIntersectedItemsTitle(group);
            if(intersectedItems != null) sb.append(' ').append(intersectedItems);
            for (CalendarItem item : group){
                item.setCreatedBy(authenticatedUser);
            }
        }

        if(sb.toString().length() > 0) throw new CalendarItemProcessingException("{\"status\":\"Error\",\"message\":\"" + "Creation failed! New item intersect: " + sb.toString() + "\"}", HttpStatus.UNPROCESSABLE_ENTITY);

        for (List<CalendarItem> group : groups){
            saveGroup(group);
        }

        return "{\"status\":\"Success\",\"calendarDate\":\"" + startDateTime.toLocalDate() + "\"}";
    }

    public String editItem(SubmitItemForm form, AppUser authenticatedUser) throws CalendarItemProcessingException{
        if(!form.isValid()) throw new CalendarItemProcessingException("{\"status\":\"Error\",\"message\":\"" + form.getErrorMessage() + "\"}", HttpStatus.BAD_REQUEST);

        CalendarItem item = form.convertToCalendarItem();
        if(item == null) throw new CalendarItemProcessingException("{\"status\":\"Error\",\"message\":\"" + "Event start and deadline date must be the same. To extend item use split function" + "\"}", HttpStatus.UNPROCESSABLE_ENTITY);


        item.setModifiedBy(authenticatedUser);
        if(!authenticatedUser.hasRole("ADMIN")){
            CalendarItem existingItem = getItemById(item.getId());
            boolean startDatePassed = existingItem.getStartDateTime().plusMinutes(15).isBefore(LocalDateTime.now());
            boolean startDateChanged = !item.getStartDateTime().isEqual(existingItem.getStartDateTime());
            boolean deadlinePassed = existingItem.getDeadlineDateTime().plusMinutes(15).isBefore(LocalDateTime.now());
            boolean deadlineChanged = !item.getDeadlineDateTime().isEqual(existingItem.getDeadlineDateTime());

            if( startDatePassed && startDateChanged || deadlinePassed && deadlineChanged ){
                throw new CalendarItemProcessingException("{\"status\":\"Error\",\"message\":\"" + "You don't allowed to edit items which have already started" + "\"}", HttpStatus.FORBIDDEN);
            }

            boolean newStartDateInThePast = item.getStartDateTime().plusMinutes(15).isBefore(LocalDateTime.now());
            boolean newDeadlineInThePast = item.getDeadlineDateTime().plusMinutes(15).isBefore(LocalDateTime.now());

            if( startDateChanged && newStartDateInThePast || deadlineChanged && newDeadlineInThePast ){
                throw new CalendarItemProcessingException("{\"status\":\"Error\",\"message\":\"" + "You don't allowed to set items in the past" + "\"}", HttpStatus.FORBIDDEN);
            }
        }

        String intersectedItems = getIntersectedItemsTitle(item);
        if(intersectedItems != null) throw new CalendarItemProcessingException("{\"status\":\"Error\",\"message\":\"" + "Save failed! New item intersect: " + intersectedItems + "\"}", HttpStatus.UNPROCESSABLE_ENTITY);

        saveItem(item);

        return "{\"status\":\"Success\",\"calendarDate\":\"" + item.getItemDate() + "\"}";
    }

    public String splitItem(int itemId, String splitTime) {
        CalendarItem item = getItemById(itemId);

        LocalTime sTime = LocalTime.parse(splitTime, timeFormatter);
        if(!sTime.minusMinutes(10).isBefore(item.getStartDate()) && !sTime.plusMinutes(10).isAfter(item.getDeadline())){
            CalendarItem newItem = new CalendarItem(item);
            item.setDeadline(sTime);
            newItem.setStartDate(sTime);

            List<CalendarItem> group = new ArrayList<>();
            group.add(item);
            group.add(newItem);

            saveGroup(group);
        }

        return "{\"status\":\"Success\"}";
    }

}
