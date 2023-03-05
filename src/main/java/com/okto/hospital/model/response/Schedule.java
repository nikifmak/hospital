package com.okto.hospital.model.response;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record Schedule(
        Integer id,
        Integer doctorId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}
