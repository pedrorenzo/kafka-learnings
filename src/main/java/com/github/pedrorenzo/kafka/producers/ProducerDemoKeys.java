package com.github.pedrorenzo.kafka.producers;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Providing a key, we guarantee that the same key always goes to the same partition.
 */
public class ProducerDemoKeys {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final Logger logger = LoggerFactory.getLogger(ProducerDemoKeys.class);

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
            String topic = "first_topic";
            String value = "hello world " + i;
            String key = "id_" + i;

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            logger.info("Key: " + key);
            // id_0 is going to partition 1
            // id_1 partition 0
            // id_2 partition 2
            // id_3 partition 0
            // id_4 partition 2
            // id_5 partition 2
            // id_6 partition 0
            // id_7 partition 2
            // id_8 partition 1
            // id_9 partition 2


            // Send data - asynchronous:
            producer.send(record, (recordMetadata, e) -> {
                // Executes every time a record is successfully sent or an exception is thrown
                if (e == null) {
                    // the record was successfully sent
                    logger.info("Received new metadata. \n" +
                            "Topic: " + recordMetadata.topic() + "\n" +
                            "Partition: " + recordMetadata.partition() + "\n" +
                            "Offset: " + recordMetadata.offset() + "\n" +
                            "Timestamp: " + recordMetadata.timestamp());
                } else {
                    logger.error("Error while producing", e);
                }
            }).get(); // Block the .send() to make it synchronous - don't do this in production!
        }

        // Flush data (as it is asynchronous):
        producer.flush();

        // Flush and close producer:
        producer.close();
    }

}
