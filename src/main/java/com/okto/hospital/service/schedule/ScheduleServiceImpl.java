package com.okto.hospital.service.schedule;


import com.okto.hospital.exception.ResourceAlreadyExists;
import com.okto.hospital.exception.ResourceNotFound;
import com.okto.hospital.mapper.ScheduleMapper;
import com.okto.hospital.model.DoctorEntity;
import com.okto.hospital.model.ScheduleEntity;
import com.okto.hospital.model.request.ScheduleRequest;
import com.okto.hospital.model.response.Schedule;
import com.okto.hospital.repository.ScheduleRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
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

    /**
     * Returns a list of Schedule objects for the specified doctor id.
     * Throws an exception if no schedules were found for the specified doctor id.
     *
     * @param doctorId Doctor id
     * @return List of Schedule objects
     */
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

    /**
     * Creates a new schedule for the specified doctor, day of week, start time, and end time.
     * Throws an exception if the schedule already exists for this specific day and doctor
     * Returns the new Schedule
     *
     * @param doctorId   Doctor id
     * @param dayOfWeek  Day of week
     * @param startTime  Start time
     * @param endTime    End time
     * @return Schedule
     */
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

    /**
     * Updates the schedule for the specified doctor, day of week, start time, and end time.
     * Throws an exception if the schedule does not exist.
     * Returns the updated Schedule
     *
     * @param doctorId   Doctor id
     * @param dayOfWeek  Day of week
     * @param startTime  Start time
     * @param endTime    End time
     * @return Schedule
     */
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


    /**
     * Deletes all schedules for the specified doctor and creates new schedules for the specified doctor.
     * Returns the updated Schedule
     *
     * @param doctorId           Doctor id
     * @param scheduleRequestList List of ScheduleRequest objects
     * @return List of Schedule objects
     */
    @Transactional
    @Override
    public List<Schedule> upsertScheduleList(Integer doctorId, List<ScheduleRequest> scheduleRequestList) {
        DoctorEntity doctor = entityManager.getReference(DoctorEntity.class, doctorId);
        scheduleRepository.deleteAllByDoctor(doctor);
        entityManager.flush();

        List<ScheduleEntity> scheduleEntityList = new ArrayList<>();
        for (ScheduleRequest scheduleRequest : scheduleRequestList) {
            scheduleEntityList.add(new ScheduleEntity(doctor,
                    scheduleRequest.dayOfWeek(),
                    scheduleRequest.startTime(),
                    scheduleRequest.endTime()
            ));
        }

        return scheduleRepository.saveAll(scheduleEntityList)
                .stream()
                .map(scheduleMapper::toSchedule)
                .toList();
    }
}
