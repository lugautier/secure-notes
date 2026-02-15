package com.securenotes.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends RuntimeException {

  private final String email;

  public EmailAlreadyExistsException(String email) {
    super("Email already registered: " + email);
    this.email = email;
  }
}
