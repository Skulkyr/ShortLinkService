package org.pogonin.shortlinkservice.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pogonin.shortlinkservice.api.dto.in.LinkRequest;
import org.pogonin.shortlinkservice.api.dto.out.LinkStatisticResponse;
import org.pogonin.shortlinkservice.core.service.ShortLinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {
    private final ShortLinkService shortLinkService;

    @GetMapping("/{shortLink}")
    public ResponseEntity<Void> redirectByShortLink(@PathVariable String shortLink) {
        String originalLink = shortLinkService.getOriginalLink(shortLink);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalLink))
                .build();
    }

    @PostMapping("/generate-link")
    public ResponseEntity<String> generateLink(@Valid @RequestBody LinkRequest linkRequest) {
        String shortLink = shortLinkService.generateShortLink(linkRequest);
        return new ResponseEntity<>(shortLink, HttpStatus.CREATED);
    }

    @PutMapping("change-link")
    public ResponseEntity<String> changeShortLink(@Valid @RequestBody LinkRequest linkRequest) {
        String shortLink = shortLinkService.changeShortLink(linkRequest);
        return new ResponseEntity<>(shortLink, HttpStatus.ACCEPTED);
    }

    @GetMapping("/statistic/{shortLink}")
    public ResponseEntity<LinkStatisticResponse> statistic(@PathVariable String shortLink) {
        return ResponseEntity.ok(shortLinkService.getLinkStatistic(shortLink));
    }
}
