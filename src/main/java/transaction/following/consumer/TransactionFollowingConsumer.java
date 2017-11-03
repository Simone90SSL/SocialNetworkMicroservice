package transaction.following.consumer;

import cache.Cache;
import domain.User;
import org.neo4j.ogm.drivers.http.request.HttpRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import repository.neo4j.UserRepository;
import transaction.following.producer.TransactionFollowingProducer;

import java.util.HashMap;

@Component
public class TransactionFollowingConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionFollowingConsumer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionFollowingProducer transactionFollowingProducer;

    @KafkaListener(topics = "${kafka.topic.followingtransactiona}")
    public void receive(String following) {

        long sysTime = System.currentTimeMillis();
        LOGGER.info("Received Following Transaction '{}'", sysTime);

        User currentUser = null;
        boolean currentNode = true;
        long TwitterId;
        int addedFollowing = 0;
        int threshold = 10000;
        HashMap<Long, User> followingMaps = new HashMap<Long, User>();

        String[] followingArray = following.split(",");
        LOGGER.info("Transaction '{}' with '{}' elements", sysTime, followingArray.length-1);
        try {
            for (String TwitterIdStr : followingArray) {
                TwitterId = Long.parseLong(TwitterIdStr);
                if (currentNode) {
                    // This is the current node
                    LOGGER.debug("'{}' is the current node", TwitterId);

                    // Check number of following is less that threshold
                    if (followingArray.length > threshold){
                        transactionFollowingProducer.send("" + TwitterId+",KO");
                        return;
                    }

                    currentUser = userRepository.findByTwitterId(TwitterId);
                    if (currentUser == null) {
                        // Get information of the user --> insert into the DB
                        LOGGER.debug("'{}' not found --> CREATE", TwitterId);
                        currentUser = new User(TwitterId);
                        this.userRepository.save(currentUser);
                        currentUser = this.userRepository.findByTwitterId(TwitterId);
                    } else if (currentUser.follows != null){
                        for (User followingUser : currentUser.follows) {
                            LOGGER.debug("'{}' found --> mapping already following", TwitterId);
                            followingMaps.put(followingUser.getId(), followingUser);
                        }
                    }
                    currentNode = false;
                } else {
                    // This is one of the following of the current node
                    if (followingMaps.get(TwitterId) != null) {
                        LOGGER.debug("'{}' already follow '{}' --> SKIP IT", currentUser, TwitterId);
                        continue;
                    }

                    User userFollowed = Cache.getUser(TwitterId);
                    if (userFollowed == null){
                        userFollowed = userRepository.findByTwitterId(TwitterId);
                        if (userFollowed == null) {
                            LOGGER.debug("'{}' not found --> CREATE", TwitterId);
                            userFollowed = new domain.User(
                                    TwitterId);
                            this.userRepository.save(userFollowed);
                            userFollowed = this.userRepository.findByTwitterId(TwitterId);
                        }
                        Cache.addUser(userFollowed);
                    }
                    LOGGER.debug("'{}' has following '{}'", TwitterId, userFollowed);
                    currentUser.follow(userFollowed);

                    addedFollowing++;
                    if (addedFollowing % 1000 == 0){
                        LOGGER.debug("Save intermediate result for user '{}'", currentUser);
                        this.userRepository.save(currentUser);
                    }
                }

            }
            this.userRepository.save(currentUser);
            transactionFollowingProducer.send("" + currentUser.getTwitterId()+",OK");
        } catch (HttpRequestException hre){
            LOGGER.error("Error caused by connection, transaction '{}'", sysTime);
        }
    }
}