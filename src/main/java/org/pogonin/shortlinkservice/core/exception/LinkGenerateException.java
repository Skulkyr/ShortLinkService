package org.pogonin.shortlinkservice.core.exception;

import lombok.Getter;
import org.pogonin.shortlinkservice.api.dto.in.LinkRequest;

@Getter
public class LinkGenerateException extends RuntimeException {
  private final LinkRequest linkRequest;

  public LinkGenerateException(String message, LinkRequest linkRequest) {
    super(message);
    this.linkRequest = linkRequest;
  }
}
