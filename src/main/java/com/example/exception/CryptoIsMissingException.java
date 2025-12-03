package com.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NO_CONTENT)
public class CryptoIsMissingException extends RuntimeException {
  public CryptoIsMissingException(String message) {
    super(message);
  }
}
