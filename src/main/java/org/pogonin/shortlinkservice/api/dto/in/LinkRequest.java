package org.pogonin.shortlinkservice.api.dto.in;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LinkRequest {
    @NotNull
    private String link;
    private int length;
    private String alias;
}
