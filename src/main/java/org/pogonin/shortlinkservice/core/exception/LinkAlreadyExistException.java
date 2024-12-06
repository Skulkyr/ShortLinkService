package org.pogonin.shortlinkservice.core.exception;

import lombok.Getter;

@Getter
public class LinkAlreadyExistException extends RuntimeException {
    private final String shortLink;

    public LinkAlreadyExistException(String message, String shortLink) {
        super(message);
        this.shortLink = shortLink;
    }
}
