package org.pogonin.shortlinkservice.api.swager.general;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.pogonin.shortlinkservice.core.handler.GlobalExceptionHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "400",
        description = "Ошибка валидации входных данных",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                examples = {
                        @ExampleObject(
                                name = "Ошибка входных данных",
                                value =
                                        """
                                                {
                                                  "timestamp": "2024-12-07T16:54:39.025Z",
                                                  "statusCode": 400,
                                                  "error": "Невозможно сгенерировать короткую ссылку с указанными параметрами"
                                                }
                                                """
                        ),
                        @ExampleObject(
                                name = "Данный алиас уже занят",
                                value =
                                        """
                                                {
                                                  "timestamp": "2024-12-07T16:54:39.025Z",
                                                  "statusCode": 400,
                                                  "error": "Выбранное имя уже занято, выберите другое"
                                                }
                                                """
                        ),
                        @ExampleObject(
                                name = "Уже существует",
                                value =
                                        """
                                                {
                                                  "timestamp": "2024-12-07T16:54:39.025Z",
                                                  "statusCode": 400,
                                                  "error": "Короткая ссылка для данного ресурса уже существует: http://this_resource.com/ObAODui1"
                                                }
                                                """
                        )
                }
        )
)
public @interface ApiResponse400ForCreate {
}
