package com.okto.hospital.controller;

import com.okto.hospital.model.request.ScheduleRequest;
import com.okto.hospital.model.response.Schedule;
import com.okto.hospital.service.schedule.ScheduleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1")
@Validated
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Get the schedule of a doctor
     *
     * @param doctorId The doctor's id
     * @return The schedule of the doctor
     */
    @GetMapping("/doctors/{doctorId}/schedule")
    public List<Schedule> getSchedule(@PathVariable Integer doctorId) {
        return scheduleService.getScheduleByDoctorId(doctorId);
    }

    /**
     * Create a schedule for a doctor
     *
     * @param doctorId The doctor's id
     * @param schedule The schedule request
     * @return The created schedule
     */
    @PostMapping("/doctors/{doctorId}/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    public Schedule createSchedule(
            @PathVariable Integer doctorId,
            @Valid @RequestBody ScheduleRequest schedule
    ) {
        return scheduleService.createSchedule(
                doctorId,
                schedule.dayOfWeek(),
                schedule.startTime(),
                schedule.endTime()
        );
    }

    /**
     * Update a schedule for a doctor
     *
     * @param doctorId The doctor's id
     * @param schedule The schedule request
     * @return The updated schedule
     */
    @PutMapping("/doctors/{doctorId}/schedule")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Schedule updateSchedule(
            @PathVariable Integer doctorId,
            @Valid @RequestBody ScheduleRequest schedule
    ) {
        return scheduleService.updateSchedule(
                doctorId,
                schedule.dayOfWeek(),
                schedule.startTime(),
                schedule.endTime()
        );
    }

    /**
     * Upsert a list of schedules for a doctor
     *
     * @param doctorId The doctor's id
     * @param scheduleRequestList The list of schedule requests
     * @return The list of schedules
     */
    @PutMapping("/doctors/{doctorId}/schedule/list")
    public List<Schedule> upsertScheduleList(
            @PathVariable Integer doctorId,
            @RequestBody @NotEmpty List<@Valid @NotNull ScheduleRequest> scheduleRequestList
    ) {
        return scheduleService.upsertScheduleList(doctorId, scheduleRequestList);
    }
}
