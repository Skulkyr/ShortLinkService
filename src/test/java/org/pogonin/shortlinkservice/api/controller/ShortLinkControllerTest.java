package org.pogonin.shortlinkservice.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pogonin.shortlinkservice.api.dto.in.LinkRequest;
import org.pogonin.shortlinkservice.api.dto.out.LinkStatisticResponse;
import org.pogonin.shortlinkservice.core.service.ShortLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortLinkController.class)
public class ShortLinkControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    @SuppressWarnings("all")
    private ShortLinkService shortLinkService;
    @Autowired
    private ObjectMapper objectMapper;
    private LinkRequest linkRequest;

    @BeforeEach
    public void setup() {
        linkRequest = new LinkRequest();
        linkRequest.setLink("https://original-link.com");
    }

    @Test
    public void testRedirectByShortLink() throws Exception {
        String shortLink = "abc123";
        String originalLink = "https://original-link.com";
        when(shortLinkService.getOriginalLink(shortLink)).thenReturn(originalLink);


        mockMvc.perform(get("/{shortLink}", shortLink))


                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalLink));
        verify(shortLinkService, times(1)).getOriginalLink(shortLink);
    }

    @Test
    public void testGenerateLink() throws Exception {
        String shortLink = "short123";
        when(shortLinkService.generateShortLink(any(LinkRequest.class))).thenReturn(shortLink);


        mockMvc.perform(post("/generate-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkRequest)))


                .andExpect(status().isCreated())
                .andExpect(content().string(shortLink));
        ArgumentCaptor<LinkRequest> linkRequestCaptor = ArgumentCaptor.forClass(LinkRequest.class);
        verify(shortLinkService, times(1)).generateShortLink(linkRequestCaptor.capture());
        LinkRequest capturedRequest = linkRequestCaptor.getValue();
        assertThat(capturedRequest.getLink()).isEqualTo(linkRequest.getLink());
        assertThat(capturedRequest.getLength()).isEqualTo(10);
        assertThat(capturedRequest.getRollingExpiration()).isFalse();
        assertThat(capturedRequest.getTtl()).isEqualTo(Duration.ofDays(7));
    }

    @Test
    public void testChangeShortLink() throws Exception {
        String newShortLink = "newShort123";
        when(shortLinkService.changeShortLink(any(LinkRequest.class))).thenReturn(newShortLink);


        mockMvc.perform(put("/change-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkRequest)))


                .andExpect(status().isAccepted())
                .andExpect(content().string(newShortLink));
        ArgumentCaptor<LinkRequest> linkRequestCaptor = ArgumentCaptor.forClass(LinkRequest.class);
        verify(shortLinkService, times(1)).changeShortLink(linkRequestCaptor.capture());
        LinkRequest capturedRequest = linkRequestCaptor.getValue();
        assertThat(capturedRequest.getLink()).isEqualTo(linkRequest.getLink());
        assertThat(capturedRequest.getLength()).isEqualTo(10);
        assertThat(capturedRequest.getRollingExpiration()).isFalse();
        assertThat(capturedRequest.getTtl()).isEqualTo(Duration.ofDays(7));
    }

    @Test
    public void testStatistic() throws Exception {
        String shortLink = "stat123";
        LinkStatisticResponse statisticResponse = new LinkStatisticResponse() {
            @Override
            public Integer getRank() {return 2;}
            @Override
            public Long getNumberOfUses() {return 13L;}
        };
        when(shortLinkService.getLinkStatistic(shortLink)).thenReturn(statisticResponse);


        mockMvc.perform(get("/statistic/{shortLink}", shortLink))


                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.rank").value(statisticResponse.getRank()))
                .andExpect(jsonPath("$.numberOfUses").value(statisticResponse.getNumberOfUses()));
        verify(shortLinkService, times(1)).getLinkStatistic(shortLink);
    }
}
