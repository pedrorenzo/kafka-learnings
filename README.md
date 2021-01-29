## Kafka Learnings:
In order to learn about Kafka, I set up this readme with relevant information on this subject, in addition to some simple Java examples.

Much of the theory and also the implemented examples were taken from this great [course](https://www.udemy.com/course/apache-kafka/).

## About Kafka:
Integrations between different systems can be very complicated and have large amounts of data, making connections slow.

Kafka helps us with this, as Source Systems will send messages to Apache Kafka and Target Systems will consume these messages directly from Kafka. All of this in real time (latency of less than 10ms).
Kafka can be used in several cases, for example:
* Messaging System;
* Application Logs gathering;
* Stream processing;
* Integration with Big Data technologies.

### Some theory:
* Topics: A particular stream of data, similar to a database table, are split in partitions;
* Partitions: Are ordered inside of Topics, each message within a partition gets an incremental id, called offset;
* Offset only has a meaning for a specific partition (offset 3 in partition 0 does not represent the same data as offset 3 in partition 1);
* Once the data is written to a partition, it can not be changed (immutability);
* Messages are appended to a topic-partition in the order they are sent;
* Brokers: A Kafka cluster is composed of multiple brokers (servers), each broker has one ID, each broker contains certain topic partitions;
* Topic replication factor: If a broker goes down, we need another broker to be able to respond in place of what went down, so the Topic replication factor must be greater than 1 (usually 2 or 3). Assuming it is 2, I will have Topic-A, Partition 0 in 2 different brokers. Only 1 broker will be the leader, the others will only be replicas of the leader;
* Producers: Write data to topics (which is made of partitions) and knows automatically to which broker and partition to write to (load balancing). In case of Broker failures, Producers will automatically recover;
* Producers can choose to receive confirmation of data writes:
  * Acks = 0, Won't wait for confirmation (possible data loss);
  * Acks = 1, Will wait for leader confirmation (limited data loss);
  * Acks = all, Will wait for leader + replicas confirmation (no data loss).
* Producers can choose to send a key with the message:
  * key = null, round robin (sequential to the brokers);
  * key != null, all messages will go to the same partition.
* Consumers: Read data from a topic (identified by name) and knows automatically which broker to read from. In case of Broker failures, Consumers will automatically recover;
* Consumers read data in order within each partitions;
* Consumer Groups (basically represents an application): Consumers read data in consumers groups and each consumer within a group reads from exclusive partitions. If you have more consumers than partitions, some consumers will be inactive. If you have more partitions than consumers, consumers can read from more than one partition.
* If a consumer from a consumer group dies, it will be able to read back from where it left off (which offset), thanks to the committed consumer offsets;
* Delivery semantics for consumers (consumers choose when to commit offsets):
  * At most once: offsets are committed as soon as the message is received and if the processing goes wrong, the message will be lost;
  * At least once (usually preferred): offsets are committed after the message is processed and if the processing goes wrong, the message will be read again. PS: Make sure if you process the message again, won't impact (idempotent);
  * Exactly once: Can be achieved for Kafka to Kafka. Kafka to External System, use idempotent consumer.
* Kafka Broker Discovery: You only need to connect to one broker, and you will be connected to the entire cluster. Each broker knows about all brokers, topics and partitions;
* Zookeeper:
  * Manages brokers (keeps a list of them);
  * Helps in performing leader election for partitions;
  * Sends notifications to Kafka in case of changes (e.g. new topic);
  * Kafka can't work without Zookeeper;
  * Operates with an odd number of servers;
  * Has a leader (handle writes) and the rest are followers (handle reads).

## Configuring your environment:
* Download Kafka (https://kafka.apache.org/downloads - For this example, I used version 2.7.0);
* We will change the value of the dataDir property of config/zookeeper.properties, to a data/zookeeper file that we will create at the same level as the bin file. This way, snapshots will no longer be temporary as previously configured;
* Inside the kafka bin file, run the command (on the command line) to start Zookeeper:
  * zookeeper-server-start.sh ../config/zookeeper.properties -> It has to continue running while you use Kafka.
* We will also change the value of the logs.dir property of config/server.properties (kafka properties), to a data/kafka file that we will create at the same level as the bin file. That way, the logs will not be lost;
* Inside the Kafka file, we will run the command (on the command line) to start Kafka:
  * kafka-server-start.sh config/server.properties

## Important Kafka Commands:

### Topics:
  * To create a new topic:
    * kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --create --partitions 3 --replication-factor 1 -> where first_topic is the name of the topic
  * To list topics:
    * kafka-topics.sh --zookeeper 127.0.0.1:2181 --list
  * To get more details from a specific topic:
    * kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --describe
  * To delete a topic:
    * kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --delete

### Producer:
  * To produce for a topic, using default config:
    * kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic -> where :9092 is where kafka is "listening". Everything that you type now, is going to be a message
  * To produce for a topic, using custom configs, for example, acks = all:
    * kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic --producer-property acks=all
  * If we execute the previous command for a topic that does not exist, Kafka will automatically create the topic, using some default configuration. This may not be a good thing, as the default configurations may be configured in a way that we do not consider good (e.g. using only 1 partition). Anyway, we can change the default configurations on config/server.properties.

### Consumer:
  * To consume messages from a topic:
    * kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic -> It will consume messages produced after running this command. It will not do anything with the ones that were produced before this consumer started. The order of the messages on consumers is per partition!
  * To consume messages from a topic, including messages that have been previously produced:
    * kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning

### Consumers in Group:
  * To consume messages from a topic, with a group:
    * kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application -> If we run exactly that same command in another terminal and keep 2 consumers, from the same group in parallel, we will see that if we send 2 messages, each one will be consumed by a different consumer, as they are from the same group.
  * To consume messages from a topic, with a group, including messages that have been previously produced:
    * kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-second-application --from-beginning -> If we stop that consumer and run the same command again, we will see that no messages will be consumed. This is because this group has already consumed these messages, that is, the offsets were commited to Kafka, so, we can say that the command from beggining only has "function" once. If we already run this group without from-beginning command, we can't run with this command. If we stop the consumer, continue to produce messages and then run the consumer again, it will consume the messages we produced while he was offline (even if the from-beginning wasn't executed).
  * To list consumers group:
    * kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --list
  * To get more details of a consumer group:
    * kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --describe --group my-first-application -> The LAG field will show how many messages this group is pending to read.
  * To reset the offsets, to read again some message(s):
    * kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --group my-first-application --reset-offsets --to-earliest --execute --topic first_topic -> There are more options, different than --to-earliest.
