package org.pogonin.shortlinkservice.api.controller;

import lombok.RequiredArgsConstructor;
import org.pogonin.shortlinkservice.api.dto.in.LinkRequest;
import org.pogonin.shortlinkservice.api.dto.out.LinkStatisticResponse;
import org.pogonin.shortlinkservice.api.swager.IShortLinkController;
import org.pogonin.shortlinkservice.core.service.ShortLinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ShortLinkController implements IShortLinkController {
    private final ShortLinkService shortLinkService;

    @GetMapping("/{shortLink}")
    @Override
    public ResponseEntity<Void> redirectByShortLink(@PathVariable String shortLink) {
        String originalLink = shortLinkService.getOriginalLink(shortLink);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalLink))
                .build();
    }

    @PostMapping("/generate-link")
    @Override
    public ResponseEntity<String> generateLink(@Validated @RequestBody LinkRequest linkRequest) {
        String shortLink = shortLinkService.generateShortLink(linkRequest);
        return new ResponseEntity<>(shortLink, HttpStatus.CREATED);
    }

    @PutMapping("change-link")
    @Override
    public ResponseEntity<String> changeShortLink(@Validated @RequestBody LinkRequest linkRequest) {
        String shortLink = shortLinkService.changeShortLink(linkRequest);
        return new ResponseEntity<>(shortLink, HttpStatus.ACCEPTED);
    }

    @GetMapping("/statistic/{shortLink}")
    @Override
    public LinkStatisticResponse statistic(@PathVariable String shortLink) {
        return shortLinkService.getLinkStatistic(shortLink);
    }
}
