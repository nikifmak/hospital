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

    private LocalTime calculateEndTime(ScheduleEntity dayAvailability, LocalTime startTime) {
        LocalTime plusOneHour = startTime.plusMinutes(60);
        return (plusOneHour.isAfter(dayAvailability.getEndTime()))
                ? dayAvailability.getEndTime()
                : plusOneHour;
    }

    private DayOfWeek getDayOfWeek(LocalDate date) {
        return date.getDayOfWeek();
    }

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

    private boolean isDoctorFreeInThatTimeSlot(Integer doctorId, LocalDate date, LocalTime appointmentStartTime) {
        return appointmentRepository.findByDoctorIdAndDate(doctorId, date)
                .stream()
                .noneMatch(appointment -> appointment.getStartTime().equals(appointmentStartTime));
    }
}
