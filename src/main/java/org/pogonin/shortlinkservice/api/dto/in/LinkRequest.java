package org.pogonin.shortlinkservice.api.dto.in;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Duration;

@Data
@Schema(name = "Запрос на создание ссылки",
        description = """
                Запрос конфигурирует способ создания, а так же настройки времени жизни ссылки.
                Создать ссылку можно используя alias для конкретного указания необходимого конечного адреса.
                Если не указывать это поле, будет выполнена генерация случайно ссылки установленной длинны(length).
                Поддерживается удаление по времени относительно времени создания или же последнего доступа(флаг rollingExpiration),
                а так же по количеству использований ссылки.
                
                Дефолтные настройки установлены на создание ссылки случайной ссылки длинной в 10 символов
                и временем жизни в 7 дней с момента создания.
                """)
public class LinkRequest {

    @NotNull(message = "link не может быть null")
    @Schema(description = "Ссылка которую необходимо сократить", example = "https://google.com")
    private String link;

    @NotNull(message = "length не может быть null")
    @Min(value = 4, message = "Длинна не может быть меньше 4")
    @Max(value = 20, message = "Длинна не может быть больше 20")
    @Schema(description = "Длинна генерируемой ссылки", defaultValue = "10")
    private int length = 10;

    @Size(min = 4, max = 20, message = "Алиас должен содержать от 4 до 20 знаков")
    @Schema(description = "Алиас для конкретного указания конечной ссылки", example = "lena_golovach")
    private String alias;

    @NotNull(message = "rollingExpiration не может быть null")
    @Schema(description = "Флаг установки обновления ttl относительно времени последнего доступа", defaultValue = "false")
    private Boolean rollingExpiration = false;

    @Schema(description = "Время жизни ссылки. Подробнее в [документации по ISO 8601](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-).",
            defaultValue = "PT7D", example = "P2DT12H31M40S", type = "string")
    private Duration ttl = Duration.ofDays(7);

    @Schema(description = "Лимит количества использований ссылки, по умолчанию выключен")
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
