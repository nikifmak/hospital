package com.okto.hospital.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Slot is not bookable")
public class SlotNotBookable extends RuntimeException{
}
