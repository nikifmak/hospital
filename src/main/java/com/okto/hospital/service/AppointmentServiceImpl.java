package com.okto.hospital.service;

import com.okto.hospital.exception.DoctorScheduleAvailabilityException;
import com.okto.hospital.exception.SlotAlreadyBooked;
import com.okto.hospital.exception.SlotNotBookable;
import com.okto.hospital.mapper.AppointmentMapper;
import com.okto.hospital.model.AppointmentEntity;
import com.okto.hospital.model.DoctorEntity;
import com.okto.hospital.model.PatientEntity;
import com.okto.hospital.model.ScheduleEntity;
import com.okto.hospital.model.response.Appointment;
import com.okto.hospital.repository.AppointmentRepository;
import com.okto.hospital.repository.ScheduleRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final EntityManager entityManager;
    private final AppointmentMapper appointmentMapper;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, ScheduleRepository scheduleRepository, EntityManager entityManager, AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.entityManager = entityManager;
        this.appointmentMapper = appointmentMapper;
    }

    /**
     * Creates a new appointment for the specified doctor, patient, date, and start time.
     * Throws an exception if the doctor is not available on that day, the slot is not bookable, or the doctor is already booked in that time slot.
     * Calls the private method saveAppointment() to persist the new appointment to the database.
     * Returns the new Appointment object created from the persisted AppointmentEntity object.
     *
     * @param doctorId   Doctor id
     * @param patientId  Patient id
     * @param date       Date of the appointment
     * @param startTime  Start time of the appointment
     * @return Appointment
     */
    @Override
    public Appointment createAppointment(Integer doctorId, Integer patientId, LocalDate date, LocalTime startTime) {
        var dayAvailability = scheduleRepository.findByDoctorIdAndDayOfWeek(
                doctorId,
                getDayOfWeek(date)
        );

        if (dayAvailability.isEmpty() || !isDoctorAvailableThatDayAndHours(dayAvailability, doctorId, date, startTime)) {
            throw new DoctorScheduleAvailabilityException();
        }

        if (!isSlotBookable(dayAvailability, startTime)) {
            throw new SlotNotBookable();
        }

        if (!isDoctorFreeInThatTimeSlot(doctorId, date, startTime)) {
            log.info("Slot already booked for doctorId: {}, date: {}, startTime: {}", doctorId, date, startTime);
            throw new SlotAlreadyBooked();
        }

        AppointmentEntity result = saveAppointment(dayAvailability.get(), doctorId, patientId, date, startTime);

        return appointmentMapper.toAppointment(result);
    }

    /**
     * Creates a new AppointmentEntity object and saves it to the database via the appointmentRepository.
     * Returns the saved AppointmentEntity object.
     *
     * @param dayAvailability the aviailability of the doctor on that day
     * @param doctorId Doctor id
     * @param patientId Patient id
     * @param date the date of the appointment
     * @param startTime the start time of the appointment(Start of the slot)
     * @return List of appointments
     */
    private AppointmentEntity saveAppointment(
            ScheduleEntity dayAvailability,
            Integer doctorId,
            Integer patientId,
            LocalDate date,
            LocalTime startTime
    ) {
        LocalTime endTime = calculateEndTime(dayAvailability, startTime);

        DoctorEntity doctor = entityManager.getReference(DoctorEntity.class, doctorId);
        PatientEntity patient = entityManager.getReference(PatientEntity.class, patientId);

        return appointmentRepository.save(
                new AppointmentEntity(patient,
                        doctor,
                        date,
                        startTime,
                        endTime)
        );
    }


    /**
     * Returns the end time of the appointment. If startTime + 60 minutes exceeds the end of
     * the doctor's working hour then we return the end of the doctor's working hour.
     *
     * @param dayAvailability the availability of the doctor on that day
     * @param startTime the start time of the appointment(Start of the slot)
     * @return List of appointments
     */
    private LocalTime calculateEndTime(ScheduleEntity dayAvailability, LocalTime startTime) {
        LocalTime plusOneHour = startTime.plusMinutes(60);
        return (plusOneHour.isAfter(dayAvailability.getEndTime()))
                ? dayAvailability.getEndTime()
                : plusOneHour;
    }

    private DayOfWeek getDayOfWeek(LocalDate date) {
        return date.getDayOfWeek();
    }

    /**
     * Returns true if the doctor is works that day and the working hours include
     * the appointment's start time.
     *
     * @param dayAvailability doctor's availability on that day
     * @param doctorId Doctor id
     * @param date the date of the appointment
     * @param appointmentStartTime the start time of the appointment(Start of the slot)
     * @return boolean
     */
    private boolean isDoctorAvailableThatDayAndHours(
            Optional<ScheduleEntity> dayAvailability,
            Integer doctorId,
            LocalDate date,
            LocalTime appointmentStartTime
    ) {
        if (dayAvailability.isEmpty()) {
            log.info("Doctor with id={} is not available that day={}", doctorId, date);
            return false;
        }

        var startHour = dayAvailability.get().getStartTime();
        var endHour = dayAvailability.get().getEndTime();
        var isAvailableThatHours = (startHour.equals(appointmentStartTime) || startHour.isBefore(appointmentStartTime))
                && endHour.isAfter(appointmentStartTime);

        log.info("Doctor with id={}, isAvailableThatHours={} for day={} and hours={}", doctorId, isAvailableThatHours, date, appointmentStartTime);
        return isAvailableThatHours;
    }

    /**
     * Given the working hours, its generates the available slots for the doctor.
     * We are assuming the doctor works in 1 hour slots.
     * So if they work from 10:00 to 16:00, they have 6 slots available.
     * which are 10:00, 11:00, 12:00, 13:00, 14:00, 15:00
     * After generating the timeslots, checks if the proposed appointment's start time is in the list of available slots.
     *
     * @param dayAvailability doctor's availability on that day
     * @param appointmentStartTime the start time of the appointment(Start of the slot)
     * @return boolean returns true if the appointment's start time is in the list of available slots.
     */
    private boolean isSlotBookable(Optional<ScheduleEntity> dayAvailability, LocalTime appointmentStartTime) {
        if (dayAvailability.isEmpty()) {
            return false;
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

        return slots.contains(appointmentStartTime);
    }

    /**
     * Returns true if the doctor is not booked in that time slot. We fetch all the appointments
     * of the doctor on that day and check if the appointment's start time is equal to the proposed
     *
     * @param doctorId Doctor id
     * @param date the date of the appointment
     * @param appointmentStartTime the start time of the appointment(Start of the slot)
     * @return boolean true
     */
    private boolean isDoctorFreeInThatTimeSlot(Integer doctorId, LocalDate date, LocalTime appointmentStartTime) {
        return appointmentRepository.findByDoctorIdAndDate(doctorId, date)
                .stream()
                .noneMatch(appointment -> appointment.getStartTime().equals(appointmentStartTime));
    }
}
