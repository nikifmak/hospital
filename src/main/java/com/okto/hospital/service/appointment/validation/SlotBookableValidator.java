package com.okto.hospital.service.appointment.validation;

import com.okto.hospital.exception.SlotNotBookable;
import com.okto.hospital.model.ScheduleEntity;
import com.okto.hospital.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component("slotBookableValidator")
public class SlotBookableValidator implements AppointmentValidator {

    private static final Logger log = LoggerFactory.getLogger(SlotBookableValidator.class);

    private final ScheduleRepository scheduleRepository;
    private AppointmentValidator next;

    public SlotBookableValidator(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * Given the working hours, its generates the available slots for the doctor.
     * We are assuming the doctor works in 1 hour slots.
     * So if they work from 10:00 to 16:00, they have 6 slots available.
     * which are 10:00, 11:00, 12:00, 13:00, 14:00, 15:00
     * After generating the timeslots, checks if the proposed appointment's start time is in the list of available slots.
     * if yes then chain the next validator else throws SlotNotBookable exception.
     *
     * @param doctorId doctor id
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
            throw new SlotNotBookable();
        }

        ScheduleEntity scheduleOfTheDay = dayAvailability.get();
        LocalTime startHour = scheduleOfTheDay.getStartTime();
        LocalTime endHour = scheduleOfTheDay.getEndTime();

        List<LocalTime> slots = new ArrayList<>();
        while (startHour.isBefore(endHour)) {
            slots.add(startHour);
            startHour = startHour.plusMinutes(60);
        }

        log.info("Doctor with id={} for day={} has the following slots available={} and patient wanted to book slot={}",
                scheduleOfTheDay.getDoctor().getId(), scheduleOfTheDay.getDayOfWeek(), slots, appointmentStartTime);

        if (!slots.contains(appointmentStartTime)) {
            throw new SlotNotBookable();
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
