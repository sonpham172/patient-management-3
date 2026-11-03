package org.study.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consumeEvent(byte[] event) {
        log.info("Received raw Kafka message, size={}", event.length);
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);
            log.info("Success deserializing at: [patientId={}, email={}, name={}]",
                    patientEvent.getPatientId(),
                    patientEvent.getEmail(),
                    patientEvent.getName());
        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing at: {}", e.getMessage());
        }
    }
}
