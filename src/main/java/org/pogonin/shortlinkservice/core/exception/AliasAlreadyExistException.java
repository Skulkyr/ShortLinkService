package org.pogonin.shortlinkservice.core.exception;

public class AliasAlreadyExistException extends RuntimeException {
    public AliasAlreadyExistException(String message) {
        super(message);
    }
}
