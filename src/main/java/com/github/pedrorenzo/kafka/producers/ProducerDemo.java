package com.github.pedrorenzo.kafka.producers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerDemo {

    public static void main(String[] args) {
        String bootstrapServers = "127.0.0.1:9092";

        // Create Producer properties (you can see all the properties on the Kafka documentation
        // https://kafka.apache.org/documentation/#producerconfigs):
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Key and value serializers will tell the Producer what type of value we're sending to Kafka and
        // how it could be serialized. In this case, we send Strings.
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the producer:
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // Create a producer record:
        ProducerRecord<String, String> record =
                new ProducerRecord<>("first_topic", "hello world");

        // Send data - asynchronous:
        producer.send(record);

        // Flush data (as it is asynchronous):
        producer.flush();

        // Flush and close producer:
        producer.close();
    }

}

