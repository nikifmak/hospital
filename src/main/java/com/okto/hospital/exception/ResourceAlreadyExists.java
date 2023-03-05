package com.okto.hospital.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.CONFLICT, reason = "Resource already exists")
public class ResourceAlreadyExists extends RuntimeException{
}
