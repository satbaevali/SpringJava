package com.example.sis2.catalog.web;

import com.example.sis2.catalog.app.ProductService;
import com.example.sis2.catalog.domain.Product;
import com.example.sis2.catalog.domain.ProductDomainEvent;
import com.example.sis2.catalog.infra.ProductEventConsumer;
import com.example.sis2.catalog.infra.ProductEventProducer;
import com.example.sis2.catalog.web.dto.EventPipelineStatusResponse;
import com.example.sis2.common.config.KafkaMessagingConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/events", produces = "application/json;charset=UTF-8")
@Tag(name = "Kafka Domain Events & DLT", description = "Публикация доменных событий (key = aggregateId), идемпотентный консьюмер и демо Poison Message -> Dead-Letter Topic")
public class EventDemoController {

    private final ProductService productService;
    private final ProductEventProducer eventProducer;
    private final ProductEventConsumer eventConsumer;

    public EventDemoController(
            ProductService productService,
            ProductEventProducer eventProducer,
            ProductEventConsumer eventConsumer) {
        this.productService = productService;
        this.eventProducer = eventProducer;
        this.eventConsumer = eventConsumer;
    }

    @PostMapping("/publish/{productId}")
    @Operation(
            summary = "Опубликовать доменное событие товара в Kafka (key = aggregate id)",
            description = "Создаёт и отправляет ProductDomainEvent в топик catalog.product-events с ключом сообщения равным ID агрегата (productId)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Событие отправлено и обработано консьюмером",
                    content = @Content(schema = @Schema(implementation = EventPipelineStatusResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар с указанным ID не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public EventPipelineStatusResponse publishProductEvent(
            @Parameter(description = "ID товара (используется как Kafka message key)", example = "1")
            @PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        ProductDomainEvent event = ProductDomainEvent.stockReduced(product);
        eventProducer.publishDomainEvent(event);
        return buildStatus();
    }

    @PostMapping("/demo-idempotency/{productId}")
    @Operation(
            summary = "Демо идемпотентности консьюмера (отправка дубликата события)",
            description = "Отправляет одно и то же событие с одинаковым eventId дважды подряд. Идемпотентный консьюмер обрабатывает его только один раз и пропускает дубликат."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Дубликат успешно отфильтрован идемпотентным консьюмером",
                    content = @Content(schema = @Schema(implementation = EventPipelineStatusResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар с указанным ID не найден",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public EventPipelineStatusResponse demonstrateIdempotency(
            @Parameter(description = "ID товара", example = "1")
            @PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        ProductDomainEvent duplicateEvent = ProductDomainEvent.productCreated(product);
        eventProducer.publishDomainEvent(duplicateEvent);
        eventProducer.publishDomainEvent(duplicateEvent);
        return buildStatus();
    }

    @PostMapping("/poison-demo/{productId}")
    @Operation(
            summary = "Демо Poison Message -> Dead-Letter Topic (catalog.product-events.DLT)",
            description = "Отправляет отравленное сообщение (POISON_EVENT с отрицательным остатком -999) с ключом productId. Основной консьюмер выбрасывает ошибку, и после retry сообщение попадает в Dead-Letter Topic."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Отравленное сообщение перенаправлено в Dead-Letter Topic",
                    content = @Content(schema = @Schema(implementation = EventPipelineStatusResponse.class))
            )
    })
    public EventPipelineStatusResponse sendPoisonMessage(
            @Parameter(description = "ID агрегата для ключа сообщения", example = "999")
            @PathVariable Long productId) {
        ProductDomainEvent poison = ProductDomainEvent.poisonEvent(productId);
        eventProducer.publishDomainEvent(poison);
        return buildStatus();
    }

    @GetMapping("/status")
    @Operation(
            summary = "Получить текущий статус событий Kafka, идемпотентного консьюмера и DLT",
            description = "Показывает количество отправленных сообщений, обработанных уникальных событий, отброшенных дубликатов и сообщений в Dead-Letter Topic."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Текущая статистика событий и DLT",
                    content = @Content(schema = @Schema(implementation = EventPipelineStatusResponse.class))
            )
    })
    public EventPipelineStatusResponse getStatus() {
        return buildStatus();
    }

    private EventPipelineStatusResponse buildStatus() {
        return new EventPipelineStatusResponse(
                KafkaMessagingConfig.PRODUCT_EVENTS_TOPIC,
                KafkaMessagingConfig.PRODUCT_EVENTS_DLT_TOPIC,
                KafkaMessagingConfig.CONSUMER_GROUP_ID,
                eventProducer.getPublishedRecords().size(),
                eventConsumer.getProcessedEvents().size(),
                eventConsumer.getDuplicateIgnoredCount(),
                eventConsumer.getDeadLetterPayloads().size(),
                eventConsumer.getProcessedEvents(),
                eventConsumer.getDeadLetterPayloads()
        );
    }
}
