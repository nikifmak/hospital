package com.okto.hospital.controller;

import com.okto.hospital.model.AppointmentEntity;
import com.okto.hospital.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Comparator;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AppointmentControllerTest {

    public static final String CLEAN_TABLES_QUERY = """
            DELETE FROM appointment;
            DELETE FROM schedule;
            DELETE FROM patient; 
            DELETE FROM doctor;
            """;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void test_createAppointment_whenMethodIsNotPost_shouldReturn405() throws Exception {
        mockMvc.perform(get("/v1/doctors/1/appointments"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().string(""));
    }

    @Test
    void test_createAppointment_whenDoctorDoesNotExist_shouldReturn409() throws Exception {
        mockMvc.perform(post("/v1/doctors/1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "patientId": 1,
                                    "date": "2023-03-06",
                                    "startTime": "09:00"
                                }"""))
                .andExpect(status().isConflict());
    }

    @Test
    void test_createAppointment_whenRequstBodyIsMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post("/v1/doctors/1/appointments"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
            "1,'',09:00",
            "1,2023-03-06,''",
            ",'',22:00",
            "1,2023-03-06,25:00",
            "1,202-03-06,25:00",
            "1,2023-13-06,25:00",
            "1,2023-12-32,25:00",
    })
    void test_createAppointment_whenRequestIsInvalid_shouldReturn400(
            Integer patientId,
            String date,
            String startTime
    ) throws Exception {
        String request = """
                {
                    "patientId": %d,
                    "date": %s,
                    "startTime": %s
                }""".formatted(patientId, date, startTime);

        mockMvc.perform(post("/v1/doctors/1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }


    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'Doctor Smith');
                        
            INSERT INTO patient (id, name)
            VALUES ( 1, 'Patient Doe');
                        
            INSERT INTO schedule (id, doctor_id, day_of_week, start_time, end_time)
            VALUES
            (1, 1, 'MONDAY', '09:00:00', '13:00:00')
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createAppointment_whenDoctorDoesNotWorkThatDay_shouldReturn409() throws Exception {
        mockMvc.perform(post("/v1/doctors/1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "patientId": 1,
                                    "date": "2023-03-07",
                                    "startTime": "09:00"
                                }"""))
                .andExpect(status().isConflict());
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'Doctor Smith');
                        
            INSERT INTO patient (id, name)
            VALUES ( 1, 'Patient Doe');
                        
            INSERT INTO schedule (id, doctor_id, day_of_week, start_time, end_time)
            VALUES
            (1, 1, 'MONDAY', '13:00:00', '22:00:00')
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createAppointment_whenDoctorDoesNotWorkThatHour_shouldReturn409() throws Exception {
        mockMvc.perform(post("/v1/doctors/1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "patientId": 1,
                                    "date": "2023-03-06",
                                    "startTime": "09:00"
                                }"""))
                .andExpect(status().isConflict());
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'Doctor Smith');
                        
            INSERT INTO patient (id, name)
            VALUES ( 1, 'Patient Doe');
                        
            INSERT INTO schedule (id, doctor_id, day_of_week, start_time, end_time)
            VALUES
            (1, 1, 'MONDAY', '13:00:00', '22:00:00')
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createAppointment_whenSlotIsNotBookable_shouldReturn409() throws Exception {
        mockMvc.perform(post("/v1/doctors/1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "patientId": 1,
                                    "date": "2023-03-06",
                                    "startTime": "13:15"
                                }"""))
                .andExpect(status().isConflict());
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'Doctor Smith');
                        
            INSERT INTO patient (id, name)
            VALUES ( 1, 'Patient Doe') , (2, 'Patient Two');
                        
            INSERT INTO schedule (id, doctor_id, day_of_week, start_time, end_time)
            VALUES
            (1, 1, 'MONDAY', '13:00:00', '22:00:00');
            
            INSERT INTO appointment (id, patient_id, doctor_id, date, start_time, end_time, created_at)
            VALUES
            (1, 2, 1, '2023-03-06', '14:00:00', '15:00:00', '2021-03-06 14:00:00');
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createAppointment_whenSlotIsAlreadyBooked_shouldReturn409() throws Exception {
        mockMvc.perform(post("/v1/doctors/1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "patientId": 1,
                                    "date": "2023-03-06",
                                    "startTime": "14:00"
                                }"""))
                .andExpect(status().isConflict());
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'Doctor Smith');
                        
            INSERT INTO patient (id, name)
            VALUES ( 1, 'Patient Doe'), (2, 'Patient Two');
                        
            INSERT INTO schedule (id, doctor_id, day_of_week, start_time, end_time)
            VALUES
            (1, 1, 'MONDAY', '09:00:00', '22:00:00');
            
            INSERT INTO appointment (patient_id, doctor_id, date, start_time, end_time, created_at)
            VALUES
            (2, 1, '2023-03-06', '10:00:00', '11:00:00', '2021-03-06 10:00:00');
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createAppointment_success() throws Exception {
        String request = """
                {
                    "patientId": 1,
                    "date": "2023-03-06",
                    "startTime": "20:00"
                }""";

        mockMvc.perform(post("/v1/doctors/1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.*", hasSize(7)))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.doctorId").value(1))
                .andExpect(jsonPath("$.date").value("2023-03-06"))
                .andExpect(jsonPath("$.startTime").value("20:00"))
                .andExpect(jsonPath("$.endTime").value("21:00"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertEquals(2, appointmentRepository.count());
        var appointments = appointmentRepository.findByDoctorIdAndDate(
                1,
                LocalDate.of(2023, 3, 6)
        ).stream().sorted(Comparator.comparing(AppointmentEntity::getDate)).toList();

        var firstAppointment = appointments.get(0);
        assertEquals(1, firstAppointment.getId());
        assertEquals(2, firstAppointment.getPatient().getId());
        assertEquals(1, firstAppointment.getDoctor().getId());
        assertEquals("2023-03-06", firstAppointment.getDate().toString());
        assertEquals("10:00", firstAppointment.getStartTime().toString());
        assertEquals("11:00", firstAppointment.getEndTime().toString());

        var secondAppointment = appointments.get(1);
        assertEquals(2, secondAppointment.getId());
        assertEquals(1, secondAppointment.getPatient().getId());
        assertEquals(1, secondAppointment.getDoctor().getId());
        assertEquals("2023-03-06", secondAppointment.getDate().toString());
        assertEquals("20:00", secondAppointment.getStartTime().toString());
        assertEquals("21:00", secondAppointment.getEndTime().toString());
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'Doctor Smith');
                        
            INSERT INTO patient (id, name)
            VALUES ( 1, 'Patient Doe');
                        
            INSERT INTO schedule (id, doctor_id, day_of_week, start_time, end_time)
            VALUES
            (1, 1, 'MONDAY', '09:00:00', '21:45:00')
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createAppointment_success_appointmentShouldNotExceedDoctorSchedule() throws Exception {
        String request = """
                {
                    "patientId": 1,
                    "date": "2023-03-06",
                    "startTime": "21:00"
                }""";

        mockMvc.perform(post("/v1/doctors/1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.*", hasSize(7)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.doctorId").value(1))
                .andExpect(jsonPath("$.date").value("2023-03-06"))
                .andExpect(jsonPath("$.startTime").value("21:00"))
                .andExpect(jsonPath("$.endTime").value("21:45"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertEquals(1, appointmentRepository.count());
        var appointments = appointmentRepository.findByDoctorIdAndDate(
                1,
                LocalDate.of(2023, 3, 6)
        );

        assertEquals(1, appointments.size());
        var appointment = appointments.get(0);
        assertEquals(1, appointment.getId());
        assertEquals(1, appointment.getPatient().getId());
        assertEquals(1, appointment.getDoctor().getId());
        assertEquals("2023-03-06", appointment.getDate().toString());
        assertEquals("21:00", appointment.getStartTime().toString());
        assertEquals("21:45", appointment.getEndTime().toString());
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'Doctor Smith');
                        
            INSERT INTO patient (id, name)
            VALUES ( 1, 'Patient Doe'), (2, 'Patient Two');
                        
            INSERT INTO schedule (id, doctor_id, day_of_week, start_time, end_time)
            VALUES
            (1, 1, 'MONDAY', '09:00:00', '22:00:00');
            
            INSERT INTO appointment (patient_id, doctor_id, date, start_time, end_time, created_at)
            VALUES
            (2, 1, '2023-03-06', '10:00:00', '11:00:00', '2021-03-06 10:00:00');
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createAppointment_success_shouldNotMatterIfTheDayIsInDifferentWeek() throws Exception {
        String request = """
                {
                    "patientId": 1,
                    "date": "2023-03-13",
                    "startTime": "10:00"
                }""";

        mockMvc.perform(post("/v1/doctors/1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.*", hasSize(7)))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.doctorId").value(1))
                .andExpect(jsonPath("$.date").value("2023-03-13"))
                .andExpect(jsonPath("$.startTime").value("10:00"))
                .andExpect(jsonPath("$.endTime").value("11:00"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertEquals(2, appointmentRepository.count());
        var appointments = appointmentRepository.findByDoctorIdAndDate(
                1,
                LocalDate.of(2023, 3, 13)
        );
        assertEquals(1, appointments.size());

        var firstAppointment = appointments.get(0);
        assertEquals(2, firstAppointment.getId());
        assertEquals(1, firstAppointment.getPatient().getId());
        assertEquals(1, firstAppointment.getDoctor().getId());
        assertEquals("2023-03-13", firstAppointment.getDate().toString());
        assertEquals("10:00", firstAppointment.getStartTime().toString());
        assertEquals("11:00", firstAppointment.getEndTime().toString());
    }
}