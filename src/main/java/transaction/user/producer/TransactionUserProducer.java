package transaction.user.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionUserProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionUserProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String payload) {
        LOGGER.debug("sending follower id='{}' to topic='usertransactionb'", payload);
        kafkaTemplate.send("usertransactionb", payload);
    }
}