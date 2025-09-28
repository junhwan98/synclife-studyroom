package com.synclife.studyroom.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
@OpenAPIDefinition(tags = {
        @Tag(name = "Rooms", description = "회의실 등록 / 가용성 조회"),
        @Tag(name = "Reservations", description = "예약 생성/취소")
})
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("Studyroom API").version("v1"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("Opaque")));
    }

}
