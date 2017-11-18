package loader;

import cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.neo4j.UserNodeRepository;
import sample.data.neo4j.UserNode;
import transaction.TransactionConsumer;
import transaction.TransactionProducer;

import java.util.Optional;

public class FollowingLoader extends Loader{

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionConsumer.class);
    private static final int THRESHOLD = 10000;


    public FollowingLoader(UserNodeRepository userNodeRepository, UserNode userNode) {
        super(userNodeRepository, userNode);
    }

    @Override
    public void startLoad(String dataToLoad) {

        // Check number of following is less than threshold
        String[] inputFollowingArray = dataToLoad.split(",");
        if (inputFollowingArray.length > THRESHOLD){
            loadStatus = LOAD_STATUS.LOAD_KO;
            return;
        }

        UserNode followedUserNode;
        long twitterId;
        int addedFollowing = 0;     // Counter to save intermediate data

        for (String TwitterIdStr : inputFollowingArray) {

            twitterId = Long.parseLong(TwitterIdStr);

            // This is one of the following of the current node
            if (userNode.follows.contains(new UserNode(twitterId))) {
                LOGGER.debug("'{}' already follow '{}' --> SKIP IT", userNode, twitterId);
                continue;
            }

            followedUserNode = Optional
                    .ofNullable(Cache.getUser(twitterId))
                    .orElse(userNodeRepository.findByTwitterId(twitterId));
            if (followedUserNode == null) {
                LOGGER.debug("'{}' not found --> CREATE", twitterId);
                followedUserNode = new UserNode(twitterId);
                this.userNodeRepository.save(followedUserNode);
            }
            Cache.addUser(followedUserNode);

            LOGGER.debug("'{}' has following '{}'", twitterId, followedUserNode);

            userNode.follows.add(followedUserNode);

            // Save intermediate data every 500 following
            addedFollowing++;
            if (addedFollowing % 500 == 0){
                LOGGER.debug("Save intermediate result for user '{}'", userNode);
                this.userNodeRepository.save(userNode);
            }
        }
        this.userNodeRepository.save(userNode);
        loadStatus = LOAD_STATUS.LOAD_OK;
    }

    @Override
    public void sendTransactionResult(TransactionProducer transactionProducer) {
        transactionProducer.sendFollowing(userNode, loadStatus);
    }
}
