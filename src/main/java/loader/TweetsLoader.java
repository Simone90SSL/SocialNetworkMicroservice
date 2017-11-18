package loader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import repository.mongodb.TweetDocumentRepository;
import repository.neo4j.UserNodeRepository;
import sample.data.mongodb.TweetDocument;
import sample.data.neo4j.UserNode;
import transaction.TransactionProducer;

import java.util.ArrayList;

public class TweetsLoader extends Loader{

    private TweetDocumentRepository tweetDocumentRepository;

    public TweetsLoader(TweetDocumentRepository tweetDocumentRepository,
                        UserNodeRepository userNodeRepository,
                        UserNode userNode) {
        super(userNodeRepository, userNode);
        this.tweetDocumentRepository = tweetDocumentRepository;
    }

    @Override
    public void startLoad(String dataToLoad) {

        if (dataToLoad.isEmpty() || "[]".equals(dataToLoad)){
            // Empty array
            loadStatus = LOAD_STATUS.LOAD_OK;
            return;
        }

        ArrayList<TweetDocument> tweetDocumentList = new ArrayList<>();
        JSONArray tweetArrayJson = new JSONArray(dataToLoad);
        JSONObject tweetJsonObj;

        TweetDocument tweetDocument;
        String text;
        String createdAt;
        String geoLocation;
        long tweetTwitterId;
        String lang;
        for(int i=0; i<tweetArrayJson.length(); i++){
            tweetJsonObj = tweetArrayJson.getJSONObject(i);
            tweetTwitterId = Long.parseLong(getStringValueFromJsonOject(tweetJsonObj, "ID"));

            if (tweetDocumentRepository.exists(tweetTwitterId)){
                continue;
            }

            text = getStringValueFromJsonOject(tweetJsonObj, "TEXT");
            createdAt = getStringValueFromJsonOject(tweetJsonObj, "CREATEDAT");
            geoLocation = getStringValueFromJsonOject(tweetJsonObj, "GEOLOCATION");
            lang = getStringValueFromJsonOject(tweetJsonObj, "LANG");

            tweetDocument = new TweetDocument(tweetTwitterId, text, createdAt, geoLocation, lang);
            tweetDocumentList.add(tweetDocument);
        }
        tweetDocumentRepository.save(tweetDocumentList);
        userNodeRepository.save(userNode);
        loadStatus = LOAD_STATUS.LOAD_OK;
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
