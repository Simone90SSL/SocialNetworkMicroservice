package loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.mongodb.TweetDocumentRepository;
import repository.neo4j.HashTagNodeRepository;
import repository.neo4j.UserNodeRepository;
import sample.data.neo4j.UserNode;
import transaction.TransactionConsumer;
import transaction.TransactionProducer;

public abstract class Loader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionConsumer.class);

    public enum LOAD_STATUS {OK, KO};
    public enum LOAD_TYPE {FOLLOWING, USER, TWEETS};

    protected UserNodeRepository userNodeRepository;

    protected LOAD_STATUS loadStatus = null;
    protected UserNode userNode;

    public Loader(UserNodeRepository userNodeRepository, UserNode userNode){
        this.userNodeRepository = userNodeRepository;
        this.userNode = userNode;
    }

    public static Loader getNewInstance(LOAD_TYPE loadType,
                                        TweetDocumentRepository tweetDocumentRepository,
                                        HashTagNodeRepository hashTagNodeRepository,
                                        UserNodeRepository userNodeRepository,
                                        UserNode userNode){
        LOGGER.debug("Instantiating a '{}' loader", loadType);
        if (loadType == LOAD_TYPE.FOLLOWING){
            return new FollowingLoader(userNodeRepository, userNode);

        } else if (loadType == LOAD_TYPE.USER){
            return new UserLoader(userNodeRepository, userNode);

        } else if (loadType == LOAD_TYPE.TWEETS){
            return new TweetsLoader(tweetDocumentRepository, hashTagNodeRepository, userNodeRepository, userNode);

        } else{
            throw new RuntimeException("LOADER NOT FOUND");
        }
    }

    public void setLoadStatus(LOAD_STATUS loadStatus) {
        this.loadStatus = loadStatus;
    }

    public LOAD_STATUS getLoadStatus() {
        return loadStatus;
    }

    public abstract void startLoad(String dataToLoad);
    public abstract void sendTransactionResult(TransactionProducer transactionProducer);
}


