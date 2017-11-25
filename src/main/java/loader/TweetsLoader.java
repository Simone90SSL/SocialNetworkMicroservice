package loader;

import cache.Cache;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.mongodb.TweetDocumentRepository;
import repository.neo4j.HashTagNodeRepository;
import repository.neo4j.UserNodeRepository;
import sample.data.mongodb.TweetDocument;
import sample.data.neo4j.HashTagNode;
import sample.data.neo4j.TagsRelation;
import sample.data.neo4j.UserNode;
import transaction.TransactionConsumer;
import transaction.TransactionProducer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TweetsLoader extends Loader{
    private static final Logger LOGGER = LoggerFactory.getLogger(TweetsLoader.class);
    private TweetDocumentRepository tweetDocumentRepository;
    private HashTagNodeRepository hashTagNodeRepository;

    public TweetsLoader(TweetDocumentRepository tweetDocumentRepository,
                        HashTagNodeRepository hashTagNodeRepository,
                        UserNodeRepository userNodeRepository,
                        UserNode userNode) {
        super(userNodeRepository, userNode);
        this.tweetDocumentRepository = tweetDocumentRepository;
        this.hashTagNodeRepository = hashTagNodeRepository;
    }

    @Override
    public void startLoad(String dataToLoad) {

        if (dataToLoad.isEmpty() || "[]".equals(dataToLoad)){
            // Empty array
            loadStatus = LOAD_STATUS.OK;
            return;
        }

        ArrayList<TweetDocument> tweetDocumentList = new ArrayList<>();
        JSONArray tweetArrayJson = new JSONArray(dataToLoad);
        JSONObject tweetJsonObj;

        TweetDocument tweetDocument;
        String text, createdAt, geoLocation, lang;
        long tweetTwitterId;
        for(int i=0; i<tweetArrayJson.length(); i++){
            tweetJsonObj = tweetArrayJson.getJSONObject(i);
            tweetTwitterId = Long.parseLong(getStringValueFromJsonOject(tweetJsonObj, "ID"));
            LOGGER.debug("User '{}': Tweet '{}'", userNode.getTwitterId(), tweetTwitterId);

            if (tweetDocumentRepository.exists(tweetTwitterId)){
                LOGGER.debug("User '{}': Tweet '{}' already loaded", userNode.getTwitterId(), tweetTwitterId);
                continue;
            }

            text = getStringValueFromJsonOject(tweetJsonObj, "TEXT");
            createdAt = getStringValueFromJsonOject(tweetJsonObj, "CREATEDAT");
            geoLocation = getStringValueFromJsonOject(tweetJsonObj, "GEOLOCATION");
            lang = getStringValueFromJsonOject(tweetJsonObj, "LANG");

            tweetDocument = new TweetDocument(tweetTwitterId, userNode.getTwitterId(), text, createdAt, geoLocation, lang);
            tweetDocumentList.add(tweetDocument);

            Set<String> tags = getHashTagFromTweet(text);
            int tagIndexOf;
            HashTagNode hashTagNode;
            for (String tag: tags){
                hashTagNode =
                        Optional
                        .ofNullable(Cache.getHashTag(tag))
                        .orElse(
                                Optional
                                        .ofNullable(hashTagNodeRepository.findByHashTag(tag))
                                        .orElseGet(
                                                () -> {
                                                    HashTagNode h = new HashTagNode(tag);
                                                    hashTagNodeRepository.save(h);
                                                    return h;
                                                })
                        );
                tagIndexOf = userNode.tagsRelations.indexOf(new TagsRelation(1, userNode, hashTagNode));
                if (tagIndexOf == -1)
                    userNode.tagsRelations.add(new TagsRelation(1, userNode, hashTagNode));
                else{
                    userNode.tagsRelations.get(tagIndexOf).incrementCount();
                }
            }
        }
        tweetDocumentRepository.save(tweetDocumentList);
        userNodeRepository.save(userNode);
    }

    private static Set<String> getHashTagFromTweet(String text) {
        Set<String> tags = new HashSet<>();
        Matcher matcher = Pattern.compile("#(\\w+)").matcher(text);
        while (matcher.find()) {
            tags.add(matcher.group(1));
        }
        return tags;
    }

    @Override
    public void sendTransactionResult(TransactionProducer transactionProducer) {
        transactionProducer.sendTweets(userNode, loadStatus);
    }

    private String getStringValueFromJsonOject(JSONObject jsonObj, String propertyName){
        try {
            return jsonObj.getString(propertyName);
        } catch (JSONException je){
            return "";
        }
    }
}
