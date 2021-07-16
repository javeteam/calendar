package com.aspect.calendar.service;

import com.aspect.calendar.dao.AttendanceDao;
import com.aspect.calendar.entity.exceptions.EntityNotExistException;
import com.aspect.calendar.entity.timeManagement.Attendance;
import com.aspect.calendar.entity.user.Person;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AttendanceService {
    private final AttendanceDao attendanceDao;

    public AttendanceService(AttendanceDao attendanceDao) {
        this.attendanceDao = attendanceDao;
    }

    public String isUserActionRequired(int userId){
        List<Attendance> overdueAttendances = this.attendanceDao.getOverdueAttendances(userId);
        Attendance currentAttendance = this.attendanceDao.getTodayAttendance(userId);
        if(!overdueAttendances.isEmpty() || currentAttendance == null) return "{\"Status\":\"SUCCESS\", \"actionRequired\":true}";
        else return "{\"Status\":\"SUCCESS\", \"actionRequired\":false}";
    }

    public List<Attendance> getOverdueAttendances(int userId){
        return this.attendanceDao.getOverdueAttendances(userId);
    }

    public Attendance getTodayAttendance(int userId){
        Attendance todayAttendance = this.attendanceDao.getTodayAttendance(userId);
        if(todayAttendance == null){
            todayAttendance = new Attendance();
            todayAttendance.setUser(new Person(userId));
        }

        return todayAttendance;
    }

    public void save(Attendance attendance){
        if(attendance.getId() == 0) this.attendanceDao.save(attendance);
        else this.attendanceDao.update(attendance);
    }

    public Attendance clockInUser(Person user, LocalTime clockInTime){
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setDate(LocalDate.now());
        attendance.setClockIn(clockInTime);
        attendance.setActualClockIn(LocalDateTime.now());
        save(attendance);

        return attendance;
    }

    public Attendance clockOutUser(int attendanceId, Person user, LocalTime clockOutTime) throws EntityNotExistException {
        Attendance attendance = this.attendanceDao.get(attendanceId);
        if(attendance.getUser().getId() != user.getId()) return new Attendance();
        if(attendance.getClockOut() == null){
            attendance.setClockOut(clockOutTime);
            attendance.setActualClockOut(LocalDateTime.now());
            save(attendance);
        }

        return attendance;
    }
}
