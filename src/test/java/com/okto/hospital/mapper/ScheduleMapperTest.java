package com.okto.hospital.mapper;

import com.okto.hospital.model.DoctorEntity;
import com.okto.hospital.model.ScheduleEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScheduleMapperTest {

    ScheduleMapper scheduleMapper = new ScheduleMapperImpl();

    @Test
    void testToSchedule() {
        DoctorEntity doctor = mock(DoctorEntity.class);
        when(doctor.getId()).thenReturn(1);
        ScheduleEntity input = mock(ScheduleEntity.class);
        when(input.getId()).thenReturn(1);
        when(input.getDoctor()).thenReturn(doctor);
        when(input.getDayOfWeek()).thenReturn(DayOfWeek.MONDAY);
        when(input.getStartTime()).thenReturn(LocalTime.MIN);
        when(input.getEndTime()).thenReturn(LocalTime.MAX);

        var output = scheduleMapper.toSchedule(input);
        assertNotNull(output);
        assertEquals(1, output.id());
        assertEquals(1, output.doctorId());
        assertEquals(DayOfWeek.MONDAY, output.dayOfWeek());
        assertEquals(LocalTime.MIN, output.startTime());
        assertEquals(LocalTime.MAX, output.endTime());
    }

}