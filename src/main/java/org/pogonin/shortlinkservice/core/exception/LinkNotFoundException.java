package org.pogonin.shortlinkservice.core.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String message) {
        super(message);
    }
}
