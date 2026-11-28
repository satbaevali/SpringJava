package com.example.sis2.catalog.infra;

import com.example.sis2.catalog.domain.ProductDomainEvent;
import com.example.sis2.common.config.KafkaMessagingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Component
public class ProductEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ProductEventConsumer fallbackLocalConsumer;
    private final List<PublishedRecord> publishedRecords = new CopyOnWriteArrayList<>();

    public ProductEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ProductEventConsumer fallbackLocalConsumer) {
        this.kafkaTemplate = kafkaTemplate;
        this.fallbackLocalConsumer = fallbackLocalConsumer;
    }

    public PublishedRecord publishDomainEvent(ProductDomainEvent event) {
        String key = event.aggregateKey();
        String payload = event.toJson();
        PublishedRecord record = new PublishedRecord(KafkaMessagingConfig.PRODUCT_EVENTS_TOPIC, key, event);
        publishedRecords.add(record);

        try {
            kafkaTemplate.send(KafkaMessagingConfig.PRODUCT_EVENTS_TOPIC, key, payload)
                    .get(1500, TimeUnit.MILLISECONDS);
            log.info("Kafka Producer отправил событие в топик [{}] с ключом агрегата key={}: eventId={}, type={}",
                    KafkaMessagingConfig.PRODUCT_EVENTS_TOPIC, key, event.eventId(), event.eventType());
        } catch (Exception ex) {
            log.warn("Брокер Kafka недоступен ({}), выполняется локальная доставка для демо: key={}",
                    ex.getMessage(), key);
            deliverLocallyWithDltFallback(payload, key);
        }

        return record;
    }

    private void deliverLocallyWithDltFallback(String payload, String key) {
        try {
            fallbackLocalConsumer.onProductDomainEvent(payload, key);
        } catch (Exception consumerEx) {
            fallbackLocalConsumer.onDeadLetterMessage(payload, key);
        }
    }

    public List<PublishedRecord> getPublishedRecords() {
        return List.copyOf(publishedRecords);
    }

    public void clearPublishedRecords() {
        publishedRecords.clear();
    }

    public record PublishedRecord(String topic, String key, ProductDomainEvent event) {
    }
}
