package org.pogonin.shortlinkservice.core.repository;

import org.pogonin.shortlinkservice.api.dto.out.LinkStatisticResponse;
import org.pogonin.shortlinkservice.core.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, String> {

    Optional<Link> findByShortLink(String shortLink);

    @Query("""
            select main.numberOfUses AS numberOfUses,
                        (select COUNT(*) + 1
                         from Link l
                         where l.numberOfUses > main.numberOfUses) AS rank
            from Link main
            where main.shortLink = ?1
            """)
    Optional<LinkStatisticResponse> getStatisticByLink(String shortLink);

    boolean existsByShortLink(String shortLink);

    boolean existsByOriginalLink(String originalLink);


    @Modifying
    @Query("update Link l set l.numberOfUses = l.numberOfUses + 1 where l.shortLink = ?1")
    void updateNumberOfUsesByShortLink(@NonNull String shortLink);
}