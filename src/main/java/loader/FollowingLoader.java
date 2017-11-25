package loader;

import cache.Cache;
import org.neo4j.ogm.exception.CypherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import repository.neo4j.UserNodeRepository;
import sample.data.neo4j.UserNode;
import transaction.TransactionConsumer;
import transaction.TransactionProducer;

import java.util.Optional;

public class FollowingLoader extends Loader{

    private static final Logger LOGGER = LoggerFactory.getLogger(FollowingLoader.class);
    private static final int THRESHOLD = 10000;


    public FollowingLoader(UserNodeRepository userNodeRepository, UserNode userNode) {
        super(userNodeRepository, userNode);
    }

    @Override
    public void startLoad(String dataToLoad) {

        // Check number of following is less than threshold
        String[] inputFollowingArray = dataToLoad.split(",");
        if (inputFollowingArray.length > THRESHOLD){
            throw new RuntimeException("Too many following");
        }

        UserNode followedUserNode;
        long twitterId;

        for (String TwitterIdStr : inputFollowingArray) {

            twitterId = Long.parseLong(TwitterIdStr);

            // This is one of the following of the current node
            if (userNode.follows.contains(new UserNode(twitterId))) {
                LOGGER.debug("'{}' already follow '{}' --> SKIP IT", userNode, twitterId);
                continue;
            }

            followedUserNode = Optional
                    .ofNullable(Cache.getUser(twitterId))
                    .orElse(Optional
                            .ofNullable(userNodeRepository.findByTwitterId(twitterId))
                            .orElseGet(() ->
                            {
                                UserNode x = new UserNode(Long.parseLong(TwitterIdStr));
                                this.userNodeRepository.save(x);
                                return x;
                            })
                    );

            LOGGER.debug("'{}' has following '{}'", userNode.getTwitterId(), followedUserNode);
            userNodeRepository.addFollow(userNode.getTwitterId(), followedUserNode.getTwitterId());
        }
    }

    @Override
    public void sendTransactionResult(TransactionProducer transactionProducer) {
        transactionProducer.sendFollowing(userNode, loadStatus);
    }
}
