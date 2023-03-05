package com.okto.hospital.mapper;

import com.okto.hospital.model.AppointmentEntity;
import com.okto.hospital.model.response.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "doctorId", source = "appointmentEntity.doctor.id")
    @Mapping(target = "patientId", source = "appointmentEntity.patient.id")
    Appointment toAppointment(AppointmentEntity appointmentEntity);
}
