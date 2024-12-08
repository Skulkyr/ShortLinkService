package org.pogonin.shortlinkservice.api.swager.general;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Система сокращения ссылок",
                description = "Сервис сокращения ссылок и редиректа по ним, с возможностью гибкого задания ttl",
                version = "0.3",
                contact = @Contact(
                        name = "Алексей Погонин",
                        email = "skulkyr20@gmaill.com"
                )
        )
)
public class OpenApiConfig {
}
