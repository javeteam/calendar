package com.aspect.calendar.service;

import com.aspect.calendar.dao.CalendarItemDao;
import com.aspect.calendar.entity.calendar.CalendarItem;
import com.aspect.calendar.entity.calendar.Group;
import com.aspect.calendar.entity.enums.CalendarItemType;
import com.aspect.calendar.entity.exceptions.CalendarItemProcessingException;
import com.aspect.calendar.entity.user.AppUser;
import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.entity.calendar.UserDayCalendar;
import com.aspect.calendar.entity.report.LoadReport;
import com.aspect.calendar.form.SubmitItemForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.aspect.calendar.utils.CommonUtils.timeFormatter;

@Service
public class CalendarItemService {
    private final CalendarItemDao calendarItemDao;
    private final UserDetailsServiceImpl userDetailsService;


    @Autowired
    CalendarItemService(CalendarItemDao calendarItemDao, UserDetailsServiceImpl userDetailsService){
        this.calendarItemDao = calendarItemDao;
        this.userDetailsService = userDetailsService;
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
            UserDayCalendar userCalendar = calendarTemplate.getCopy();
            userCalendar.setUser(person);
            dayCalendar.add(userCalendar);
        }
        this.calendarItemDao.addItems(dayCalendar, day, responsibleManager);

        return dayCalendar;
    }

    public List<UserDayCalendar> getUserDayCalendar(Person person, LocalDate day){
        List<UserDayCalendar> dayCalendar = new ArrayList<>();
        UserDayCalendar calendarTemplate = this.getEmptyDayCalendar(day);
        dayCalendar.add(calendarTemplate);
        UserDayCalendar userCalendar = calendarTemplate.getCopy();
        userCalendar.setUser(person);
        dayCalendar.add(userCalendar);
        this.calendarItemDao.addItems(dayCalendar, day, null);

        return dayCalendar;
    }

    public CalendarItem getItemById(long id){
        return this.calendarItemDao.get(id);
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


    public void saveItemsGroup(List<CalendarItem> itemsGroup){
        if(!itemsGroup.isEmpty()){
            Group group = itemsGroup.get(0).getGroup();
            long groupId = group.getId();
            if(group.getType() != CalendarItemType.PROJECT){
                if(groupId <= 0){
                    this.calendarItemDao.addGroup(group);
                    groupId = group.getId();
                } else this.calendarItemDao.updateGroup(group);

            }
            for(CalendarItem item : itemsGroup){
                item.getGroup().setId(groupId);
                saveItem(item);
            }
        }
    }

    public void saveItem(CalendarItem item){
        if(item.getId() > 0) this.calendarItemDao.update(item);
        else this.calendarItemDao.save(item);
    }

    public void deleteGroupOfItems(long groupId, long itemId){
        this.calendarItemDao.deleteGroupOfItems(groupId, itemId);
    }

    public void deleteItem(long id){
        this.calendarItemDao.delete(id);
    }

    public LoadReport getReport(LocalDate dateFrom, LocalDate dateTo, int providerId){
        List<Person> providers = new ArrayList<>();

        if(providerId > 0){ providers.add(this.userDetailsService.getPersonById(providerId));
        } else providers = this.userDetailsService.getAllActiveProviders();

        return this.calendarItemDao.getReport(dateFrom, dateTo, providers);
    }

    public String getItemsAsCSV(LocalDate dateFrom, LocalDate dateTo, int providerId){
        return this.calendarItemDao.getItemsAsCSV(dateFrom, dateTo, providerId);
    }

    public String addNewItem(SubmitItemForm form, AppUser authenticatedUser) throws CalendarItemProcessingException {
        LocalDateTime startDateTime = form.getStartDate().atTime(form.getStartDateHour(), form.getStartDateMinute());
        if(startDateTime.isBefore(LocalDateTime.now()) && ! authenticatedUser.hasRole("ADMIN")){
            throw new CalendarItemProcessingException("{\"status\":\"Error\",\"message\":\"" + "You don't allowed to create items in the past" + "\"}", HttpStatus.FORBIDDEN);
        }
        form.checkValidity();

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
            saveItemsGroup(group);
        }

        return "{\"status\":\"Success\",\"calendarDate\":\"" + startDateTime.toLocalDate() + "\"}";
    }

    public String editItem(SubmitItemForm form, AppUser authenticatedUser) throws CalendarItemProcessingException{
        form.checkValidity();

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

        Group itemGroup = item.getGroup();

        // If item type was changed from project to another one we have to create new group for it
        if(itemGroup.getType() != CalendarItemType.PROJECT){
            CalendarItem existingItem = this.calendarItemDao.get(item.getId());
            if(existingItem.getGroup().getType() == CalendarItemType.PROJECT) {
                itemGroup.setCreatedBy(authenticatedUser);
                this.calendarItemDao.addGroup(itemGroup);
            }
            else this.calendarItemDao.updateGroup(item.getGroup());
        }

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

            saveItemsGroup(group);
        }

        return "{\"status\":\"Success\"}";
    }

    public List<CalendarItem> getItemsByProjectIds(List<Long> projectIds){
        return this.calendarItemDao.getItemsByProjectIds(projectIds);
    }

}
