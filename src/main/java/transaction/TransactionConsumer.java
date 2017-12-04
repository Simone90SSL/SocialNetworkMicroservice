package transaction;

import cache.Cache;
import loader.Loader;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import repository.mongodb.TweetDocumentRepository;
import repository.neo4j.HashTagNodeRepository;
import repository.neo4j.UserNodeRepository;
import sample.data.neo4j.UserNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

@Component
public class TransactionConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionConsumer.class);

    @Autowired
    private UserNodeRepository userNodeRepository;
    @Autowired
    private TweetDocumentRepository tweetDocumentRepository;
    @Autowired
    private HashTagNodeRepository hashTagNodeRepository;
    @Autowired
    private TransactionProducer transactionProducer;


    @KafkaListener(topics = "${kafka.topic.followingtransactiona}")
    public void receiveFollowing(String inputFollowingTransaction) {
        receive(inputFollowingTransaction, Loader.LOAD_TYPE.FOLLOWING);
    }

    @KafkaListener(topics = "${kafka.topic.usertransactiona}")
    public void receiveUser(String inputUserTransaction) {
        receive(inputUserTransaction, Loader.LOAD_TYPE.USER);
    }

    @KafkaListener(topics = "${kafka.topic.tweetstransactiona}")
    public void receiveTweets(String inputTweetsTransaction) {
        receive(inputTweetsTransaction, Loader.LOAD_TYPE.TWEETS);
    }

    private void receive(final String inputTransaction, final Loader.LOAD_TYPE loadType){
        LOGGER.info("Received transaction of type '{}'", loadType);
        // Just return if the input is not acceptable
        if (inputTransaction==null || inputTransaction.isEmpty()){
            return;
        }

        UserNode userNode = getUserNodeFromTransactionInput(userNodeRepository, inputTransaction);
        String dataToLoad = getDataFromTransactionInput(inputTransaction);

        Loader loader = Loader.getNewInstance(loadType, tweetDocumentRepository, hashTagNodeRepository, userNodeRepository, userNode);
        try {
            LOGGER.info("Start Loading '{}' of '{}'", loadType, userNode.getTwitterId());
            loader.startLoad(dataToLoad);
            loader.setLoadStatus(Loader.LOAD_STATUS.OK);
        } catch (JSONException je) {
            LOGGER.error("JSON EXCEPTION DURING '{}' LOADING of '{}'", loadType, userNode.getTwitterId());
            LOGGER.error(je.getMessage(), je);
            je.printStackTrace();
            loader.setLoadStatus(Loader.LOAD_STATUS.KO);
        } catch (Exception e){
            LOGGER.error("EXCEPTION DURING '{}' LOADING of '{}'", loadType, userNode.getTwitterId());
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
            loader.setLoadStatus(Loader.LOAD_STATUS.KO);
        }
        loader.sendTransactionResult(transactionProducer);
    }

    private static synchronized UserNode getUserNodeFromTransactionInput(
            UserNodeRepository userNodeRepository, String dataTransactionInput){

        // Read UserNode in input
        long twitterId = Long.parseLong(dataTransactionInput.split(":")[0]);

        // This is the current node
        LOGGER.debug("'{}' is the start node", twitterId);

        UserNode userNode = Optional
                .ofNullable(Cache.getUser(twitterId))
                .orElse(Optional
                        .ofNullable(userNodeRepository.findByTwitterId(twitterId))
                        .orElseGet(() ->
                        {
                            LOGGER.debug("'{}' not found --> CREATE", twitterId);
                            UserNode u = new UserNode(twitterId);
                            userNodeRepository.save(u);
                            return u;
                        })
                );

        if (userNode.follows == null){
            userNode.follows = new HashSet<>();
        }
        if (userNode.tagsRelations == null){
            userNode.tagsRelations = new ArrayList<>();
        }
        Cache.addUser(userNode);

        return userNode;
    }

    private String getDataFromTransactionInput(String dataTransactionInput){
        // Return all after the first colon
        return dataTransactionInput.substring(dataTransactionInput.indexOf(':')+1);
    }
}
