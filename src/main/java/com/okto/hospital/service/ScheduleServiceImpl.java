package com.okto.hospital.service;


import com.okto.hospital.exception.ResourceNotFound;
import com.okto.hospital.mapper.ScheduleMapper;
import com.okto.hospital.model.response.Schedule;
import com.okto.hospital.repository.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    public ScheduleServiceImpl(ScheduleRepository scheduleRepository, ScheduleMapper scheduleMapper) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    public List<Schedule> getScheduleByDoctorId(Integer doctorId) {
        return scheduleRepository.findAllByDoctorId(doctorId)
                .filter(schedules -> !schedules.isEmpty())
                .map(schedules -> schedules.stream()
                        .map(scheduleMapper::toSchedule)
                        .toList()
                )
                .orElseThrow(ResourceNotFound::new);
    }
}
