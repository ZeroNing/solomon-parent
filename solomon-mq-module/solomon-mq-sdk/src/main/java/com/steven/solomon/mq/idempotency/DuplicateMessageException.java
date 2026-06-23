package com.steven.solomon.mq.idempotency;

public class DuplicateMessageException extends RuntimeException {

  public DuplicateMessageException(String messageKey) {
    super("Duplicate message: " + messageKey);
  }
}
