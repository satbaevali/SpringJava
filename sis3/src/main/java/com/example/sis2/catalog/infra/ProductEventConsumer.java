package com.example.sis2.catalog.infra;

import com.example.sis2.catalog.domain.ProductDomainEvent;
import com.example.sis2.common.config.KafkaMessagingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();
    private final List<ProductDomainEvent> processedEvents = new CopyOnWriteArrayList<>();
    private final List<String> deadLetterPayloads = new CopyOnWriteArrayList<>();
    private final AtomicInteger duplicateIgnoredCount = new AtomicInteger(0);

    @KafkaListener(
            topics = KafkaMessagingConfig.PRODUCT_EVENTS_TOPIC,
            groupId = KafkaMessagingConfig.CONSUMER_GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onProductDomainEvent(
            @Payload String payload,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String aggregateKey) {

        ProductDomainEvent event = ProductDomainEvent.fromJson(payload);

        if ("POISON_EVENT".equals(event.eventType()) || event.remainingStock() < 0) {
            log.error("Обнаружено отравленное сообщение (Poison Message) для ключа {}: eventId={}, type={}",
                    aggregateKey, event.eventId(), event.eventType());
            throw new IllegalArgumentException("Poison message detected for aggregateId=" + event.productId());
        }

        if (!processedEventIds.add(event.eventId())) {
            int duplicates = duplicateIgnoredCount.incrementAndGet();
            log.info("Идемпотентный консьюмер [{}]: дубликат события eventId={} (key={}) пропущен. Всего дубликатов: {}",
                    KafkaMessagingConfig.CONSUMER_GROUP_ID, event.eventId(), aggregateKey, duplicates);
            return;
        }

        processedEvents.add(event);
        log.info("Консьюмер [{}] успешно обработал событие: type={}, key={}, product={}, stock={}",
                KafkaMessagingConfig.CONSUMER_GROUP_ID,
                event.eventType(),
                aggregateKey,
                event.productName(),
                event.remainingStock());
    }

    @KafkaListener(
            topics = KafkaMessagingConfig.PRODUCT_EVENTS_DLT_TOPIC,
            groupId = KafkaMessagingConfig.DLT_CONSUMER_GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDeadLetterMessage(
            @Payload String payload,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String aggregateKey) {
        deadLetterPayloads.add(payload);
        log.warn("Dead-Letter Topic [{}] принял отравленное сообщение с ключом key={}: {}",
                KafkaMessagingConfig.PRODUCT_EVENTS_DLT_TOPIC, aggregateKey, payload);
    }

    public boolean isEventProcessed(String eventId) {
        return processedEventIds.contains(eventId);
    }

    public List<ProductDomainEvent> getProcessedEvents() {
        return List.copyOf(processedEvents);
    }

    public List<String> getDeadLetterPayloads() {
        return List.copyOf(deadLetterPayloads);
    }

    public int getDuplicateIgnoredCount() {
        return duplicateIgnoredCount.get();
    }

    public void resetState() {
        processedEventIds.clear();
        processedEvents.clear();
        deadLetterPayloads.clear();
        duplicateIgnoredCount.set(0);
    }
}
