package transaction;

import loader.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import sample.data.neo4j.UserNode;


@Component
public class TransactionProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionProducer.class);

    private static final String FOLLOWING = "followingtransactionb";
    private static final String USER = "usertransactionb";
    private static final String TWEETS = "tweetstransactionb";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    public void sendFollowing(UserNode userNode, Loader.LOAD_STATUS loadStatus) {
        send(FOLLOWING, userNode.getTwitterId(), ""+loadStatus);
    }

    public void sendUser(UserNode userNode, Loader.LOAD_STATUS loadStatus) {
        send(USER, userNode.getTwitterId(), ""+loadStatus);
    }

    public void sendTweets(UserNode userNode, Loader.LOAD_STATUS loadStatus) {
        send(TWEETS, userNode.getTwitterId(), ""+loadStatus);
    }

    private void send(String transactionType, long twitterId, String transactionStatus){
        LOGGER.debug("Sending '{}' transaction resposnse for user '{}' with status '{}' ",
                transactionType, twitterId, transactionStatus);
        kafkaTemplate.send(transactionType, twitterId+","+transactionStatus);
    }
}
