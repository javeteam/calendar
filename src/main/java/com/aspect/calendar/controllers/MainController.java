package com.aspect.calendar.controllers;

import com.aspect.calendar.entity.exceptions.AuthenticationRequiredException;
import com.aspect.calendar.entity.exceptions.CalendarItemProcessingException;
import com.aspect.calendar.entity.exceptions.InvalidValueException;
import com.aspect.calendar.entity.user.AppUser;
import com.aspect.calendar.entity.exceptions.FolderCreationException;
import com.aspect.calendar.entity.calendar.CalendarItem;
import com.aspect.calendar.entity.calendar.UserDayCalendar;
import com.aspect.calendar.entity.report.LoadReport;
import com.aspect.calendar.form.CalendarItemForm;
import com.aspect.calendar.form.SubmitItemForm;
import com.aspect.calendar.service.CalendarItemService;
import com.aspect.calendar.service.ProjectEntitiesService;
import com.aspect.calendar.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import static com.aspect.calendar.utils.CommonUtils.sqlDateFormatter;

@Controller
public class MainController {
    private final Map<Integer, LoadReport> reportMap = new HashMap<>();
    private final CalendarItemService calendarItemService;
    private final UserDetailsServiceImpl userDetailsService;
    private final ConversionService conversionService;
    private final ProjectEntitiesService projectEntitiesService;

    @Autowired
    MainController(CalendarItemService calendarItemService, UserDetailsServiceImpl userDetailsService, ConversionService conversionService, ProjectEntitiesService projectEntitiesService){
        this.calendarItemService = calendarItemService;
        this.userDetailsService = userDetailsService;
        this.conversionService = conversionService;
        this.projectEntitiesService = projectEntitiesService;
    }

    @ExceptionHandler({AuthenticationRequiredException.class})
    public String handleAuthException(HttpServletResponse response){
        response.setStatus(401);
        return "redirect:/login";
    }

    @ExceptionHandler({InvalidValueException.class})
    public String handleInvalidValueException(HttpServletResponse response, Exception ex){
        response.setStatus(400);
        return "{\"status\":\"Error\",\"message\":\"" + ex.getMessage() + "\"}";
    }

    public AppUser getAuthenticatedUser() throws AuthenticationRequiredException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) throw new AuthenticationRequiredException("User is not authenticated");
        AppUser user = (AppUser) authentication.getPrincipal();
        if(user.getId() == 0) throw new AuthenticationRequiredException("User is not authenticated");

        return user;
    }

    @RequestMapping(value = {"/"}, method = RequestMethod.GET)
    public String welcome() {
        return "redirect:/calendar";
    }

    @RequestMapping(value = {"/calendar"})
    public String calendar(Model model,
                           @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate calendarDate,
                           @RequestParam(required = false) Integer responsibleManager) throws AuthenticationRequiredException {
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
            model.addAttribute("responsibleManager", responsibleManager);
            model.addAttribute("managers", this.userDetailsService.getAllActiveManagers());
        }

        model.addAttribute("calendar", calendar);
        return "calendar";
    }

    @RequestMapping(value = {"/ajax/newItemToggle"}, method = RequestMethod.POST)
    public String newItemToggle(HttpServletRequest request, Model model, @RequestParam("providerId") int providerId,
                                @RequestParam("itemDate") String itemDate,
                                @RequestParam ("startTP") int startTP) throws AuthenticationRequiredException, InvalidValueException {
        if(providerId == 0) throw new InvalidValueException("Provider is invalid");

        LocalDate date;
        try{
            date = LocalDate.parse(itemDate, sqlDateFormatter);
            if(date.isBefore(LocalDate.now()) && !request.isUserInRole("ADMIN")) throw new InvalidValueException("Provider is invalid");
        } catch (DateTimeParseException ignored){
            throw new InvalidValueException("Data format is invalid");
        }

        int currentTP = LocalTime.now().toSecondOfDay();

        if(!request.isUserInRole("ADMIN") && date.isEqual(LocalDate.now()) && startTP < currentTP) startTP = currentTP;
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
        model.addAttribute("currentDate", LocalDate.now());

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
    public String itemInfo(@RequestParam ("itemId") int itemId, Model model) throws AuthenticationRequiredException  {
        CalendarItem item = this.calendarItemService.getItemById(itemId);
        LocalTime minSplitTime;

        if(this.getAuthenticatedUser().hasRole("ADMIN") || item.getItemDate().isAfter(LocalDate.now())){
            minSplitTime = item.getStartDate().plusMinutes(10);
        } else minSplitTime = LocalTime.now().withMinute(LocalTime.now().getMinute() / 5 * 5).plusMinutes(10);

        LocalTime maxSplitTime = item.getDeadline().minusMinutes(10);

        model.addAttribute("item", this.conversionService.convert(item, CalendarItemForm.class));
        model.addAttribute("minSplitTime", minSplitTime);
        model.addAttribute("maxSplitTime", maxSplitTime);
        return "itemInfoToggle";
    }

    @RequestMapping(value = {"/ajax/editItemToggle"}, method = RequestMethod.POST)
    public String editItemToggle(@RequestParam ("itemId") int itemId, Model model) {
        CalendarItem item = this.calendarItemService.getItemById(itemId);

        model.addAttribute("managers", this.userDetailsService.getAllActiveManagers());
        model.addAttribute("item", this.conversionService.convert(item, CalendarItemForm.class));
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
    public String delete(@RequestParam ("itemId") int itemId, @RequestParam(value = "groupId", required = false) Integer groupId, HttpServletResponse response) {
        CalendarItem item = this.calendarItemService.getItemById(itemId);
        boolean success = false;
        if(groupId != null) success = this.calendarItemService.deleteGroup(groupId);
        else if(item != null) success = this.calendarItemService.deleteItem(itemId);

        if(success) return "{\"status\":\"Success\"}";
        else{
            response.setStatus(400);
            return "{\"status\":\"Error\",\"message\":\"Failed to delete item or group\"}";
        }
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


    @RequestMapping(value = {"/login","/logoutSuccessful"})
    public String loginPage(@RequestParam (value = "error", required = false) String error, Model model) {
        if (error != null && error.equals("true")){
            String message = "В авторизації відмовлено. Хибні дані.";
            model.addAttribute("message", message);
        }
        return "login";
    }

    @RequestMapping(value = {"/ajax/newFolderToggle"}, method = RequestMethod.POST)
    public String getNewFolderToggle(Model model){
        model.addAttribute("clients", this.projectEntitiesService.getClients());
        model.addAttribute("workflows", this.projectEntitiesService.getWorkflows());
        model.addAttribute("languages", this.projectEntitiesService.getLanguages());
        return "newFolderToggle";
    }

    @RequestMapping(value = {"/ajax/addNewFolder"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createFolder(String client, String workflow, String sourceLanguage, String[] targetLanguages, String clientEmailSubject, HttpServletResponse response){
        try{
            return this.projectEntitiesService.createFolder(client, workflow, sourceLanguage, targetLanguages, clientEmailSubject);
        } catch (FolderCreationException ex){
            response.setStatus(406);
            return "{\"error\":\"" + ex.getMessage() + "\"}";
        }
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
