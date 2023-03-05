package com.okto.hospital.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Doctor is not available on this day")
public class DoctorScheduleAvailabilityException extends RuntimeException{
}
