package org.pogonin.shortlinkservice.api.dto.in;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Duration;

@Data
public class LinkRequest {
    @NotNull(message = "link не может быть null")
    private String link;
    @NotNull(message = "length не может быть null")
    @Min(value = 4, message = "Длинна не может быть меньше 4")
    @Max(value = 20, message = "Длинна не может быть больше 20")
    private int length = 10;
    @Size(min = 4, max = 20, message = "Алиас должен содержать от 4 до 20 знаков")
    private String alias;
    @NotNull(message = "rollingExpiration не может быть null")
    private Boolean rollingExpiration = false;

    private Duration ttl = Duration.ofDays(7);
    private Integer usageLimit;

    @AssertTrue(message = "TTL не может превышать 2-х месяцев")
    @JsonIgnore
    @SuppressWarnings("all")
    private boolean isTtlValid() {
        return ttl == null || ttl.getSeconds() <= Duration.ofDays(60).getSeconds();
    }

    @AssertFalse(message = "Не возможно создать бессмертную ссылку")
    @JsonIgnore
    @SuppressWarnings("all")
    private boolean hasCondition() {
        return ttl == null && usageLimit == null;
    }

}
