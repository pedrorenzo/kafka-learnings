package com.github.pedrorenzo.kafka.producers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * With call back, we can get some informantions about the topic, partition and offset.
 */
public class ProducerDemoWithCallback {

    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallback.class);

        String bootstrapServers = "127.0.0.1:9092";

        // Create Producer properties:
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the producer:
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int i = 0; i < 10; i++) {
            // Create a producer record:
            ProducerRecord<String, String> record =
                    new ProducerRecord<>("first_topic", "hello world " + i);

            // Send data - asynchronous:
            producer.send(record, (recordMetadata, e) -> {
                // Executes every time a record is successfully sent or an exception is thrown
                if (e == null) {
                    // The record was successfully sent
                    logger.info("Received new metadata. \n" +
                            "Topic: " + recordMetadata.topic() + "\n" +
                            "Partition: " + recordMetadata.partition() + "\n" +
                            "Offset: " + recordMetadata.offset() + "\n" +
                            "Timestamp: " + recordMetadata.timestamp());
                } else {
                    logger.error("Error while producing", e);
                }
            });
        }

        // Flush data (as it is asynchronous):
        producer.flush();

        // Flush and close producer:
        producer.close();
    }

}
