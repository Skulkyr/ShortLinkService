package org.pogonin.shortlinkservice.db.repository;

import jakarta.persistence.LockModeType;
import org.pogonin.shortlinkservice.api.dto.out.LinkStatisticResponse;
import org.pogonin.shortlinkservice.db.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Link> findByShortLink(String shortLink);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Link> findByOriginalLink(String originalLink);

    @Query("""
            select main.numberOfUses AS numberOfUses,
                        (select COUNT(*) + 1
                         from Link l
                         where l.numberOfUses > main.numberOfUses) AS rank
            from Link main
            where main.shortLink = ?1
            """)
    Optional<LinkStatisticResponse> findStatisticByLink(String shortLink);

    boolean existsByShortLink(String shortLink);

    boolean existsByOriginalLink(String originalLink);

    @Modifying
    @Query("DELETE FROM Link l WHERE l.expirationTime IS NOT NULL AND l.expirationTime < :currentTime")
    int deleteAllExpired(LocalDateTime currentTime);
}