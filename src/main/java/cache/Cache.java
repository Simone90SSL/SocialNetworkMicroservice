package cache;

import domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import transaction.following.consumer.TransactionFollowingConsumer;

import java.util.HashMap;

public class Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cache.class);

    private static HashMap<Long, User> userCache = new HashMap<>();

    public synchronized static void addUser(User user){
        LOGGER.debug("Ass user to the cache '{}'", user);
        userCache.put(user.getTwitterId(), user);
    }

    public synchronized static User getUser(Long TwitterId){
        LOGGER.debug("Getting user from the cache with twitter id '{}'", TwitterId);
        return userCache.get(TwitterId);
    }
}
