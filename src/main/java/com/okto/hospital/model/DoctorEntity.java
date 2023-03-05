package com.okto.hospital.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity(name = "doctor")
public class DoctorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<ScheduleEntity> scheduleEntityList;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<AppointmentEntity> appointmentEntityList;

    public DoctorEntity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScheduleEntity> getScheduleEntityList() {
        return scheduleEntityList;
    }

    public void setScheduleEntityList(List<ScheduleEntity> scheduleEntityList) {
        this.scheduleEntityList = scheduleEntityList;
    }

    public List<AppointmentEntity> getAppointmentEntityList() {
        return appointmentEntityList;
    }

    public void setAppointmentEntityList(List<AppointmentEntity> appointmentEntityList) {
        this.appointmentEntityList = appointmentEntityList;
    }
}
