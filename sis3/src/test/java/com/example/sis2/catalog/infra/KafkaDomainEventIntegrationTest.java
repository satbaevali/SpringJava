package com.example.sis2.catalog.infra;

import com.example.sis2.catalog.domain.Money;
import com.example.sis2.catalog.domain.Product;
import com.example.sis2.catalog.domain.ProductDomainEvent;
import com.example.sis2.common.config.KafkaMessagingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {
        KafkaMessagingConfig.class,
        ProductEventProducer.class,
        ProductEventConsumer.class
})
@EmbeddedKafka(
        partitions = 3,
        topics = {
                KafkaMessagingConfig.PRODUCT_EVENTS_TOPIC,
                KafkaMessagingConfig.PRODUCT_EVENTS_DLT_TOPIC
        }
)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "app.kafka.auto-startup=true"
})
@DirtiesContext
class KafkaDomainEventIntegrationTest {

    @Autowired
    private ProductEventProducer eventProducer;

    @Autowired
    private ProductEventConsumer eventConsumer;

    @BeforeEach
    void setUp() {
        eventProducer.clearPublishedRecords();
        eventConsumer.resetState();
    }

    @Test
    void producerPublishesWithAggregateKeyAndConsumerIsIdempotent() throws InterruptedException {
        Product product = new Product(42L, "MacBook Pro 16", Money.of("2499.99"), 8, 1L, "Электроника", 0L);
        ProductDomainEvent event = ProductDomainEvent.stockReduced(product);

        ProductEventProducer.PublishedRecord published = eventProducer.publishDomainEvent(event);
        assertThat(published.key()).isEqualTo("42");
        assertThat(published.topic()).isEqualTo(KafkaMessagingConfig.PRODUCT_EVENTS_TOPIC);

        eventProducer.publishDomainEvent(event);

        waitForCondition(() -> eventConsumer.isEventProcessed(event.eventId())
                && eventConsumer.getDuplicateIgnoredCount() >= 1);

        assertThat(eventConsumer.getProcessedEvents()).hasSize(1);
        assertThat(eventConsumer.getProcessedEvents().getFirst().productId()).isEqualTo(42L);
        assertThat(eventConsumer.getDuplicateIgnoredCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void poisonMessageIsRoutedToDeadLetterTopic() throws InterruptedException {
        ProductDomainEvent poisonEvent = ProductDomainEvent.poisonEvent(999L);

        ProductEventProducer.PublishedRecord published = eventProducer.publishDomainEvent(poisonEvent);
        assertThat(published.key()).isEqualTo("999");

        waitForCondition(() -> !eventConsumer.getDeadLetterPayloads().isEmpty());

        assertThat(eventConsumer.getProcessedEvents()).isEmpty();
        assertThat(eventConsumer.getDeadLetterPayloads()).isNotEmpty();
        assertThat(eventConsumer.getDeadLetterPayloads().getFirst()).contains("POISON_EVENT");
    }

    private void waitForCondition(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(50);
        }
    }
}
