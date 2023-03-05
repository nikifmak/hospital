package com.okto.hospital.controller;

import com.okto.hospital.model.ScheduleEntity;
import com.okto.hospital.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScheduleControllerTest {

    public static final String CLEAN_TABLES_QUERY = """
            DELETE FROM schedule;
            DELETE FROM doctor;
            """;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    void test_getSchedule_whenDoctorDoesNotExist_shouldReturn404() throws Exception {
        mockMvc.perform(get("/v1/doctors/1/schedule"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'John Smith');

            INSERT INTO schedule (id, doctor_id, day_of_week, start_time, end_time)
            VALUES
            (1, 1, 'MONDAY', '09:00:00', '13:00:00'),
            (2, 1, 'TUESDAY', '12:00:00', '17:00:00'),
            (4, 1, 'THURSDAY', '17:00:00', '22:00:00'),
            (5, 1, 'FRIDAY', '09:00:00', '17:00:00');
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_getSchedule_whenDoctorExists_shouldReturn200_withWorkingHoursAndDays() throws Exception {
        mockMvc.perform(get("/v1/doctors/1/schedule"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {
                            "id": 1,
                            "doctorId": 1,
                            "dayOfWeek": "MONDAY",
                            "startTime": "09:00:00",
                            "endTime": "13:00:00"
                          },
                          {
                            "id": 2,
                            "doctorId": 1,
                            "dayOfWeek": "TUESDAY",
                            "startTime": "12:00:00",
                            "endTime": "17:00:00"
                          },
                          {
                            "id": 4,
                            "doctorId": 1,
                            "dayOfWeek": "THURSDAY",
                            "startTime": "17:00:00",
                            "endTime": "22:00:00"
                          },
                          {
                            "id": 5,
                            "doctorId": 1,
                            "dayOfWeek": "FRIDAY",
                            "startTime": "09:00:00",
                            "endTime": "17:00:00"
                          }
                        ]
                        """));
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'John Smith');
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createSchedule_whenEndTimeIsBeforeStartTime_shouldReturn400() throws Exception {
        String request = """
                {
                    "doctorId": 1,
                    "dayOfWeek": "MONDAY",
                    "startTime": "09:00",
                    "endTime":  "08:00"
                }""";
        mockMvc.perform(post("/v1/doctors/1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'John Smith');
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createSchedule_whenDoctorIdIsMissing_shouldReturn400() throws Exception {
        String request = """
                {
                    "dayOfWeek": "monday",
                    "startTime": "09:00",
                    "endTime":  "10:00"
                }""";
        mockMvc.perform(post("/v1/doctors/1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
            "'', 10:00",
            "09:00,''",
            "'',''"
    })
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'John Smith');
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createSchedule_whenStartOrEndTimeIsMissing_shouldReturn400(String startTime, String endTime) throws Exception {
        String request = """
                {
                    "doctorId": 1,
                    "dayOfWeek": "monday",
                    "startTime": %s,
                    "endTime":  %s
                }""".formatted(startTime, endTime);
        mockMvc.perform(post("/v1/doctors/1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'John Smith');
            
            INSERT INTO schedule (id, doctor_id, day_of_week, start_time, end_time)
            VALUES
            (1, 1, 'MONDAY', '09:00:00', '13:00:00');
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createAppointment_whenScheduleForDayAlreadyExists_thenCannotCreateSameDayAndReturn409() throws Exception {
        String request = """
                {
                    "doctorId": 1,
                    "dayOfWeek": "MONDAY",
                    "startTime": "09:00",
                    "endTime":  "21:00"
                }""";

        mockMvc.perform(post("/v1/doctors/1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'John Smith');
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_createAppointment_success() throws Exception {
        String request = """
                {
                    "doctorId": 1,
                    "dayOfWeek": "MONDAY",
                    "startTime": "09:00",
                    "endTime":  "21:00"
                }""";

        mockMvc.perform(post("/v1/doctors/1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                            "id": 1,
                            "doctorId": 1,
                            "dayOfWeek": "MONDAY",
                            "startTime": "09:00:00",
                            "endTime": "21:00:00"
                        }
                        """));

        assertEquals(1, scheduleRepository.count());
        ScheduleEntity scheduleEntity = scheduleRepository.findAll().get(0);
        assertEquals(1, scheduleEntity.getId());
        assertEquals(1, scheduleEntity.getDoctor().getId());
        assertEquals(DayOfWeek.MONDAY, scheduleEntity.getDayOfWeek());
        assertEquals(LocalTime.of(9, 0), scheduleEntity.getStartTime());
        assertEquals(LocalTime.of(21, 0), scheduleEntity.getEndTime());
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'John Smith');
            
            INSERT INTO schedule (id, doctor_id, day_of_week, start_time, end_time)
            VALUES
            (1, 1, 'MONDAY', '09:00:00', '13:00:00');
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_updateAppointment_success() throws Exception {
        String request = """
                {
                    "doctorId": 1,
                    "dayOfWeek": "MONDAY",
                    "startTime": "09:00",
                    "endTime":  "21:00"
                }""";

        mockMvc.perform(put("/v1/doctors/1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNoContent());

        assertEquals(1, scheduleRepository.count());
        ScheduleEntity scheduleEntity = scheduleRepository.findAll().get(0);
        assertEquals(1, scheduleEntity.getId());
        assertEquals(1, scheduleEntity.getDoctor().getId());
        assertEquals(DayOfWeek.MONDAY, scheduleEntity.getDayOfWeek());
        assertEquals(LocalTime.of(9, 0), scheduleEntity.getStartTime());
        assertEquals(LocalTime.of(21, 0), scheduleEntity.getEndTime());
    }

    @Test
    @Sql(statements = """
            INSERT INTO doctor (id, name)
            VALUES (1, 'John Smith');
            """,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAN_TABLES_QUERY,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_updateAppointment_whenWeTryWithScheduleDayThatDontExists_shouldReturnNotFound() throws Exception {
        String request = """
                {
                    "doctorId": 1,
                    "dayOfWeek": "MONDAY",
                    "startTime": "09:00",
                    "endTime":  "21:00"
                }""";

        mockMvc.perform(put("/v1/doctors/1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

}