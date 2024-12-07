package org.pogonin.shortlinkservice.api.swager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.pogonin.shortlinkservice.api.dto.in.LinkRequest;
import org.pogonin.shortlinkservice.api.dto.out.LinkStatisticResponse;
import org.pogonin.shortlinkservice.api.swager.general.ApiResponse400ForCreate;
import org.pogonin.shortlinkservice.core.handler.GlobalExceptionHandler;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "Контроллер работы с ссылками",
        description = "Позволяет создавать, изменять, получать, а так же просматривать рейтинг коротких ссылок на ресурсы"
)
@SuppressWarnings("all")
public interface IShortLinkController {

    @Operation(
            summary = "Перенаправление",
            description = "Перенаправляет по короткой ссылке на другой url",
            responses = {
                    @ApiResponse(
                            responseCode = "302",
                            description = "Перенаправление на целевой URL"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Ссылка не найдена",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                                    examples = @ExampleObject(
                                            """
                                                    {
                                                      "timestamp": "2024-12-07T16:54:39.025Z",
                                                      "statusCode": 404,
                                                      "error": "Ссылка с указанным идентификатором не найдена."
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<Void> redirectByShortLink(String shortLink);

    @ApiResponse400ForCreate
    @Operation(
            summary = "Создание короткой ссылки",
            description = "Создает и возвращает короткую ссылку на целевой URL",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Создано",
                            content = @Content(
                                    mediaType = "text",
                                    examples = @ExampleObject("http://this_resource.com/ObAODui1")
                            )
                    )
            }
    )
    ResponseEntity<String> generateLink(LinkRequest linkRequest);

    @ApiResponse400ForCreate
    @Operation(
            summary = "Изменение существующей короткой ссылки",
            description = "Создает и возвращает короткую ссылку для уже существующего URL",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Изменено",
                            content = @Content(
                                    mediaType = "text",
                                    examples = @ExampleObject("http://this_resource.com/ObAODui1")
                            )
                    )
            }
    )
    ResponseEntity<String> changeShortLink(LinkRequest linkRequest);


    @Operation(
            summary = "Получить статистику",
            description = "Возвращает количество использований и ранг ссылки",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "ok",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = LinkStatisticResponse.class),
                                    examples = @ExampleObject(
                                            """
                                                    {
                                                      "numberOfUses": 2061,
                                                      "rank": 13
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Ссылка не найдена",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                                    examples = @ExampleObject(
                                            """
                                                    {
                                                      "timestamp": "2024-12-07T16:54:39.025Z",
                                                      "statusCode": 404,
                                                      "error": "Ссылка с указанным идентификатором не найдена."
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    LinkStatisticResponse statistic(String shortLink);
}
