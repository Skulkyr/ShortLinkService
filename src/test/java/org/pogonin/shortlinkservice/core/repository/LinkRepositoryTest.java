package org.pogonin.shortlinkservice.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pogonin.shortlinkservice.api.dto.out.LinkStatisticResponse;
import org.pogonin.shortlinkservice.core.entity.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@DataJpaTest
class LinkRepositoryTest {

    @Autowired
    private LinkRepository linkRepository;

    @PersistenceContext
    private EntityManager entityManager;


    private Link createLink(String originalLink, String shortLink, Long numberOfUses,
                            LocalDateTime expirationTime, Duration ttl, Boolean rollingExpiration) {
        Link link = new Link();
        link.setOriginalLink(originalLink);
        link.setShortLink(shortLink);
        link.setNumberOfUses(numberOfUses);
        link.setExpirationTime(expirationTime);
        link.setTtl(ttl);
        link.setRollingExpiration(rollingExpiration);
        return link;
    }

    @Test
    @DisplayName("Сохранение и поиск Link по shortLink")
    void testSaveAndFindByShortLink() {
        Link link = createLink("https://original.com", "short123", 0L,
                LocalDateTime.now().plusDays(1), Duration.ofHours(1), true);


        linkRepository.save(link);
        Optional<Link> foundLinkOpt = linkRepository.findByShortLink("short123");


        assertThat(foundLinkOpt).isPresent();
        Link foundLink = foundLinkOpt.get();
        assertThat(foundLink.getOriginalLink()).isEqualTo("https://original.com");
        assertThat(foundLink.getShortLink()).isEqualTo("short123");
        assertThat(foundLink.getNumberOfUses()).isEqualTo(0L);
        assertThat(foundLink.getLastAccess()).isNull();
    }

    @Test
    @DisplayName("Поиск Link по shortLink, когда записи нет")
    void testFindByShortLink_NotFound() {
        Optional<Link> foundLinkOpt = linkRepository.findByShortLink("nonexistent");


        assertThat(foundLinkOpt).isNotPresent();
    }

    @Test
    @DisplayName("Проверка метода existsByShortLink")
    void testExistsByShortLink() {
        Link link = createLink("https://original.com", "short123", 0L,
                null, null, false);


        linkRepository.save(link);


        assertThat(linkRepository.existsByShortLink("short123")).isTrue();
        assertThat(linkRepository.existsByShortLink("unknown")).isFalse();
    }

    @Test
    @DisplayName("Проверка метода existsByOriginalLink")
    void testExistsByOriginalLink() {
        Link link = createLink("https://original.com", "short123", 0L,
                null, null, false);


        linkRepository.save(link);


        assertThat(linkRepository.existsByOriginalLink("https://original.com")).isTrue();
        assertThat(linkRepository.existsByOriginalLink("https://unknown.com")).isFalse();
    }

    @Test

    @DisplayName("Получение статистики по shortLink")
    void testGetStatisticByLink() {
        Link link1 = createLink("https://original1.com", "short1", 10L,
                null, null, false);
        Link link2 = createLink("https://original2.com", "short2", 20L,
                null, null, false);
        Link link3 = createLink("https://original3.com", "short3", 5L,
                null, null, false);
        linkRepository.save(link1);
        linkRepository.save(link2);
        linkRepository.save(link3);


        entityManager.flush();
        entityManager.clear();
        Optional<LinkStatisticResponse> statsOpt = linkRepository.getStatisticByLink("short1");


        assertThat(statsOpt).isPresent();
        LinkStatisticResponse stats = statsOpt.get();
        assertThat(stats.getNumberOfUses()).isEqualTo(10L);
        assertThat(stats.getRank()).isEqualTo(2);
    }

    @Test
    @DisplayName("Удаление всех просроченных ссылок")
    void testDeleteAllExpired() {
        LocalDateTime now = LocalDateTime.now();
        Link expiredLink1 = createLink("https://expired1.com", "exp1", 0L,
                now.minusDays(2), null, false);
        Link expiredLink2 = createLink("https://expired2.com", "exp2", 0L,
                now.minusHours(5), null, false);
        Link validLink = createLink("https://valid.com", "val1", 0L,
                now.plusDays(1), null, false);


        linkRepository.save(expiredLink1);
        linkRepository.save(expiredLink2);
        linkRepository.save(validLink);
        int deletedCount = linkRepository.deleteAllExpired(now);


        assertThat(deletedCount).isEqualTo(2);
        assertThat(linkRepository.existsByShortLink("exp1")).isFalse();
        assertThat(linkRepository.existsByShortLink("exp2")).isFalse();
        assertThat(linkRepository.existsByShortLink("val1")).isTrue();
    }

    @Test
    @DisplayName("Удаление всех просроченных ссылок без наличия")
    void testDeleteAllExpired_NoExpired() {

        LocalDateTime now = LocalDateTime.now();
        Link validLink1 = createLink("https://valid1.com", "val1", 0L,
                now.plusDays(1), null, false);
        Link validLink2 = createLink("https://valid2.com", "val2", 0L,
                now.plusHours(5), null, false);


        linkRepository.save(validLink1);
        linkRepository.save(validLink2);
        int deletedCount = linkRepository.deleteAllExpired(now);


        assertThat(deletedCount).isEqualTo(0);
        assertThat(linkRepository.existsByShortLink("val1")).isTrue();
        assertThat(linkRepository.existsByShortLink("val2")).isTrue();
    }

    @Test
    @DisplayName("Проверка уникальности originalLink и shortLink")
    void testUniqueConstraints() {
        Link original = createLink("https://original.com", "short123", 0L,
                null, null, false);
        Link duplicateShort = createLink("https://neworiginal.com", "short123", 0L,
                null, null, false);
        Link duplicateOriginal = createLink("https://original.com", "short456", 0L,
                null, null, false);


        linkRepository.save(original);


        assertThatThrownBy(() -> linkRepository.saveAndFlush(duplicateOriginal))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> linkRepository.saveAndFlush(duplicateShort))
                .isInstanceOf(DataIntegrityViolationException.class);
    }


    @Test
    @DisplayName("Проверка автоматической установки creationDate")
    void testCreationTimestamp() {

        Link link = createLink("https://original.com", "short123", 0L,
                null, null, false);


        linkRepository.save(link);
        Optional<Link> savedLinkOpt = linkRepository.findByShortLink("short123");


        assertThat(savedLinkOpt).isPresent();
        Link savedLink = savedLinkOpt.get();
        assertThat(savedLink.getCreationDate()).isNotNull();
    }

    @Test
    @DisplayName("Проверка автоматического увеличения numberOfUses и обновления lastAccess")
    void testPostLoadBehavior() {

        Link link = createLink("https://original.com", "short123", 0L,
                null, Duration.ofHours(2), true);


        linkRepository.save(link);
        Optional<Link> foundLinkOpt = linkRepository.findByShortLink("short123");


        assertThat(foundLinkOpt).isPresent();
        Link foundLink = foundLinkOpt.get();
        assertThat(foundLink.getNumberOfUses()).isEqualTo(0L);
        assertThat(foundLink.getLastAccess()).isNull();
    }
}

