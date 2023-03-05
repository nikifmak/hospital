package com.okto.hospital.service;

import com.okto.hospital.model.request.ScheduleRequest;
import com.okto.hospital.model.response.Schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleService {
    List<Schedule> getScheduleByDoctorId(Integer doctorId);

    Schedule createSchedule(Integer doctorId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);

    Schedule updateSchedule(Integer doctorId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);

    List<Schedule> upsertScheduleList(Integer doctorId, List<ScheduleRequest> scheduleRequestList);

}
