package com.okto.hospital.controller;

import com.okto.hospital.model.request.ScheduleRequest;
import com.okto.hospital.model.response.Schedule;
import com.okto.hospital.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/doctors/{doctorId}/schedule")
    public List<Schedule> getSchedule(@PathVariable Integer doctorId) {
        return scheduleService.getScheduleByDoctorId(doctorId);
    }

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


}
