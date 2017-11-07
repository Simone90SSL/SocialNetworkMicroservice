package transaction.following.consumer;

import cache.Cache;
import sample.data.neo4j.UserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import repository.neo4j.UserNodeRepository;
import transaction.following.producer.TransactionFollowingProducer;

import java.util.HashMap;
import java.util.HashSet;

@Component
public class TransactionFollowingConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionFollowingConsumer.class);

    private static final int THRESHOLD = 10000;
    @Autowired
    private UserNodeRepository userNodeRepository;

    @Autowired
    private TransactionFollowingProducer transactionFollowingProducer;

    @KafkaListener(topics = "${kafka.topic.followingtransactiona}")
    public void receive(String following) {

        long sysTime = System.currentTimeMillis();
        LOGGER.info("Received Following Transaction '{}'", sysTime);

        UserNode startUserNode = null;
        boolean isStartNode = true;
        long TwitterId;
        int addedFollowing = 0;
        HashMap<Long, UserNode> followingMaps = new HashMap<Long, UserNode>();

        String[] followingArray = following.split(",");
        LOGGER.info("Transaction '{}' with '{}' elements", sysTime, followingArray.length-1);
        try {
            for (String TwitterIdStr : followingArray) {
                TwitterId = Long.parseLong(TwitterIdStr);
                if (isStartNode) {
                    // This is the current node
                    LOGGER.debug("'{}' is the start node", TwitterId);

                    // Check number of following is less that threshold
                    if (followingArray.length > THRESHOLD){
                        transactionFollowingProducer.send("" + TwitterId+",KO");
                        return;
                    }

                    startUserNode = userNodeRepository.findByTwitterId(TwitterId);
                    if (startUserNode == null) {
                        // Get information of the user --> insert into the DB
                        LOGGER.debug("'{}' not found --> CREATE", TwitterId);
                        startUserNode = new UserNode(TwitterId);
                        startUserNode.follows = new HashSet<>();
                    } else if (startUserNode.follows != null){
                        for (UserNode followingUserNode : startUserNode.follows) {
                            LOGGER.debug("'{}' found --> mapping already following", TwitterId);
                            followingMaps.put(followingUserNode.getTwitterId(), followingUserNode);
                        }
                    } else{
                        startUserNode.follows = new HashSet<>();
                    }
                    isStartNode = false;
                } else {
                    // This is one of the following of the current node
                    if (followingMaps.get(TwitterId) != null) {
                        LOGGER.debug("'{}' already follow '{}' --> SKIP IT", startUserNode, TwitterId);
                        continue;
                    }

                    UserNode followedUserNode = Cache.getUser(TwitterId);
                    if (followedUserNode == null){
                        followedUserNode = userNodeRepository.findByTwitterId(TwitterId);
                        if (followedUserNode == null) {
                            LOGGER.debug("'{}' not found --> CREATE", TwitterId);
                            followedUserNode = new UserNode(
                                    TwitterId);
                            this.userNodeRepository.save(followedUserNode);
                        }
                        Cache.addUser(followedUserNode);
                    }
                    LOGGER.debug("'{}' has following '{}'", TwitterId, followedUserNode);
                    //startUserNode.follow(followedUserNode);
                    startUserNode.follows.add(followedUserNode);

                    addedFollowing++;
                    if (addedFollowing % 1000 == 0){
                        LOGGER.debug("Save intermediate result for user '{}'", startUserNode);
                        this.userNodeRepository.save(startUserNode);
                    }
                }
            }
            this.userNodeRepository.save(startUserNode);
            transactionFollowingProducer.send("" + startUserNode.getTwitterId()+",OK");
            return;
        } catch (Exception e){
            LOGGER.error("Error during transaction following consumer with input '{}'", following);
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }

        transactionFollowingProducer.send("" + startUserNode.getTwitterId()+",KO");
    }
}