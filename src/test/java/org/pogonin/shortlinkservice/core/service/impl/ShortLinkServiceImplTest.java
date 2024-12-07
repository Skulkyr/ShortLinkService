package org.pogonin.shortlinkservice.core.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pogonin.shortlinkservice.api.dto.in.LinkRequest;
import org.pogonin.shortlinkservice.api.dto.out.LinkStatisticResponse;
import org.pogonin.shortlinkservice.db.entity.Link;
import org.pogonin.shortlinkservice.core.exception.AliasAlreadyExistException;
import org.pogonin.shortlinkservice.core.exception.LinkAlreadyExistException;
import org.pogonin.shortlinkservice.core.exception.LinkGenerateException;
import org.pogonin.shortlinkservice.core.exception.LinkNotFoundException;
import org.pogonin.shortlinkservice.db.repository.LinkRepository;
import org.pogonin.shortlinkservice.core.utils.CompressUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShortLinkServiceImplTest {

    @Mock
    private LinkRepository linkRepository;
    @Mock
    private CompressUtils compressUtils;
    private ShortLinkServiceImpl shortLinkService;
    private final String BASE_HOST = "https://short.ly/";
    private final int MAX_ATTEMPT = 5;

    @BeforeEach
    void setUp() {
        shortLinkService = new ShortLinkServiceImpl(linkRepository, compressUtils, BASE_HOST, MAX_ATTEMPT);
    }


    @Nested
    class GetOriginalLinkTests {

        @Test
        void getOriginalLink_WhenLinkExists_ShouldReturnOriginalLink() {
            String shortLink = "abc123";
            String originalLink = "https://example.com";
            Link link = new Link();
            link.setShortLink(shortLink);
            link.setOriginalLink(originalLink);
            link.setUsageLimit(null);
            link.setNumberOfUses(0L);
            when(linkRepository.findByShortLink(shortLink)).thenReturn(Optional.of(link));


            String result = shortLinkService.getOriginalLink(shortLink);


            assertThat(result).isEqualTo(originalLink);
            verify(linkRepository, times(1)).findByShortLink(shortLink);
            verify(linkRepository, never()).delete(any(Link.class));
        }

        @Test
        void getOriginalLink_WhenLinkNotFound_ShouldThrowLinkNotFoundException() {
            String shortLink = "nonexistent";
            when(linkRepository.findByShortLink(shortLink)).thenReturn(Optional.empty());


            assertThatThrownBy(() -> shortLinkService.getOriginalLink(shortLink))


                    .isInstanceOf(LinkNotFoundException.class)
                    .hasMessageContaining(shortLink);
            verify(linkRepository, times(1)).findByShortLink(shortLink);
            verify(linkRepository, never()).delete(any(Link.class));
        }

        @Test
        void getOriginalLink_WhenUsageLimitExceeded_ShouldDeleteLink() {
            String shortLink = "limit123";
            String originalLink = "https://example.com/limit";
            Link link = new Link();
            link.setShortLink(shortLink);
            link.setOriginalLink(originalLink);
            link.setUsageLimit(5);
            link.setNumberOfUses(5L);
            when(linkRepository.findByShortLink(shortLink)).thenReturn(Optional.of(link));


            String result = shortLinkService.getOriginalLink(shortLink);


            assertThat(result).isEqualTo(originalLink);
            verify(linkRepository, times(1)).findByShortLink(shortLink);
            verify(linkRepository, times(1)).delete(link);
        }
    }


    @Nested
    class GenerateShortLinkTests {

        @Test
        void generateShortLink_WhenOriginalLinkAlreadyExists_ShouldThrowLinkAlreadyExistException() {
            LinkRequest linkRequest = new LinkRequest();
            linkRequest.setLink("https://example.com/exist");
            linkRequest.setLength(6);
            when(linkRepository.existsByOriginalLink(linkRequest.getLink())).thenReturn(true);
            when(linkRepository.findById(linkRequest.getLink())).thenReturn(Optional.of(new Link()));


            assertThatThrownBy(() -> shortLinkService.generateShortLink(linkRequest))
                    .isInstanceOf(LinkAlreadyExistException.class)
                    .hasMessageContaining("Link already exist");


            verify(linkRepository, times(1)).existsByOriginalLink(linkRequest.getLink());
            verify(linkRepository, times(1)).findById(linkRequest.getLink());
            verify(linkRepository, never()).save(any(Link.class));
        }

        @Test
        void generateShortLink_WithAlias_WhenAliasDoesNotExist_ShouldSaveAndReturnAlias() {
            LinkRequest linkRequest = new LinkRequest();
            linkRequest.setLink("https://example.com/new");
            linkRequest.setAlias("custom123");
            linkRequest.setLength(6); // Не используется, так как alias задан
            when(linkRepository.existsByOriginalLink(linkRequest.getLink())).thenReturn(false);
            when(linkRepository.existsByShortLink(linkRequest.getAlias())).thenReturn(false);
            ArgumentCaptor<Link> linkCaptor = ArgumentCaptor.forClass(Link.class);
            when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));


            String result = shortLinkService.generateShortLink(linkRequest);


            assertThat(result).isEqualTo(BASE_HOST + linkRequest.getAlias());
            verify(linkRepository, times(1)).existsByOriginalLink(linkRequest.getLink());
            verify(linkRepository, times(1)).existsByShortLink(linkRequest.getAlias());
            verify(linkRepository, times(1)).save(linkCaptor.capture());
            Link savedLink = linkCaptor.getValue();
            assertThat(savedLink.getOriginalLink()).isEqualTo(linkRequest.getLink());
            assertThat(savedLink.getShortLink()).isEqualTo(linkRequest.getAlias());
            assertThat(savedLink.getNumberOfUses()).isEqualTo(0L);
            assertThat(savedLink.getUsageLimit()).isNull();
        }

        @Test
        void generateShortLink_WithAlias_WhenAliasExists_ShouldThrowAliasAlreadyExistException() {
            LinkRequest linkRequest = new LinkRequest();
            linkRequest.setLink("https://example.com/new");
            linkRequest.setAlias("custom123");
            linkRequest.setLength(6);
            when(linkRepository.existsByOriginalLink(linkRequest.getLink())).thenReturn(false);
            when(linkRepository.existsByShortLink(linkRequest.getAlias())).thenReturn(true);


            assertThatThrownBy(() -> shortLinkService.generateShortLink(linkRequest))


                    .isInstanceOf(AliasAlreadyExistException.class)
                    .hasMessageContaining("Alias " + linkRequest.getAlias() + " already exists!");
            verify(linkRepository, times(1)).existsByOriginalLink(linkRequest.getLink());
            verify(linkRepository, times(1)).existsByShortLink(linkRequest.getAlias());
            verify(linkRepository, never()).save(any(Link.class));
        }

        @Test
        void generateShortLink_WithoutAlias_WhenCompressSucceeds_ShouldSaveAndReturnShortLink() {
            LinkRequest linkRequest = new LinkRequest();
            linkRequest.setLink("https://example.com/new");
            linkRequest.setLength(6);
            when(linkRepository.existsByOriginalLink(linkRequest.getLink())).thenReturn(false);
            when(compressUtils.compress(linkRequest.getLink(), linkRequest.getLength()))
                    .thenReturn("short1")
                    .thenReturn("short2");
            when(linkRepository.existsByShortLink("short1")).thenReturn(true);
            when(linkRepository.existsByShortLink("short2")).thenReturn(false);
            ArgumentCaptor<Link> linkCaptor = ArgumentCaptor.forClass(Link.class);
            when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));


            String result = shortLinkService.generateShortLink(linkRequest);


            assertThat(result).isEqualTo(BASE_HOST + "short2");
            verify(compressUtils, times(2)).compress(linkRequest.getLink(), linkRequest.getLength());
            verify(linkRepository, times(2)).existsByShortLink(anyString());
            verify(linkRepository, times(1)).save(linkCaptor.capture());
            Link savedLink = linkCaptor.getValue();
            assertThat(savedLink.getOriginalLink()).isEqualTo(linkRequest.getLink());
            assertThat(savedLink.getShortLink()).isEqualTo("short2");
            assertThat(savedLink.getNumberOfUses()).isEqualTo(0L);
            assertThat(savedLink.getUsageLimit()).isNull();
        }

        @Test
        void generateShortLink_WithoutAlias_WhenCompressFails_ShouldThrowLinkGenerateException() {
            LinkRequest linkRequest = new LinkRequest();
            linkRequest.setLink("https://example.com/new");
            linkRequest.setLength(6);
            when(linkRepository.existsByOriginalLink(linkRequest.getLink())).thenReturn(false);
            when(compressUtils.compress(linkRequest.getLink(), linkRequest.getLength()))
                        .thenReturn("short");
            when(linkRepository.existsByShortLink("short")).thenReturn(true);



            assertThatThrownBy(() -> shortLinkService.generateShortLink(linkRequest))


                    .isInstanceOf(LinkGenerateException.class)
                    .hasMessageContaining("Unable to generate unique link");
            verify(compressUtils, times(MAX_ATTEMPT)).compress(linkRequest.getLink(), linkRequest.getLength());
            verify(linkRepository, times(MAX_ATTEMPT)).existsByShortLink(anyString());
            verify(linkRepository, never()).save(any(Link.class));
        }
    }


    @Nested
    class ChangeShortLinkTests {

        @Test
        void changeShortLink_WhenLinkExistsAndAliasNotSet_ShouldCompressAndSaveNewShortLink() {
            LinkRequest linkRequest = new LinkRequest();
            linkRequest.setLink("https://example.com/exist");
            linkRequest.setLength(6);
            linkRequest.setAlias(null);
            Link existingLink = new Link();
            existingLink.setOriginalLink(linkRequest.getLink());
            existingLink.setShortLink("oldShort");
            existingLink.setNumberOfUses(10L);
            existingLink.setUsageLimit(null);
            when(linkRepository.findById(linkRequest.getLink())).thenReturn(Optional.of(existingLink));
            when(compressUtils.compress(linkRequest.getLink(), linkRequest.getLength()))
                    .thenReturn("newShort");
            when(linkRepository.existsByShortLink("newShort")).thenReturn(false);
            ArgumentCaptor<Link> linkCaptor = ArgumentCaptor.forClass(Link.class);
            when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));


            String result = shortLinkService.changeShortLink(linkRequest);


            assertThat(result).isEqualTo(BASE_HOST + "newShort");
            verify(linkRepository, times(1)).findById(linkRequest.getLink());
            verify(compressUtils, times(1)).compress(linkRequest.getLink(), linkRequest.getLength());
            verify(linkRepository, times(1)).existsByShortLink("newShort");
            verify(linkRepository, times(1)).save(linkCaptor.capture());
            Link savedLink = linkCaptor.getValue();
            assertThat(savedLink.getShortLink()).isEqualTo("newShort");
            assertThat(savedLink.getNumberOfUses()).isEqualTo(10L);
        }

        @Test
        void changeShortLink_WhenLinkNotFound_ShouldThrowLinkNotFoundException() {
            LinkRequest linkRequest = new LinkRequest();
            linkRequest.setLink("https://example.com/nonexistent");
            linkRequest.setLength(6);
            linkRequest.setAlias(null);
            when(linkRepository.findById(linkRequest.getLink())).thenReturn(Optional.empty());


            assertThatThrownBy(() -> shortLinkService.changeShortLink(linkRequest))
                    .isInstanceOf(LinkNotFoundException.class)
                    .hasMessageContaining(linkRequest.getLink());

            verify(linkRepository, times(1)).findById(linkRequest.getLink());
            verify(compressUtils, never()).compress(anyString(), anyInt());
            verify(linkRepository, never()).save(any(Link.class));
        }

        @Test
        void changeShortLink_WithAlias_WhenAliasDoesNotExist_ShouldSaveAndReturnAlias() {
            LinkRequest linkRequest = new LinkRequest();
            linkRequest.setLink("https://example.com/exist");
            linkRequest.setAlias("newAlias");
            linkRequest.setLength(6);
            Link existingLink = new Link();
            existingLink.setOriginalLink(linkRequest.getLink());
            existingLink.setShortLink("oldShort");
            existingLink.setNumberOfUses(10L);
            existingLink.setUsageLimit(null);
            when(linkRepository.findById(linkRequest.getLink())).thenReturn(Optional.of(existingLink));
            when(linkRepository.existsByShortLink(linkRequest.getAlias())).thenReturn(false);
            ArgumentCaptor<Link> linkCaptor = ArgumentCaptor.forClass(Link.class);
            when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));

            
            String result = shortLinkService.changeShortLink(linkRequest);

            
            assertThat(result).isEqualTo(BASE_HOST + linkRequest.getAlias());
            verify(linkRepository, times(1)).findById(linkRequest.getLink());
            verify(linkRepository, times(1)).existsByShortLink(linkRequest.getAlias());
            verify(linkRepository, times(1)).save(linkCaptor.capture());

            Link savedLink = linkCaptor.getValue();
            assertThat(savedLink.getShortLink()).isEqualTo(linkRequest.getAlias());
            assertThat(savedLink.getNumberOfUses()).isEqualTo(10L);
        }

        @Test
        void changeShortLink_WithAlias_WhenAliasExists_ShouldThrowAliasAlreadyExistException() {
            LinkRequest linkRequest = new LinkRequest();
            linkRequest.setLink("https://example.com/exist");
            linkRequest.setAlias("existingAlias");
            linkRequest.setLength(6);
            Link existingLink = new Link();
            existingLink.setOriginalLink(linkRequest.getLink());
            existingLink.setShortLink("oldShort");
            existingLink.setNumberOfUses(10L);
            existingLink.setUsageLimit(null);
            when(linkRepository.findById(linkRequest.getLink())).thenReturn(Optional.of(existingLink));
            when(linkRepository.existsByShortLink(linkRequest.getAlias())).thenReturn(true);

            
            assertThatThrownBy(() -> shortLinkService.changeShortLink(linkRequest))
                    
                    
                    .isInstanceOf(AliasAlreadyExistException.class)
                    .hasMessageContaining("Alias " + linkRequest.getAlias() + " already exists!");
            verify(linkRepository, times(1)).findById(linkRequest.getLink());
            verify(linkRepository, times(1)).existsByShortLink(linkRequest.getAlias());
            verify(linkRepository, never()).save(any(Link.class));
        }
    }

    
    @Nested
    class GetLinkStatisticTests {

        @Test
        void getLinkStatistic_WhenLinkExists_ShouldReturnStatistics() {
            String shortLink = "stat123";
            LinkStatisticResponse statistic = new LinkStatisticResponse() {
                @Override
                public Integer getRank() {return 1;}
                @Override
                public Long getNumberOfUses() {return 12L;}
            };
            when(linkRepository.findStatisticByLink(shortLink)).thenReturn(Optional.of(statistic));

            
            LinkStatisticResponse result = shortLinkService.getLinkStatistic(shortLink);

            
            assertThat(result).isEqualTo(statistic);
            verify(linkRepository, times(1)).findStatisticByLink(shortLink);
        }

        @Test
        void getLinkStatistic_WhenLinkNotFound_ShouldThrowLinkNotFoundException() {
            String shortLink = "nonexistent";
            when(linkRepository.findStatisticByLink(shortLink)).thenReturn(Optional.empty());

            
            assertThatThrownBy(() -> shortLinkService.getLinkStatistic(shortLink))
                    .isInstanceOf(LinkNotFoundException.class)
                    .hasMessageContaining(shortLink);
            verify(linkRepository, times(1)).findStatisticByLink(shortLink);
        }
    }
}
