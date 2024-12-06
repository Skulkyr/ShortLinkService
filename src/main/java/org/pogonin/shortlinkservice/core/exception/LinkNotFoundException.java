package org.pogonin.shortlinkservice.core.exception;

import lombok.Getter;

@Getter
public class LinkNotFoundException extends RuntimeException {
    private final String link;
    public LinkNotFoundException(String link) {
        super("Link not found: " + link);
        this.link = link;
    }
}
