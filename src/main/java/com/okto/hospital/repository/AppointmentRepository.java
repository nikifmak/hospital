package com.okto.hospital.repository;

import com.okto.hospital.model.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Integer> {

    List<AppointmentEntity> findByDoctorIdAndDate(Integer doctorId, LocalDate date);
}
