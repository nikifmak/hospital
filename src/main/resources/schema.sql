CREATE TABLE doctor
(
    id   INT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE patient
(
    id   INT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE appointment
(
    id         INT PRIMARY KEY,
    patient_id INT,
    doctor_id  INT,
    date       DATE,
    start_time TIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctor (id) ON DELETE CASCADE
);

CREATE TABLE schedule
(
    id          INT PRIMARY KEY,
    doctor_id   INT,
    day_of_week VARCHAR(9),
    start_time  TIME,
    end_time    TIME,
    FOREIGN KEY (doctor_id) REFERENCES doctor (id) ON DELETE CASCADE,
    CONSTRAINT unique_doctor_day UNIQUE (doctor_id, day_of_week)
);