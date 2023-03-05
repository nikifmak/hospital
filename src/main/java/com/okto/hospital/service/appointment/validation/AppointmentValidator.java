package com.okto.hospital.service.appointment.validation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentValidator {
    void validate(Integer doctorId, Integer patientId, LocalDate date, LocalTime startTime);

    AppointmentValidator setNext(AppointmentValidator next);

    default DayOfWeek getDayOfWeek(LocalDate date) {
        return date.getDayOfWeek();
    }
}