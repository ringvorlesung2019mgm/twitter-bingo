package twitterbingo.connectiontest;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConnectionTest {

	private static final Logger logger = LoggerFactory.getLogger(ConnectionTest.class);

	private RingvorlesungKafkaPropertiesManager propertiesCreator;

	@Before
	public void setup() {
		propertiesCreator = new RingvorlesungKafkaPropertiesManager();
	}

	@Test
	public void doProduce() {
		try (Producer<String, String> producer = new KafkaProducer<>(propertiesCreator.createProducerProperties())) {
			for (int i = 0; i < 100; i++) {
				producer.send(new ProducerRecord<>("my-topic", Integer.toString(i), Integer.toString(i)));
			}
		}
	}


	@Test
	public void doConsume() {
		try (KafkaConsumer<String, String> consumer =
				     new KafkaConsumer<>(propertiesCreator.createConsumerProperties())) {

			// Subscribe to the topic.
			consumer.subscribe(Collections.singletonList("my-topic"));

			resetConsumer(consumer);

			final int giveUp = 1;
			int noRecordsCount = 0;

			while (true) {
				final ConsumerRecords<String, String> consumerRecords =

						consumer.poll(Duration.ofSeconds(3L));

				if (consumerRecords.count() == 0) {

					noRecordsCount++;

					if (noRecordsCount > giveUp) break;

					else continue;

				}

				consumerRecords.forEach(record -> logger.info(String.format("Consumer Record:(%s, %s, %d, %d)\n",

						record.key(), record.value(),

						record.partition(), record.offset())));

				consumer.commitAsync();

			}

		}
	}

	@Test
	public void listTopics() {
		final KafkaConsumer<String, String> consumer =
				new KafkaConsumer<>(propertiesCreator.createConsumerProperties());

		Map<String, List<PartitionInfo>> topics = consumer.listTopics();
		topics.forEach((s, list) -> logger.info(s + ": " + String.join(",", list.stream().map(Objects::toString).collect(Collectors.toList()))));
	}

	private void resetConsumer(Consumer<?, ?> consumer) {
		consumer.poll(Duration.ofMillis(1L));
		// Now there is heartbeat and consumer is "alive"
		consumer.seekToBeginning(consumer.assignment());
	}

}
