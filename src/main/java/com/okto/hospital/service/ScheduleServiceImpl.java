package com.okto.hospital.service;


import com.okto.hospital.exception.ResourceAlreadyExists;
import com.okto.hospital.exception.ResourceNotFound;
import com.okto.hospital.mapper.ScheduleMapper;
import com.okto.hospital.model.DoctorEntity;
import com.okto.hospital.model.ScheduleEntity;
import com.okto.hospital.model.response.Schedule;
import com.okto.hospital.repository.ScheduleRepository;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final EntityManager entityManager;

    public ScheduleServiceImpl(ScheduleRepository scheduleRepository, ScheduleMapper scheduleMapper, EntityManager entityManager) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleMapper = scheduleMapper;
        this.entityManager = entityManager;
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

    @Override
    public Schedule createSchedule(Integer doctorId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        DoctorEntity doctor = entityManager.getReference(DoctorEntity.class, doctorId);
        try {
            ScheduleEntity result = scheduleRepository.save(new ScheduleEntity(doctor, dayOfWeek, startTime, endTime));
            return scheduleMapper.toSchedule(result);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceAlreadyExists();
        }
    }

    @Override
    public Schedule updateSchedule(Integer doctorId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        DoctorEntity doctor = entityManager.getReference(DoctorEntity.class, doctorId);
        return scheduleRepository.findByDoctorAndDayOfWeek(doctor, dayOfWeek)
                .map(schedule -> {
                    schedule.setStartTime(startTime);
                    schedule.setEndTime(endTime);
                    return scheduleRepository.save(schedule);
                })
                .map(scheduleMapper::toSchedule)
                .orElseThrow(ResourceNotFound::new);
    }
}
