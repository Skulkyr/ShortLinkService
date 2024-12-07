package org.pogonin.shortlinkservice.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.pogonin.shortlinkservice.db.repository.LinkRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class LinkCleanerService {
    private final LinkRepository linkRepository;

    @Transactional
    @Scheduled(cron = "${spring.application.setting.cron-clean-up}")
    protected void clean() {
        int deletedCount = linkRepository.deleteAllExpired(LocalDateTime.now());
        log.info("Deleted {} links", deletedCount);
    }
}
