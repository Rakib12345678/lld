package com.rockydrive.metadata.event;

import com.rockydrive.common.event.FileDeletedEvent;
import com.rockydrive.common.event.FileSharedEvent;
import com.rockydrive.common.event.FileUpdatedEvent;
import com.rockydrive.common.event.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FileEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FileEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishFileUpdated(FileUpdatedEvent event) {
        kafkaTemplate.send(KafkaTopics.FILE_UPDATED, event.fileId(), event);
    }

    public void publishFileDeleted(FileDeletedEvent event) {
        kafkaTemplate.send(KafkaTopics.FILE_DELETED, event.fileId(), event);
    }

    public void publishFileShared(FileSharedEvent event) {
        kafkaTemplate.send(KafkaTopics.FILE_SHARED, event.fileId(), event);
    }
}
