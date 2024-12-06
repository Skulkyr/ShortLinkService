package org.pogonin.shortlinkservice.core.service;

import org.pogonin.shortlinkservice.api.dto.in.LinkRequest;
import org.pogonin.shortlinkservice.api.dto.out.LinkStatisticResponse;

public interface ShortLinkService {
    String getOriginalLink(String shortLink);
    String generateShortLink(LinkRequest linkRequest);
    String changeShortLink(LinkRequest linkRequest);
    LinkStatisticResponse getLinkStatistic(String shortLink);
}
