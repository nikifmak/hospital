package com.okto.hospital.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity(name = "patient")
public class PatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<AppointmentEntity> appointmentEntityList;

    public PatientEntity() {

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

    public List<AppointmentEntity> getAppointmentEntityList() {
        return appointmentEntityList;
    }

    public void setAppointmentEntityList(List<AppointmentEntity> appointmentEntityList) {
        this.appointmentEntityList = appointmentEntityList;
    }
}
