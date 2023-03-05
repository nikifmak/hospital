package com.okto.hospital.service.appointment.validation;

import com.okto.hospital.exception.DoctorScheduleAvailabilityException;
import com.okto.hospital.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component("doctorWorkHoursValidator")
public class DoctorWorkHoursValidator implements AppointmentValidator {

    private static final Logger log = LoggerFactory.getLogger(DoctorWorkHoursValidator.class);

    private AppointmentValidator next;
    private final ScheduleRepository scheduleRepository;

    public DoctorWorkHoursValidator(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * Chain the next validator if the doctor is works that day and the working hours include
     * the appointment's start time.
     *
     * @param doctorId Doctor id
     * @param patientId patient id
     * @param date the date of the appointment
     * @param appointmentStartTime the start time of the appointment(Start of the slot)
     */
    @Override
    public void validate(Integer doctorId, Integer patientId, LocalDate date, LocalTime appointmentStartTime) {
        var dayAvailability = scheduleRepository.findByDoctorIdAndDayOfWeek(
                doctorId,
                getDayOfWeek(date)
        );

        if (dayAvailability.isEmpty()) {
            log.info("Doctor with id={} is not available that day={}", doctorId, date);
            throw new DoctorScheduleAvailabilityException();
        }

        var startHour = dayAvailability.get().getStartTime();
        var endHour = dayAvailability.get().getEndTime();
        var isAvailableThatHours = (startHour.equals(appointmentStartTime) || startHour.isBefore(appointmentStartTime))
                && endHour.isAfter(appointmentStartTime);

        log.info("Doctor with id={}, isAvailableThatHours={} for day={} and hours={}", doctorId, isAvailableThatHours, date, appointmentStartTime);
        if (!isAvailableThatHours) {
            throw new DoctorScheduleAvailabilityException();
        }
        if (this.next != null) {
            this.next.validate(doctorId, patientId, date, appointmentStartTime);
        }
    }

    @Override
    public AppointmentValidator setNext(AppointmentValidator next) {
        this.next = next;
        return next;
    }
}