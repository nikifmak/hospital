package com.okto.hospital.service.appointment.validation;

import com.okto.hospital.exception.SlotAlreadyBooked;
import com.okto.hospital.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component("slotAlreadyBookedValidator")
public class SlotAlreadyBookedValidator implements AppointmentValidator{

    private static final Logger log = LoggerFactory.getLogger(SlotAlreadyBookedValidator.class);

    private final AppointmentRepository appointmentRepository;
    private AppointmentValidator next;

    public SlotAlreadyBookedValidator(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Chains next validator if the doctor is not booked in that time slot. We fetch all the appointments
     * of the doctor on that day and check if the appointment's start time is equal to the proposed
     *
     * @param doctorId Doctor id
     * @param patientId pa
     *
     * @param date the date of the appointment
     * @param appointmentStartTime the start time of the appointment(Start of the slot)
     */
    @Override
    public void validate(Integer doctorId, Integer patientId, LocalDate date, LocalTime appointmentStartTime) {
        var isDoctorFreeInThatTimeSlot = appointmentRepository.findByDoctorIdAndDate(doctorId, date)
                .stream()
                .noneMatch(appointment -> appointment.getStartTime().equals(appointmentStartTime));

        if (!isDoctorFreeInThatTimeSlot) {
            log.info("Slot already booked for doctorId: {}, date: {}, startTime: {}", doctorId, date, appointmentStartTime);
            throw new SlotAlreadyBooked();
        }
        if (this.next != null) {
            this.next.validate(doctorId, patientId, date, appointmentStartTime);
        }
    }

    @Override
    public AppointmentValidator setNext(AppointmentValidator next) {
        return null;
    }
}
