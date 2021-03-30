package com.aspect.calendar.form;

import com.aspect.calendar.entity.calendar.CalendarItem;
import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.entity.calendar.UserDayCalendar;
import com.aspect.calendar.entity.enums.CalendarItemType;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SubmitItemForm {
    private int id;
    private int groupId;
    private String title;
    private String description;
    private int providerId;
    private int managerId;
    private CalendarItemType type;
    private boolean excludeLunchtime;
    private int[] repetitionDays;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate;
    int startDateHour;
    int startDateMinute;
    int endDateHour;
    int endDateMinute;
    int durationHours;
    int durationMinutes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public CalendarItemType getType() {
        return type;
    }

    public void setType(CalendarItemType type) {
        this.type = type;
    }

    public boolean isExcludeLunchtime() {
        return excludeLunchtime;
    }

    public void setExcludeLunchtime(boolean excludeLunchtime) {
        this.excludeLunchtime = excludeLunchtime;
    }

    public int[] getRepetitionDays() {
        return repetitionDays;
    }

    public void setRepetitionDays(int[] repetitionDays) {
        this.repetitionDays = repetitionDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getStartDateHour() {
        return startDateHour;
    }

    public void setStartDateHour(int startDateHour) {
        this.startDateHour = startDateHour;
    }

    public int getStartDateMinute() {
        return startDateMinute;
    }

    public void setStartDateMinute(int startDateMinute) {
        this.startDateMinute = startDateMinute;
    }

    public int getEndDateHour() {
        return endDateHour;
    }

    public void setEndDateHour(int endDateHour) {
        this.endDateHour = endDateHour;
    }

    public int getEndDateMinute() {
        return endDateMinute;
    }

    public void setEndDateMinute(int endDateMinute) {
        this.endDateMinute = endDateMinute;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean createdByDeadline(){
        return (endDate != null);
    }

    public boolean createdByDuration(){
        return !createdByDeadline();
    }

    public LocalDateTime getStartDateTime(){
        return startDate.atTime(startDateHour, startDateMinute);
    }

    public LocalDateTime getEndDateTime(){
        if(createdByDuration()) return getStartDateTime().plusHours(durationHours).plusMinutes(durationMinutes);
        else return endDate.atTime(endDateHour, endDateMinute);
    }

    public boolean isValid(){
        return this.datesValid()
                && this.isLongEnough()
                && !this.isTooLong()
                && !this.startDateTooLate()
                && !this.deadlineTooEarly()
                && !repetitionPeriodIncorrect()
                && repeatedItemLongEnough();
    }

    public String getErrorMessage(){
        if(!this.datesValid()){
            return "Deadline can't be less or equal to start date";
        } else if(!this.isLongEnough()){
            return "Item can't be shorter then 5 minutes";
        } else if(this.isTooLong()){
            return "Item created by duration can't finish after 23:55";
        } else if(this.startDateTooLate()){
            return "Item can't start after 22:00";
        } else if(this.deadlineTooEarly()){
            return "Item can't end before 07:00";
        } else if(this.repetitionPeriodIncorrect()){
            return "Repetition period can't be longer then 31 day";
        } else if(!this.repeatedItemLongEnough()){
            return "Repeated items can't be shorter then 10 minutes";
        } else  return "";
    }

    private boolean datesValid(){
        return getStartDateTime().isBefore(getEndDateTime());
    }

    private boolean isLongEnough(){
        int durationInMinutes = 0;
        if(endDate == null) durationInMinutes = durationHours * 60 + durationMinutes;
        else durationInMinutes =  (int)ChronoUnit.MINUTES.between(getStartDateTime(), getEndDateTime());

        return durationInMinutes  >= 5;
    }

    private boolean isTooLong(){
        return createdByDuration() && longerThenDay();
    }

    private boolean startDateTooLate(){
        return LocalTime.of(startDateHour, startDateMinute).isAfter(LocalTime.of(22,0));
    }

    private boolean deadlineTooEarly(){
        return longerThenDay() && getEndDateTime().toLocalTime().isBefore(LocalTime.of(7,0));
    }

    private boolean longerThenDay(){
        return !startDate.isEqual(getEndDateTime().toLocalDate());
    }

    private boolean repetitionPeriodIncorrect(){
        return createdByDeadline() && repetitionDays != null && ChronoUnit.DAYS.between(startDate, endDate) > 32;
    }

    private boolean repeatedItemLongEnough(){
        if(repetitionDays == null) return true;
        else {
            int durationInMinutes = (int)ChronoUnit.MINUTES.between(LocalTime.of(startDateHour, startDateMinute), LocalTime.of(endDateHour, endDateMinute));
            return durationInMinutes  >= 10;
        }
    }


    public CalendarItem convertToCalendarItem(){
        CalendarItem item = new CalendarItem();
        item.setId(this.id);
        item.setGroupId(this.groupId);
        item.setProvider(new Person(this.providerId));
        item.setManager(new Person(this.managerId));
        item.setTitle(this.title);
        item.setDescription(this.description);
        item.setType(this.type);
        item.setItemDate(this.startDate);
        item.setStartDate(LocalTime.of(startDateHour, startDateMinute));
        item.setDeadline(getEndDateTime().toLocalTime());

        return item;
    }

    private SubmitItemForm getCopy(){
        SubmitItemForm form = new SubmitItemForm();
        form.setProviderId(this.providerId);
        form.setManagerId(this.managerId);
        form.setTitle(this.title);
        form.setDescription(this.description);
        form.setType(this.type);
        form.setStartDate(this.startDate);
        form.setStartDateHour(this.startDateHour);
        form.setStartDateMinute(this.startDateMinute);
        form.setEndDate(this.startDate);

        return form;
    }

    public List<List<CalendarItem>> split(){
        List<List<CalendarItem>> groups = new ArrayList<>();
        List<CalendarItem> groupOfItems = new ArrayList<>();
        groups.add(groupOfItems);
        if(createdByDuration()){
            CalendarItem item = this.convertToCalendarItem();
            if(excludeLunchtime) groupOfItems.addAll(this.removeLunch(item, true, true, true));
            else groupOfItems.add(item);

        } else if(repetitionDays == null){ // Not repetition, just an item
            List<CalendarItem> itemsToHandle = this.splitIntoDays();
            if(excludeLunchtime){
                for(int i = 0; i < itemsToHandle.size(); i++){
                    boolean isFirst = ( i == 0 );
                    boolean isLast = ( i == itemsToHandle.size() -1 );
                    // if there is weekends in the middle of group - skip this days
                    if(!isFirst && !isLast && itemsToHandle.get(i).getItemDate().getDayOfWeek().getValue() > 5) continue;
                    groupOfItems.addAll(this.removeLunch(itemsToHandle.get(i), isFirst, isLast, false));
                }
            } else groupOfItems.addAll(itemsToHandle);
        } else { // item should be repeated
            List<CalendarItem> itemsToRepeat = new ArrayList<>();
            CalendarItem item = this.convertToCalendarItem();
            if(excludeLunchtime) itemsToRepeat.addAll(this.removeLunch(item, true, true, false));
            else itemsToRepeat.add(item);
            groups = handleRepetition(itemsToRepeat, repetitionDays);
        }

        return groups;
    }

    private List<CalendarItem> splitIntoDays(){
        List<CalendarItem> group = new ArrayList<>();
        long splitsRequired = ChronoUnit.DAYS.between(startDate, getEndDateTime().toLocalDate());
        final LocalTime wDayStart = LocalTime.ofSecondOfDay(UserDayCalendar.WORKING_DAY_START_TP);
        final LocalTime wDayEnd = LocalTime.ofSecondOfDay(UserDayCalendar.WORKING_DAY_END_TP);

        if(splitsRequired > 0){
            for(int currentSplit = 0; currentSplit < splitsRequired; currentSplit++){
                SubmitItemForm form = this.getCopy();
                // item starts before working day end
                if(form.getStartDateTime().toLocalTime().isBefore(wDayEnd)){
                    form.setEndDateHour(wDayEnd.getHour());
                    form.setEndDateMinute(wDayEnd.getMinute());
                } else{
                    LocalTime customDayEnd = form.getStartDateTime().toLocalTime().plusHours(1);
                    form.setEndDateHour(customDayEnd.getHour());
                    form.setEndDateMinute(customDayEnd.getMinute());
                }
                group.add(form.convertToCalendarItem());
                this.setStartDate(this.startDate.plusDays(1));

                //last split
                if(currentSplit == splitsRequired - 1){
                    // item ends after working day start
                    if(this.getEndDateTime().toLocalTime().isAfter(wDayStart)){
                        this.setStartDateHour(wDayStart.getHour());
                        this.setStartDateMinute(wDayStart.getMinute());
                    } else {
                        LocalTime customDayEnd = this.getEndDateTime().toLocalTime().minusHours(1);
                        this.setStartDateHour(customDayEnd.getHour());
                        this.setStartDateMinute(customDayEnd.getMinute());
                    }
                    group.add(this.convertToCalendarItem());
                } else {
                    this.setStartDateHour(wDayStart.getHour());
                    this.setStartDateMinute(wDayStart.getMinute());
                }

            }
        } else {
            group.add(this.convertToCalendarItem());
        }

    return group;
    }

    private List<CalendarItem> removeLunch(@NotNull CalendarItem item, boolean isFirst, boolean isLast, boolean withCompensation){
        LocalTime lStart = LocalTime.ofSecondOfDay(UserDayCalendar.LUNCH_START_TP);
        LocalTime lEnd = LocalTime.ofSecondOfDay(UserDayCalendar.LUNCH_END_TP);
        List<CalendarItem> result = new ArrayList<>();

        boolean itemInCenterOfGroup = !isFirst && !isLast;
        boolean oneItemWithLunchInTheMiddle = isFirst && isLast && item.getStartDate().isBefore(lStart) && item.getDeadline().isAfter(lEnd);
        boolean firstItemStartsBeforeLunch = isFirst && !isLast && item.getStartDate().isBefore(lStart);
        boolean lastItemEndsAfterLunch = !isFirst && isLast && item.getDeadline().isAfter(lEnd);

        if( itemInCenterOfGroup || oneItemWithLunchInTheMiddle || firstItemStartsBeforeLunch || lastItemEndsAfterLunch ){
            CalendarItem newItem = new CalendarItem(item);
            item.setDeadline(lStart);
            newItem.setStartDate(lEnd);
            if(withCompensation){
                LocalTime newDeadLine = newItem.getDeadline();
                if(newDeadLine.isAfter(LocalTime.of(22,55))) newDeadLine = LocalTime.of(23,55);
                else newDeadLine = newDeadLine.plusHours(1);
                newItem.setDeadline(newDeadLine);
            }
            result.add(item);
            result.add(newItem);
        } else result.add(item);

        return result;
    }

    private List<List<CalendarItem>> handleRepetition(List<CalendarItem> repeatedItems, int[] repetitionDays){
        List<List<CalendarItem>> result = new ArrayList<>();
        LocalDate repetitionsStartDate = this.startDate;
        LocalDate repetitionsFinalDate = this.endDate;

        List<LocalDate> repetitionDates = new ArrayList<>();

        for(int dayNumber: repetitionDays){
            LocalDate date = repetitionsStartDate.with(DayOfWeek.of(dayNumber));
            while (!date.isAfter(repetitionsFinalDate)){
                if(!date.isBefore(repetitionsStartDate)) repetitionDates.add(date);
                date = date.plusDays(7);
            }
        }

        for (LocalDate date : repetitionDates){
            List<CalendarItem> group = new ArrayList<>();
            for(CalendarItem item : repeatedItems){
                CalendarItem newItem = new CalendarItem(item);
                newItem.setItemDate(date);
                group.add(newItem);
            }
            result.add(group);
        }


        return result;
    }


}
