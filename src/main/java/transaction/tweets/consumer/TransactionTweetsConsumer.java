package transaction.tweets.consumer;

import cache.Cache;
import sample.data.neo4j.TweetNode;
import sample.data.neo4j.UserNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import repository.neo4j.TweetNodeRepository;
import repository.neo4j.UserNodeRepository;
import transaction.tweets.producer.TransactionTweetsProducer;

import java.util.*;

@Component
public class TransactionTweetsConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionTweetsConsumer.class);

    @Autowired
    private UserNodeRepository userNodeRepository;
    @Autowired
    private TweetNodeRepository tweetNodeRepository;

    @Autowired
    private TransactionTweetsProducer transactionTweetsProducer;

    @KafkaListener(topics = "${kafka.topic.tweetstransactiona}")
    public void receive(String tweets) {

        long sysTime = System.currentTimeMillis();
        LOGGER.info("Received Tweets Transaction '{}'", sysTime);

        long userTwitterId = Long.parseLong(tweets.split(":")[0]);
        ArrayList<TweetNode> tweetNodeList = new ArrayList<>();
        UserNode userNode = Optional.ofNullable(Cache.getUser(userTwitterId)).orElse(userNodeRepository.findByTwitterId(userTwitterId));
        if(userNode == null){
            userNode = new UserNode(userTwitterId);
        }
        Cache.addUser(userNode);

        HashMap<Long, TweetNode> tweetsMap = new HashMap<>();
        if (userNode.getTweets() != null){
            for (TweetNode tweetNode: userNode.getTweets()){
                tweetsMap.put(tweetNode.getTwitterId(), tweetNode);
            }
        } else{
            userNode.setTweets(new HashSet<>());
        }

        String tweetsArrayJsonString = tweets.substring(tweets.indexOf(":")+1);
        if ("[]".equals(tweetsArrayJsonString)){
            // Empty array
            transactionTweetsProducer.send(userTwitterId+",OK");
            return;
        }

        try {
            JSONArray tweetArrayJson = new JSONArray(tweetsArrayJsonString);
            JSONObject tweetJsonObj;

            TweetNode tweetNode;
            String text;
            String createdAt;
            String geoLocation;
            long tweetTwitterId;
            String lang;
            for(int i=0; i<tweetArrayJson.length(); i++){
                tweetJsonObj = tweetArrayJson.getJSONObject(i);

                text = getStringValueFromJsonOject(tweetJsonObj, "TEXT");
                createdAt = getStringValueFromJsonOject(tweetJsonObj, "CREATEDAT");
                geoLocation = getStringValueFromJsonOject(tweetJsonObj, "GEOLOCATION");
                tweetTwitterId = Long.parseLong(getStringValueFromJsonOject(tweetJsonObj, "ID"));
                lang = getStringValueFromJsonOject(tweetJsonObj, "LANG");

                // Try to get the tweet-node directly by the user-node
                tweetNode = tweetsMap.get(tweetTwitterId);

                if (tweetNode == null){
                    // If it does not exist, create it!
                    tweetNode = new TweetNode(tweetTwitterId);
                }

                if (!text.isEmpty()){
                    tweetNode.setText(text);
                }

                if (!createdAt.isEmpty()){
                    tweetNode.setCreatedAt(createdAt);
                }

                if (!geoLocation.isEmpty()){
                    tweetNode.setGeoLocation(geoLocation);
                }

                if (!lang.isEmpty()){
                    tweetNode.setLang(lang);
                }

                tweetNodeList.add(tweetNode);
                //userNode.tweets(tweetNode);
                userNode.tweets.add(tweetNode);
            }
            tweetNodeRepository.save(tweetNodeList);
            userNodeRepository.save(userNode);
            transactionTweetsProducer.send(userTwitterId+",OK");
            return;
        } catch (JSONException je) {
            LOGGER.error("Json Error during transaction tweets consumer with input '{}'", tweets);
            LOGGER.error(je.getMessage(), je);
            je.printStackTrace();
        } catch (Exception e){
            LOGGER.error("Error during transaction tweets consumer with input '{}'", tweets);
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }
        transactionTweetsProducer.send(userTwitterId+",KO");
    }

    private String getStringValueFromJsonOject(JSONObject jsonObj, String propertyName){
        String value = "";
        try {
            value = jsonObj.getString(propertyName);
        } catch (JSONException je){}
        return value;
    }
}