package com.aspect.calendar.controllers;

import com.aspect.calendar.entity.user.AppUser;
import com.aspect.calendar.entity.exceptions.FolderCreationException;
import com.aspect.calendar.entity.calendar.CalendarItem;
import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.entity.calendar.UserDayCalendar;
import com.aspect.calendar.entity.enums.Division;
import com.aspect.calendar.entity.report.LoadReport;
import com.aspect.calendar.form.CalendarItemForm;
import com.aspect.calendar.form.SubmitItemForm;
import com.aspect.calendar.service.CalendarItemService;
import com.aspect.calendar.service.ProjectEntitiesService;
import com.aspect.calendar.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.*;

import static com.aspect.calendar.utils.CommonUtils.sqlDateFormatter;
import static com.aspect.calendar.utils.CommonUtils.timeFormatter;

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

    public AppUser getAuthenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) return null;
        return (AppUser) authentication.getPrincipal();
    }

    @RequestMapping(value = {"/"}, method = RequestMethod.GET)
    public String welcome() {
        return "redirect:/calendar";
    }

    @RequestMapping(value = {"/calendar"})
    public String calendar(Model model, @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate calendarDate, @RequestParam(required = false) Integer responsibleManager) {
        if(calendarDate == null) calendarDate = LocalDate.now();
        List<UserDayCalendar> calendar;

        // Set TP value only if calendar for current day
        model.addAttribute("currentTP", calendarDate.equals(LocalDate.now()) ? LocalTime.now().toSecondOfDay() : -1);
        model.addAttribute("calendarDate", calendarDate);

        if(this.getAuthenticatedUser().hasRole("USER")){
            calendar = this.calendarItemService.getUserDayCalendar(this.getAuthenticatedUser(), calendarDate);
            model.addAttribute("calendar", calendar);

            return "providerCalendar";
        }
        else{
            calendar = this.calendarItemService.getAllUsersDayCalendar(calendarDate, responsibleManager);
            model.addAttribute("responsibleManager", responsibleManager);
            model.addAttribute("managers", this.userDetailsService.getAllActiveManagers());
            model.addAttribute("calendar", calendar);

            return "managerCalendar";
        }
    }

    @RequestMapping(value = {"/ajax/newItemToggle"}, method = RequestMethod.POST)
    public String newItemToggle(HttpServletRequest request, Model model, @RequestParam("providerId") int providerId,
                                @RequestParam("itemDate") String itemDate,
                                @RequestParam ("startTP") int startTP) {
        if(providerId > 0){
            LocalDate date;
            try{
                date = LocalDate.parse(itemDate, sqlDateFormatter);
                if(date.isBefore(LocalDate.now()) && !request.isUserInRole("ADMIN")) return "forward:/ajaxMessage";
            } catch (DateTimeParseException ignored){
                return "forward:/ajaxMessage";
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

        return "forward:/ajaxMessage";
    }

    @RequestMapping(value = {"/ajax/addNewItem"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String newItem(SubmitItemForm form, HttpServletResponse response) {

        LocalDateTime startDateTime = form.getStartDate().atTime(form.getStartDateHour(), form.getStartDateMinute());
        if(startDateTime.isBefore(LocalDateTime.now()) && ! this.getAuthenticatedUser().hasRole("ADMIN")){
            response.setStatus(403);
            return "{\"status\":\"Error\",\"message\":\"" + "You don't allowed to create items in the past" + "\"}";
        }
        if(!form.isValid()){
            response.setStatus(400);
            return "{\"status\":\"Error\",\"message\":\"" + form.getErrorMessage() + "\"}";
        }

        List<List<CalendarItem>> groups = form.split();

        StringBuilder sb = new StringBuilder();
        Person creator = this.getAuthenticatedUser();
        for(List<CalendarItem> group : groups){
            String intersectedItems = this.calendarItemService.getIntersectedItemsTitle(group);
            if(intersectedItems != null) sb.append(' ').append(intersectedItems);
            for (CalendarItem item : group){
                item.setCreatedBy(creator);
            }
        }

        if(sb.toString().length() > 0){
            response.setStatus(422);
            return "{\"status\":\"Error\",\"message\":\"" + "Creation failed! New item intersect: " + sb.toString() + "\"}";
        }

        for (List<CalendarItem> group : groups){
            this.calendarItemService.saveGroup(group);
        }

        return "{\"status\":\"Success\",\"calendarDate\":\"" + startDateTime.toLocalDate() + "\"}";
    }

    @RequestMapping(value = {"/ajax/itemInfo"}, method = RequestMethod.POST)
    public String itemInfo(@RequestParam ("itemId") int itemId, Model model) {
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
        List<Person> managers = this.userDetailsService.getAllActiveManagers();

        model.addAttribute("managers", managers);
        model.addAttribute("item", this.conversionService.convert(item, CalendarItemForm.class));
        return "editItemToggle";
    }

    @RequestMapping(value = {"/ajax/editItem"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String editItem(SubmitItemForm form, HttpServletResponse response) {
        if(!form.isValid()){
            response.setStatus(400);
            return "{\"status\":\"Error\",\"message\":\"" + form.getErrorMessage() + "\"}";
        }

        CalendarItem item = form.convertToCalendarItem();
        if(item == null){
            response.setStatus(422);
            return "{\"status\":\"Error\",\"message\":\"" + "Event start and deadline date must be the same. To extend item use split function" + "\"}";
        }

        item.setModifiedBy(this.getAuthenticatedUser());
        LocalDateTime startDateTime = item.getItemDate().atTime(item.getStartDate());
        if(startDateTime.isBefore(LocalDateTime.now()) && !this.getAuthenticatedUser().hasRole("ADMIN")){
            CalendarItem existingItem = this.calendarItemService.getItemById(item.getId());
            if(startDateTime.plusMinutes(15).isBefore(existingItem.getItemDate().atTime(existingItem.getStartDate()))){
                response.setStatus(403);
                return "{\"status\":\"Error\",\"message\":\"" + "You don't allowed to create items in the past" + "\"}";
            }
        }

        String intersectedItems = this.calendarItemService.getIntersectedItemsTitle(item);
        if(intersectedItems != null){
            response.setStatus(422);
            return "{\"status\":\"Error\",\"message\":\"" + "Save failed! New item intersect: " + intersectedItems + "\"}";
        }

        this.calendarItemService.saveItem(item);

        return "{\"status\":\"Success\",\"calendarDate\":\"" + item.getItemDate() + "\"}";
    }

    @RequestMapping(value = {"/ajax/split"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String editItem(int itemId, String splitTime) {
        CalendarItem item = this.calendarItemService.getItemById(itemId);

        LocalTime sTime = LocalTime.parse(splitTime, timeFormatter);
        if(!sTime.minusMinutes(10).isBefore(item.getStartDate()) && !sTime.plusMinutes(10).isAfter(item.getDeadline())){
            CalendarItem newItem = new CalendarItem(item);
            item.setDeadline(sTime);
            newItem.setStartDate(sTime);

            List<CalendarItem> group = new ArrayList<>();
            group.add(item);
            group.add(newItem);

            this.calendarItemService.saveGroup(group);
        }

        return "{\"status\":\"Success\"}";

    }

    @RequestMapping(value = {"/ajax/delete"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@RequestParam ("itemId") int itemId, @RequestParam(value = "groupId", required = false) Integer groupId, Model model, HttpServletResponse response) {
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
        List<Person> providers = this.userDetailsService.getAllActiveProviders();

        List<List<Person>> divisions = new ArrayList<>();

        if(providers.size() > 0){
            Division divisionName = providers.get(0).getDivision();
            List<Person> divisionProviders = new ArrayList<>();
            for (Person person : providers){
                if(person.getDivision() != divisionName){
                    divisions.add(divisionProviders);
                    divisionProviders = new ArrayList<>();
                    divisionName = person.getDivision();
                }
                divisionProviders.add(person);
            }
            divisions.add(divisionProviders);
        }

        model.addAttribute("lastMonthStart", lastMonthStart);
        model.addAttribute("lastMonthEnd", lastMonthEnd);
        model.addAttribute("divisions", divisions);

        return "statisticParamsToggle";
    }

    @RequestMapping(value = {"/ajax/reportPreview"}, method = RequestMethod.POST)
    public String reportPreviewToggle(@RequestParam("dateFrom") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
                                      @RequestParam("dateTo") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
                                      int providerId,
                                      Model model) {

        LoadReport report = this.calendarItemService.getReport(dateFrom, dateTo, providerId);
        reportMap.put(getAuthenticatedUser().getId(), report);
        model.addAttribute("report", report);

        return "reportPreviewToggle";
    }

    @RequestMapping(value = {"/getCSVReport"}, method = RequestMethod.POST)
    public void getCSVReport(int reportHash, HttpServletResponse response){
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

    @RequestMapping(value = {"/ajaxMessage"})
    public ResponseEntity<String> getError(HttpServletRequest request){
        HttpStatus status = (HttpStatus) request.getAttribute("status");

        String message = (String) request.getAttribute("message");
        if(message == null) message = "Bad Request";

        return new ResponseEntity<>( message, status == null ? HttpStatus.BAD_REQUEST : status );
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
