package com.okto.hospital.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ScheduleRequest(
        @NotNull
        Integer doctorId,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        DayOfWeek dayOfWeek,
        @NotNull
        LocalTime startTime,
        @NotNull
        LocalTime endTime
) {
    @AssertTrue(message = "End time must be after start time")
    private boolean isValidEndTime() {
        return endTime != null && endTime.isAfter(startTime);
    }
}
