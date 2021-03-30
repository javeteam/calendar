package com.aspect.calendar.converters;

import com.aspect.calendar.entity.calendar.CalendarItem;
import com.aspect.calendar.form.CalendarItemForm;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

import static com.aspect.calendar.utils.CommonUtils.*;

public class CalendarItemToFormConverter implements Converter<CalendarItem, CalendarItemForm> {

    @Override
    public CalendarItemForm convert(CalendarItem item) {
        CalendarItemForm form = new CalendarItemForm();
        form.setId(item.getId());
        form.setProviderId(item.getProvider().getId());
        form.setProviderName(item.getProvider().getFullName());
        form.setManagerId(item.getManager().getId());
        form.setManagerName(item.getManager().getFullName());
        form.setCreatedByName(item.getCreatedBy().getFullName());
        form.setModifiedByName(item.getModifiedBy().getFullName());
        form.setStartTP(item.getStartDate().toSecondOfDay());
        form.setEndTP(item.getDeadline().toSecondOfDay());
        form.setCreationDate(item.getCreationDate().format(jDateTimeFormatter));
        form.setModificationDate(item.getModificationDate() == null ? "" : item.getModificationDate().format(jDateTimeFormatter));
        form.setItemDate(item.getItemDate().format(sqlDateFormatter));
        form.setDuration(item.getDeadline().toSecondOfDay() - item.getStartDate().toSecondOfDay());
        form.setPeriod(item.getStartDate() + " - " + item.getDeadline());
        form.setType(item.getType());
        form.setTitle(item.getTitle());
        form.setDescription(item.getDescription());
        form.setGroupId(item.getGroupId());
        form.setPositionInGroup(item.getPositionInGroup());
        form.setGroupSize(item.getGroupSize());
        form.setGroupDuration(item.getGroupDuration());

        form.setGroupStartDatePassed(item.getGroupStartDate() != null && LocalDateTime.now().minusMinutes(15).isAfter(item.getGroupStartDate()));
        form.setStartDatePassed( LocalDateTime.now().minusMinutes(15).isAfter(item.getItemDate().atTime(item.getStartDate())) );
        form.setDeadlinePassed( LocalDateTime.now().minusMinutes(15).isAfter(item.getItemDate().atTime(item.getDeadline())) );

        LocalDateTime sd;
        LocalDateTime dl;
        String groupPeriod = "N/A";

        if(item.getGroupId() > 0){
            sd = item.getGroupStartDate();
            dl = item.getGroupDeadline();
        } else {
            sd = item.getItemDate().atTime(item.getStartDate());
            dl = item.getItemDate().atTime(item.getDeadline());
        }

        if(sd != null && dl != null){
            StringBuilder period = new StringBuilder();
            period.append(sd.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                    .append(' ').append(sd.getDayOfMonth())
                    .append(", ").append(sd.getYear());

            if(sd.toLocalDate().isEqual(dl.toLocalDate())){
                period.append(" (")
                        .append(sd.toLocalTime().format(timeFormatter))
                        .append(" - ")
                        .append(dl.toLocalTime().format(timeFormatter))
                        .append(')');
            } else {
                period.append(' ').append(sd.toLocalTime().format(timeFormatter))
                        .append(" - ")
                        .append(dl.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                        .append(' ').append(dl.getDayOfMonth())
                        .append(", ").append(dl.getYear())
                        .append(' ').append(dl.toLocalTime().format(timeFormatter));
            }
            groupPeriod = period.toString();
        }
        form.setGroupPeriod(groupPeriod);

        return form;
    }
}
