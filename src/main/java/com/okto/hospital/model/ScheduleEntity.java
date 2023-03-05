package com.okto.hospital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "schedule", uniqueConstraints = {@UniqueConstraint(columnNames = {"doctor_id", "day_of_week"})})
public class ScheduleEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "doctor_id")
        private DoctorEntity doctor;

        @Column(name = "day_of_week")
        @Enumerated(EnumType.STRING)
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;

        public ScheduleEntity() {
        }

        public ScheduleEntity(DoctorEntity doctor, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
                this.doctor = doctor;
                this.dayOfWeek = dayOfWeek;
                this.startTime = startTime;
                this.endTime = endTime;
        }

        public Integer getId() {
                return id;
        }

        public void setId(Integer id) {
                this.id = id;
        }

        public DoctorEntity getDoctor() {
                return doctor;
        }

        public void setDoctor(DoctorEntity doctor) {
                this.doctor = doctor;
        }

        public DayOfWeek getDayOfWeek() {
                return dayOfWeek;
        }

        public void setDayOfWeek(DayOfWeek dayOfWeek) {
                this.dayOfWeek = dayOfWeek;
        }

        public LocalTime getStartTime() {
                return startTime;
        }

        public void setStartTime(LocalTime startTime) {
                this.startTime = startTime;
        }

        public LocalTime getEndTime() {
                return endTime;
        }

        public void setEndTime(LocalTime endTime) {
                this.endTime = endTime;
        }
}
