package transaction.following.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionFollowingProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionFollowingProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String payload) {
        LOGGER.debug("sending follower id='{}' to topic='followingtransactionb'", payload);
        kafkaTemplate.send("followingtransactionb", payload);
    }
}