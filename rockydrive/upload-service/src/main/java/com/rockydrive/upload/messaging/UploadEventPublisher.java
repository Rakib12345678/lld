package com.rockydrive.upload.messaging;

import com.rockydrive.common.event.FileUploadedEvent;
import com.rockydrive.common.event.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes to Kafka instead of calling metadata-service's commit endpoint synchronously, so
 * finalize doesn't take a hard dependency on metadata-service's availability/latency.
 */
@Service
public class UploadEventPublisher {

    private static final String TOPIC = KafkaTopics.FILE_UPLOADED;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UploadEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishFileUploaded(FileUploadedEvent event) {
        kafkaTemplate.send(TOPIC, event.fileId(), event);
    }
}
