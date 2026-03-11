package com.iyte_yazilim.proje_pazari.domain.exceptions;

public class FlaggedContentNotFoundException extends RuntimeException {
  public FlaggedContentNotFoundException(String flagId) {
    super("Flagged content not found: " + flagId);
  }
}
