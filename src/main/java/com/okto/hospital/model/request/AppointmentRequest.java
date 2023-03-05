package com.okto.hospital.model.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequest(
        @NotNull
        Integer patientId,
        @NotNull
        LocalDate date,
        @NotNull
        LocalTime startTime
) {
}
