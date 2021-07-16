package com.aspect.calendar.controllers;

import com.aspect.calendar.entity.calendar.CalendarItem;
import com.aspect.calendar.entity.calendar.UserDayCalendar;
import com.aspect.calendar.entity.exceptions.AuthenticationRequiredException;
import com.aspect.calendar.entity.exceptions.CalendarItemProcessingException;
import com.aspect.calendar.entity.exceptions.InvalidValueException;
import com.aspect.calendar.entity.report.LoadReport;
import com.aspect.calendar.form.SubmitItemForm;
import com.aspect.calendar.service.CalendarItemService;
import com.aspect.calendar.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class CalendarController extends PlainController{
    private final Map<Integer, LoadReport> reportMap = new HashMap<>();
    private final CalendarItemService calendarItemService;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public CalendarController(CalendarItemService calendarItemService, UserDetailsServiceImpl userDetailsService) {
        this.calendarItemService = calendarItemService;
        this.userDetailsService = userDetailsService;
    }

    @RequestMapping(value = {"/calendar"})
    public String calendar(Model model,
                           @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate calendarDate,
                           @RequestParam(required = false) Integer responsibleManager,
                           @RequestParam(defaultValue = "false") boolean inspectionMode) throws AuthenticationRequiredException {
        if(calendarDate == null) calendarDate = LocalDate.now();
        List<UserDayCalendar> calendar;

        // Set TP value only if calendar for current day
        model.addAttribute("currentTP", calendarDate.equals(LocalDate.now()) ? LocalTime.now().toSecondOfDay() : -1);
        model.addAttribute("calendarDate", calendarDate);

        if(this.getAuthenticatedUser().hasRole("USER")){
            calendar = this.calendarItemService.getUserDayCalendar(this.getAuthenticatedUser(), calendarDate);
        } else{
            calendar = this.calendarItemService.getAllUsersDayCalendar(calendarDate, responsibleManager);
            model.addAttribute("managerView", true);
            model.addAttribute("inspectionMode", inspectionMode && this.getAuthenticatedUser().hasRole("ADMIN"));
            model.addAttribute("responsibleManager", responsibleManager);
            model.addAttribute("managers", this.userDetailsService.getAllActiveManagers());
        }

        model.addAttribute("calendar", calendar);
        return "calendar";
    }

    @RequestMapping(value = {"/ajax/newItemToggle"}, method = RequestMethod.POST)
    public String newItemToggle(Model model, @RequestParam("providerId") int providerId,
                                @RequestParam("itemDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate itemDate,
                                @RequestParam ("startTP") int startTP) throws AuthenticationRequiredException, InvalidValueException {
        if(providerId == 0) throw new InvalidValueException("Provider is invalid");

        int tpInMinutes = startTP / 60;
        int itemStartHour = tpInMinutes / 60;
        int itemStartMinute = tpInMinutes % 60;
        //Round up to 5
        itemStartMinute = (int)Math.ceil(itemStartMinute / 5f) * 5;

        if(itemStartMinute == 60){
            itemStartMinute = 0;
            itemStartHour++;
        }

        model.addAttribute("loggedUser", this.getAuthenticatedUser());
        model.addAttribute("provider", this.userDetailsService.getPersonById(providerId));
        model.addAttribute("managers", this.userDetailsService.getAllActiveManagers());
        model.addAttribute("itemStartHour", itemStartHour);
        model.addAttribute("itemStartMinute", itemStartMinute);
        model.addAttribute("itemDate", itemDate);

        return "newItemToggle";
    }



    @RequestMapping(value = {"/ajax/addNewItem"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String newItem(SubmitItemForm form, HttpServletResponse response) throws AuthenticationRequiredException  {
        try{
            return this.calendarItemService.addNewItem(form, getAuthenticatedUser());
        } catch (CalendarItemProcessingException ex){
            response.setStatus(ex.getResponseStatus().value());
            return ex.getMessage();
        }
    }

    @RequestMapping(value = {"/ajax/itemInfo"}, method = RequestMethod.POST)
    public String itemInfo(@RequestParam ("itemId") long itemId, Model model) {
        CalendarItem item = this.calendarItemService.getItemById(itemId);
        LocalTime minSplitTime = item.getStartDate().plusMinutes(10);

        /*
        if(!this.getAuthenticatedUser().hasRole("ADMIN") && item.getItemDate().isAfter(LocalDate.now())){
            minSplitTime = LocalTime.now().withMinute(LocalTime.now().getMinute() / 5 * 5).plusMinutes(10);
        }
         */

        LocalTime maxSplitTime = item.getDeadline().minusMinutes(10);

        model.addAttribute("item", item);
        model.addAttribute("minSplitTime", minSplitTime);
        model.addAttribute("maxSplitTime", maxSplitTime);
        return "itemInfoToggle";
    }

    @RequestMapping(value = {"/ajax/editItemToggle"}, method = RequestMethod.POST)
    public String editItemToggle(@RequestParam ("itemId") long itemId, Model model) {
        CalendarItem item = this.calendarItemService.getItemById(itemId);

        model.addAttribute("managers", this.userDetailsService.getAllActiveManagers());
        model.addAttribute("item", item);
        return "editItemToggle";
    }

    @RequestMapping(value = {"/ajax/editItem"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String editItem(SubmitItemForm form, HttpServletResponse response) throws AuthenticationRequiredException  {
        try{
            return this.calendarItemService.editItem(form, getAuthenticatedUser());
        } catch (CalendarItemProcessingException ex){
            response.setStatus(ex.getResponseStatus().value());
            return ex.getMessage();
        }
    }

    @RequestMapping(value = {"/ajax/split"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String splitItem(int itemId, String splitTime) {
        return this.calendarItemService.splitItem(itemId, splitTime);
    }

    @RequestMapping(value = {"/ajax/delete"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@RequestParam ("itemId") long itemId, @RequestParam(value = "groupId", required = false) Long groupId) throws AuthenticationRequiredException {
        if(groupId != null) this.calendarItemService.deleteGroupOfItems(groupId, itemId, getAuthenticatedUser());
        else this.calendarItemService.deleteItem(itemId, getAuthenticatedUser());

        return "{\"status\":\"Success\"}";
    }

    @RequestMapping(value = {"/ajax/statisticParamsToggle"}, method = RequestMethod.POST)
    public String statisticToggle(Model model) {
        LocalDate lastMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = lastMonthStart.withDayOfMonth(lastMonthStart.lengthOfMonth());

        model.addAttribute("lastMonthStart", lastMonthStart);
        model.addAttribute("lastMonthEnd", lastMonthEnd);
        model.addAttribute("divisions", this.userDetailsService.getProvidersSeparateByDivisions());

        return "statisticParamsToggle";
    }

    @RequestMapping(value = {"/ajax/reportPreview"}, method = RequestMethod.POST)
    public String reportPreviewToggle(@RequestParam("dateFrom") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
                                      @RequestParam("dateTo") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
                                      int providerId,
                                      Model model) throws AuthenticationRequiredException  {

        LoadReport report = this.calendarItemService.getReport(dateFrom, dateTo, providerId);
        reportMap.put(getAuthenticatedUser().getId(), report);
        model.addAttribute("report", report);

        return "reportPreviewToggle";
    }

    @RequestMapping(value = {"/getCSVReport"}, method = RequestMethod.POST)
    public void getCSVReport(int reportHash, HttpServletResponse response) throws AuthenticationRequiredException {
        String contentDisposition = "attachment;filename=WorkloadReport.csv";

        String csv = "";
        LoadReport report = reportMap.get(this.getAuthenticatedUser().getId());
        if(report != null && report.hashCode() == reportHash) {
            csv = report.toCSV();
            contentDisposition = "attachment;filename=WorkloadReport_" + report.getDateFrom() + "_" + report.getDateTo() + ".csv";
        }

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-Disposition", contentDisposition);

        try (OutputStream out = response.getOutputStream()) {
            out.write(csv.getBytes());
            response.flushBuffer();
        } catch (IOException ignored){}
    }

    @RequestMapping(value = {"/exportItemsAsCSV"}, method = RequestMethod.POST)
    public void exportCalendarItems(@RequestParam("dateFrom") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
                                    @RequestParam("dateTo") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
                                    int providerId,
                                    HttpServletResponse response){

        String csv = this.calendarItemService.getItemsAsCSV(dateFrom, dateTo, providerId);

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-Disposition", "attachment;filename=CalendarItems_" + dateFrom + "_" + dateTo + ".csv");

        try (OutputStream out = response.getOutputStream()) {
            out.write(csv.getBytes());
            response.flushBuffer();
        } catch (IOException ignored){}
    }
}
