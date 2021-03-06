package cache;

import sample.data.mongodb.TweetDocument;
import sample.data.neo4j.HashTagNode;
import sample.data.neo4j.UserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class Cache {

    private final static int MAX_NUMBER_ENTRIES = 10000;

    private static final Logger LOGGER = LoggerFactory.getLogger(Cache.class);

    private static HashMap<Long, UserNode> userCache = new HashMap<>();
    private static HashMap<Long, TweetDocument> tweetCache = new HashMap<>();
    private static HashMap<String, HashTagNode> hashTagCache = new HashMap<>();

    private static ArrayList<Long> userCacheFifo = new ArrayList<>();
    private static ArrayList<Long> tweetCacheFifo = new ArrayList<>();
    private static ArrayList<String> hashTagCacheFifo = new ArrayList<>();

    public synchronized static void addUser(UserNode userNode){
        if (userCache.containsKey(userNode.getTwitterId())){
            return;
        }
        LOGGER.debug("Add user to the cache '{}'", userNode);
        userCache.put(userNode.getTwitterId(), userNode);
        userCacheFifo.add(userNode.getTwitterId());

        if (userCacheFifo.size() >= MAX_NUMBER_ENTRIES){
            userCache.remove(userCacheFifo.remove(0));
        }
    }

    public synchronized static UserNode getUser(Long twitterId){
        LOGGER.debug("Getting user from the cache with twitter id '{}'", twitterId);
        return userCache.get(twitterId);
    }

    public synchronized static void addHashTag(HashTagNode hashTagNode){
        if (hashTagCache.containsKey(hashTagNode.getHashTag())){
            return;
        }
        LOGGER.debug("Add hash-tag to the cache '{}'", hashTagNode);
        hashTagCache.put(hashTagNode.getHashTag(), hashTagNode);
        hashTagCacheFifo.add(hashTagNode.getHashTag());

        if (hashTagCacheFifo.size() >= MAX_NUMBER_ENTRIES){
            hashTagCache.remove(hashTagCacheFifo.remove(0));
        }
    }

    public synchronized static HashTagNode getHashTag(String hashTag){
        LOGGER.debug("Getting hash-tag from the cache with twitter id '{}'", hashTag);
        return hashTagCache.get(hashTag);
    }

    public synchronized static void addTweet(TweetDocument tweetDocument){
        if (tweetCache.containsKey(tweetDocument.getTwitterId())){
            return;
        }
        LOGGER.debug("Ass user to the cache '{}'", tweetDocument);
        tweetCache.put(tweetDocument.getTwitterId(), tweetDocument);
        tweetCacheFifo.add(tweetDocument.getTwitterId());

        if (tweetCacheFifo.size() >= MAX_NUMBER_ENTRIES){
            tweetCache.remove(tweetCacheFifo.remove(0));
        }
    }

    public synchronized static TweetDocument getTweet(Long twitterId){
        LOGGER.debug("Getting user from the cache with twitter id '{}'", twitterId);
        return tweetCache.get(twitterId);
    }
}
