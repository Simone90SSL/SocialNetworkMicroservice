package cache;

import sample.data.neo4j.TweetNode;
import sample.data.neo4j.UserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cache.class);

    private static HashMap<Long, UserNode> userCache = new HashMap<>();
    private static HashMap<Long, TweetNode> tweetCache = new HashMap<>();

    public synchronized static void addUser(UserNode userNode){
        LOGGER.debug("Ass user to the cache '{}'", userNode);
        userCache.put(userNode.getTwitterId(), userNode);
    }

    public synchronized static UserNode getUser(Long twitterId){
        LOGGER.debug("Getting user from the cache with twitter id '{}'", twitterId);
        return userCache.get(twitterId);
    }

    public synchronized static void addTweet(TweetNode tweetNode){
        LOGGER.debug("Ass user to the cache '{}'", tweetNode);
        tweetCache.put(tweetNode.getTwitterId(), tweetNode);
    }

    public synchronized static TweetNode getTweet(Long twitterId){
        LOGGER.debug("Getting user from the cache with twitter id '{}'", twitterId);
        return tweetCache.get(twitterId);
    }
}
