package com.di2it.notification_service.config;

import com.di2it.notification_service.infrastructure.messaging.payload.TicketDocumentUploadedPayload;
import com.di2it.notification_service.infrastructure.messaging.payload.TicketStatusChangedPayload;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for notification-service. Separate container factories per payload type.
 * JsonDeserializers are owned by Kafka consumer factory lifecycle (PMD CloseResource: false positive).
 */
@Configuration
@ConditionalOnProperty(name = "notification-service.kafka.enabled", havingValue = "true")
@SuppressWarnings("PMD.CloseResource")
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:notification-service}")
    private String groupId;

    private Map<String, Object> baseConsumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TicketStatusChangedPayload>
    statusChangedListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TicketStatusChangedPayload> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        JsonDeserializer<TicketStatusChangedPayload> deserializer =
            new JsonDeserializer<>(TicketStatusChangedPayload.class, false);
        deserializer.addTrustedPackages("*");
        Map<String, Object> props = new HashMap<>(baseConsumerConfig());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        ConsumerFactory<String, TicketStatusChangedPayload> consumerFactory =
            new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TicketDocumentUploadedPayload>
    documentUploadedListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TicketDocumentUploadedPayload> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        JsonDeserializer<TicketDocumentUploadedPayload> deserializer =
            new JsonDeserializer<>(TicketDocumentUploadedPayload.class, false);
        deserializer.addTrustedPackages("*");
        Map<String, Object> props = new HashMap<>(baseConsumerConfig());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        ConsumerFactory<String, TicketDocumentUploadedPayload> consumerFactory =
            new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
