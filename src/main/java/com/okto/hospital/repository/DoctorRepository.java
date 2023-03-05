package com.okto.hospital.repository;

import com.okto.hospital.model.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<DoctorEntity, Integer>{
}
