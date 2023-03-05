package com.okto.hospital.controller;

import com.okto.hospital.model.request.AppointmentRequest;
import com.okto.hospital.model.response.Appointment;
import com.okto.hospital.service.AppointmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Create an appointment for a patient with a doctor
     *
     * @param doctorId          The doctor's id
     * @param appointmentRequest The appointment request
     * @return The created appointment
     */
    @PostMapping("/doctors/{doctorId}/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment createAppointment(
            @PathVariable Integer doctorId,
            @Valid @NotNull @RequestBody AppointmentRequest appointmentRequest
    ) {
        return appointmentService.createAppointment(doctorId,
                appointmentRequest.patientId(),
                appointmentRequest.date(),
                appointmentRequest.startTime()
        );
    }
}
