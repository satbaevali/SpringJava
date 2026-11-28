package com.example.sis2.catalog.web.dto;

import com.example.sis2.catalog.domain.ProductDomainEvent;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Статус конвейера доменных событий Kafka (Producer, Idempotent Consumer и Dead-Letter Topic)")
public record EventPipelineStatusResponse(
        @Schema(description = "Основной топик доменных событий", example = "catalog.product-events")
        String mainTopic,

        @Schema(description = "Dead-Letter Topic (DLT) для отравленных сообщений", example = "catalog.product-events.DLT")
        String deadLetterTopic,

        @Schema(description = "Группа идемпотентного консьюмера", example = "catalog-audit-consumer-group")
        String consumerGroupId,

        @Schema(description = "Количество отправленных событий", example = "3")
        int publishedCount,

        @Schema(description = "Количество успешно обработанных уникальных событий", example = "2")
        int processedUniqueCount,

        @Schema(description = "Количество пропущенных дубликатов (идемпотентность)", example = "1")
        int duplicateIgnoredCount,

        @Schema(description = "Количество отравленных сообщений (Poison Messages), попавших в DLT", example = "1")
        int deadLetterCount,

        @Schema(description = "Список успешно обработанных доменных событий")
        List<ProductDomainEvent> processedEvents,

        @Schema(description = "Список отравленных сообщений, перехваченных в Dead-Letter Topic")
        List<String> deadLetterMessages
) {
}
