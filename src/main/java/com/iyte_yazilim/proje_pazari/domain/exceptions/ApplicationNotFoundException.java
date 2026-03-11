package com.iyte_yazilim.proje_pazari.domain.exceptions;

public class ApplicationNotFoundException extends RuntimeException {
  public ApplicationNotFoundException(String applicationId) {
    super("Application not found: " + applicationId);
  }
}
