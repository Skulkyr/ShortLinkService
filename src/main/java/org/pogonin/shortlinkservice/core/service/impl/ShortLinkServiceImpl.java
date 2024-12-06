package org.pogonin.shortlinkservice.core.service.impl;

import org.pogonin.shortlinkservice.api.dto.in.LinkRequest;
import org.pogonin.shortlinkservice.api.dto.out.LinkStatisticResponse;
import org.pogonin.shortlinkservice.core.entity.Link;
import org.pogonin.shortlinkservice.core.exception.AliasAlreadyExistException;
import org.pogonin.shortlinkservice.core.exception.LinkAlreadyExistException;
import org.pogonin.shortlinkservice.core.exception.LinkGenerateException;
import org.pogonin.shortlinkservice.core.exception.LinkNotFoundException;
import org.pogonin.shortlinkservice.core.repository.LinkRepository;
import org.pogonin.shortlinkservice.core.service.ShortLinkService;
import org.pogonin.shortlinkservice.core.utils.CompressUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ShortLinkServiceImpl implements ShortLinkService {
    private final int MAX_ATTEMPT;
    private final String BASE_HOST;
    private final CompressUtils compressUtils;
    private final LinkRepository linkRepository;

    public ShortLinkServiceImpl(LinkRepository linkRepository,
                                CompressUtils compressUtils,
                                @Value("${spring.application.setting.base-host}") String BASE_HOST,
                                @Value("${spring.application.setting.max-compress-attempt}") int MAX_ATTEMPT) {
        this.BASE_HOST = BASE_HOST;
        this.MAX_ATTEMPT = MAX_ATTEMPT;
        this.compressUtils = compressUtils;
        this.linkRepository = linkRepository;
    }

    @Override
    @Transactional()
    public String getOriginalLink(String shortLink) {
        Link link = linkRepository.findByShortLink(shortLink).orElseThrow(
                () -> new LinkNotFoundException(shortLink));
        if (link.getUsageLimit() != null && link.getUsageLimit() <= link.getNumberOfUses())
            linkRepository.delete(link);
        return link.getOriginalLink();
    }

    @Override
    @Transactional
    public String generateShortLink(LinkRequest linkRequest) {
        throwIfOriginalLinkExist(linkRequest);

        Link link = new Link();
        link.setOriginalLink(linkRequest.getLink());
        link.setNumberOfUses(0L);
        fillLinkExpiration(linkRequest, link);

        if (tryGenerateShortLink(linkRequest, link))
            return saveAndReturnShortLink(link);

        throw new LinkGenerateException("Unable to generate unique link", linkRequest);
    }


    @Override
    @Transactional
    public String changeShortLink(LinkRequest linkRequest) {
        String originalLink = linkRequest.getLink();
        Link link = linkRepository.findById(originalLink).orElseThrow(
                () -> new LinkNotFoundException(originalLink));
        fillLinkExpiration(linkRequest, link);

        if (tryGenerateShortLink(linkRequest, link))
            return saveAndReturnShortLink(link);

        throw new LinkGenerateException("Unable to generate unique link", linkRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public LinkStatisticResponse getLinkStatistic(String shortLink) {
        return linkRepository.getStatisticByLink(shortLink).orElseThrow(() -> new LinkNotFoundException(shortLink));
    }

    private boolean tryCompress(LinkRequest linkRequest, Link link) {
        for (int attempt = 0; attempt < MAX_ATTEMPT; attempt++) {
            String shortLink = compressUtils.compress(linkRequest.getLink(), linkRequest.getLength());
            if (isExistByShortLink(shortLink))
                continue;
            link.setShortLink(shortLink);
            return true;
        }
        return false;
    }

    private boolean tryGenerateShortLink(LinkRequest linkRequest, Link link) {
        if (linkRequest.getAlias() != null) {
            if (isExistByShortLink(linkRequest.getAlias()))
                throw new AliasAlreadyExistException("Alias " + linkRequest.getAlias() + " already exists!");
            link.setShortLink(linkRequest.getAlias());
            return true;

        } else return tryCompress(linkRequest, link);
    }

    private boolean isExistByShortLink(String src) {
        return linkRepository.existsByShortLink(src);
    }

    private void throwIfOriginalLinkExist(LinkRequest linkRequest) {
        if (linkRepository.existsByOriginalLink(linkRequest.getLink())) {
            @SuppressWarnings("all")
            String shortLink = linkRepository.findById(linkRequest.getLink()).get().getShortLink();
            throw new LinkAlreadyExistException("Link already exist", shortLink);
        }
    }

    private void fillLinkExpiration(LinkRequest linkRequest, Link link) {
        Duration ttl = linkRequest.getTtl();
        link.setTtl(ttl);
        link.setUsageLimit(linkRequest.getUsageLimit());
        link.setExpirationTime(ttl == null ? null : LocalDateTime.now().plus(ttl));
        link.setRollingExpiration(linkRequest.getRollingExpiration());
    }

    private String saveAndReturnShortLink(Link link) {
        return BASE_HOST + linkRepository.save(link).getShortLink();
    }
}
