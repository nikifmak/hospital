package com.okto.hospital.service;

import com.okto.hospital.model.response.Schedule;

import java.util.List;

public interface ScheduleService {
    List<Schedule> getScheduleByDoctorId(Integer doctorId);
}
