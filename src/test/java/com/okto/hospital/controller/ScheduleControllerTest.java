package com.okto.hospital.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    @Sql(statements = """
            DELETE FROM schedule;
            DELETE FROM doctor;
            """,
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_getSchedule_whenDoctorExists_shouldReturn200_withWorkingHoursAndDays() throws Exception {
        mockMvc.perform(get("/v1/doctors/1/schedule"))
                .andDo(print())
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

}