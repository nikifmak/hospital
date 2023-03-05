package com.okto.hospital.service.appointment;

import com.okto.hospital.model.response.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentService {

    Appointment createAppointment(Integer doctorId, Integer patientId, LocalDate date, LocalTime startTime);
}
