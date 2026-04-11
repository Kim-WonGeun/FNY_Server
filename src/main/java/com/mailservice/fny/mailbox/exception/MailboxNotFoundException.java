package com.mailservice.fny.mailbox.exception;

public class MailboxNotFoundException extends RuntimeException {

    public MailboxNotFoundException(String message) {
        super(message);
    }
}
