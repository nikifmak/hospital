package com.okto.hospital.repository;

import com.okto.hospital.model.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Integer> {

    Optional<List<ScheduleEntity>> findAllByDoctorId(Integer doctorId);

    Optional<ScheduleEntity> findByDoctorIdAndDayOfWeek(Integer doctorId, DayOfWeek dayOfWeek);
}
