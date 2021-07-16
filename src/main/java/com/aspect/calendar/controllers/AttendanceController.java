package com.aspect.calendar.controllers;

import com.aspect.calendar.entity.exceptions.AuthenticationRequiredException;
import com.aspect.calendar.entity.exceptions.EntityNotExistException;
import com.aspect.calendar.entity.timeManagement.Attendance;
import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalTime;

import static com.aspect.calendar.utils.CommonUtils.timeFormatter;


@Controller
public class AttendanceController extends PlainController{
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @RequestMapping(value = {"/ajax/timeManagerStatus"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String isUserActionRequired() throws AuthenticationRequiredException {
        return this.attendanceService.isUserActionRequired(getAuthenticatedUser().getId());
    }

    @RequestMapping(value = {"/ajax/getTimeManagerDropdown"}, method = RequestMethod.POST)
    public String getTimeManagerDropdown(Model model) throws AuthenticationRequiredException {
        int currentUserId = getAuthenticatedUser().getId();
        model.addAttribute("overdueAttendance", this.attendanceService.getOverdueAttendances(currentUserId));
        model.addAttribute("todayAttendance", this.attendanceService.getTodayAttendance(currentUserId));

        return "timeManagerDropdown";
    }

    @RequestMapping(value = {"/ajax/updateAttendance"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateAttendance(int id, @RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) throws AuthenticationRequiredException, EntityNotExistException {
        Attendance attendance;
        if(id == 0) attendance = this.attendanceService.clockInUser(getAuthenticatedUser(), time);
        else attendance = this.attendanceService.clockOutUser(id, getAuthenticatedUser(), time);

        String clockIn = attendance.getClockIn() == null ? "--:--" : attendance.getClockIn().format(timeFormatter);
        String clockOut = attendance.getClockOut() == null ? "--:--" : attendance.getClockOut().format(timeFormatter);


        return "{\"Status\":\"SUCCESS\", \"id\":\"" + attendance.getId() + "\",\"clockInTime\":\"" + clockIn + "\",\"clockOutTime\":\"" + clockOut + "\"}";
    }

}
