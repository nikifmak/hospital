package com.okto.hospital.mapper;

import com.okto.hospital.model.ScheduleEntity;
import com.okto.hospital.model.response.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "doctorId", source = "scheduleEntity.doctor.id")
    Schedule toSchedule(ScheduleEntity scheduleEntity);
}
