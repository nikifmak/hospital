package com.okto.hospital.service.appointment;

import com.okto.hospital.exception.ResourceNotFound;
import com.okto.hospital.mapper.AppointmentMapper;
import com.okto.hospital.model.AppointmentEntity;
import com.okto.hospital.model.DoctorEntity;
import com.okto.hospital.model.PatientEntity;
import com.okto.hospital.model.response.Appointment;
import com.okto.hospital.repository.AppointmentRepository;
import com.okto.hospital.repository.ScheduleRepository;
import com.okto.hospital.service.appointment.validation.AppointmentValidator;
import com.okto.hospital.service.appointment.validation.DoctorWorkHoursValidator;
import com.okto.hospital.service.appointment.validation.SlotAlreadyBookedValidator;
import com.okto.hospital.service.appointment.validation.SlotBookableValidator;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final EntityManager entityManager;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentValidator appointmentChainValidator;

    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            ScheduleRepository scheduleRepository,
            EntityManager entityManager,
            AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.entityManager = entityManager;
        this.appointmentMapper = appointmentMapper;

        this.appointmentChainValidator = new DoctorWorkHoursValidator(scheduleRepository);
        this.appointmentChainValidator.setNext(new SlotBookableValidator(scheduleRepository))
                .setNext(new SlotAlreadyBookedValidator(appointmentRepository));
    }

    @Override
    public Appointment createAppointment(Integer doctorId, Integer patientId, LocalDate date, LocalTime startTime) {
        appointmentChainValidator.validate(doctorId, patientId, date, startTime);
        AppointmentEntity result = saveAppointment(doctorId, patientId, date, startTime);
        return appointmentMapper.toAppointment(result);
    }

    /**
     * Creates a new AppointmentEntity object and saves it to the database via the appointmentRepository.
     * Returns the saved AppointmentEntity object.
     *
     * @param doctorId Doctor id
     * @param patientId Patient id
     * @param date the date of the appointment
     * @param startTime the start time of the appointment(Start of the slot)
     * @return List of appointments
     */
    private AppointmentEntity saveAppointment(
            Integer doctorId,
            Integer patientId,
            LocalDate date,
            LocalTime startTime
    ) {
        DoctorEntity doctor = entityManager.getReference(DoctorEntity.class, doctorId);
        PatientEntity patient = entityManager.getReference(PatientEntity.class, patientId);

        LocalTime endTime = calculateEndTime(doctor, date, startTime);

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
     * @param doctor the doctor
     * @param date the date of the appointment
     * @param startTime the start time of the appointment(Start of the slot)
     * @return List of appointments
     */
    private LocalTime calculateEndTime(DoctorEntity doctor, LocalDate date, LocalTime startTime) {
        var dayAvailability = scheduleRepository.findByDoctorAndDayOfWeek(
                doctor,
                date.getDayOfWeek()
        );
        if (dayAvailability.isEmpty()) {
            throw new ResourceNotFound();
        }

        LocalTime plusOneHour = startTime.plusMinutes(60);
        return (plusOneHour.isAfter(dayAvailability.get().getEndTime()))
                ? dayAvailability.get().getEndTime()
                : plusOneHour;
    }


}
